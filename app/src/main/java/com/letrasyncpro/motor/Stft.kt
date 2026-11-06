package com.letrasyncpro.motor

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * **La FFT** de tamaño `2^m` o `3·2^m` (el separador usa 6144 = 3·2048): tres FFT de radix 2 sobre las muestras
 * de 3 en 3 y se juntan con sus giros. Las tablas se calculan una vez; no es para varios hilos a la vez (cada
 * hilo, su [Fft]).
 */
class Fft(val n: Int) {
    private val tres = n % 3 == 0
    private val m = if (tres) n / 3 else n
    init { require(m > 0 && m and (m - 1) == 0) { "tamaño $n: tiene que ser 2^m o 3·2^m" } }

    private val cosM = DoubleArray(m / 2) { cos(2 * PI * it / m) }
    private val senM = DoubleArray(m / 2) { -sin(2 * PI * it / m) }
    private val cosN = DoubleArray(n) { cos(2 * PI * it / n) }
    private val senN = DoubleArray(n) { -sin(2 * PI * it / n) }
    private val bits = IntArray(m).also { b ->
        var j = 0
        for (i in 1 until m) { var bit = m shr 1; while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }; j = j xor bit; b[i] = j }
    }
    private val r = Array(3) { DoubleArray(m) }
    private val i = Array(3) { DoubleArray(m) }

    /** La FFT de radix 2, en su sitio. */
    private fun radix2(re: DoubleArray, im: DoubleArray) {
        for (k in 0 until m) { val j = bits[k]; if (j > k) { var t = re[k]; re[k] = re[j]; re[j] = t; t = im[k]; im[k] = im[j]; im[j] = t } }
        var largo = 2
        while (largo <= m) {
            val mitad = largo / 2; val paso = m / largo
            var inicio = 0
            while (inicio < m) {
                var w = 0
                for (k in inicio until inicio + mitad) {
                    val c = cosM[w]; val s = senM[w]
                    val l = k + mitad
                    val tr = re[l] * c - im[l] * s; val ti = re[l] * s + im[l] * c
                    re[l] = re[k] - tr; im[l] = im[k] - ti
                    re[k] += tr; im[k] += ti
                    w += paso
                }
                inicio += largo
            }
            largo = largo shl 1
        }
    }

    /** X = FFT(x), complejo, en [re] e [im] (de tamaño [n], se pisan). */
    fun directa(re: DoubleArray, im: DoubleArray) {
        if (!tres) { radix2(re, im); return }
        for (q in 0 until 3) for (k in 0 until m) { r[q][k] = re[3 * k + q]; i[q][k] = im[3 * k + q] }
        for (q in 0 until 3) radix2(r[q], i[q])
        for (k in 0 until n) {
            val kk = k % m
            val k2 = (2 * k) % n
            re[k] = r[0][kk] + (r[1][kk] * cosN[k] - i[1][kk] * senN[k]) + (r[2][kk] * cosN[k2] - i[2][kk] * senN[k2])
            im[k] = i[0][kk] + (r[1][kk] * senN[k] + i[1][kk] * cosN[k]) + (r[2][kk] * senN[k2] + i[2][kk] * cosN[k2])
        }
    }

    /** La inversa (con el 1/n), en su sitio. */
    fun inversa(re: DoubleArray, im: DoubleArray) {
        for (k in 0 until n) im[k] = -im[k]
        directa(re, im)
        for (k in 0 until n) { re[k] /= n; im[k] = -im[k] / n }
    }
}

/**
 * **La STFT y su inversa** como `torch.stft`/`istft` con `center = true` (relleno por reflejo) y ventana de Hann
 * periódica: lo que esperan los modelos UVR MDX-Net (y lo que hace sherpa-onnx con kaldi-native-fbank).
 * Se comprobó contra sherpa-onnx con la misma canción: 76 dB de SNR, lo mismo (`docs/separar-voz.md`).
 */
class Stft(val nFft: Int, val hop: Int) {
    val bandas = nFft / 2 + 1
    private val fft = Fft(nFft)
    private val ventana = DoubleArray(nFft) { 0.5 - 0.5 * cos(2 * PI * it / nFft) }
    private val re = DoubleArray(nFft)
    private val im = DoubleArray(nFft)

    /** Cuántos marcos salen de [largo] muestras. */
    fun marcos(largo: Int): Int = 1 + largo / hop

    /**
     * La STFT de `x[desde, desde + largo)`: devuelve (re, im), cada uno `[marco * bandas + banda]`. Relleno por
     * reflejo de `nFft / 2` a cada lado.
     */
    fun directa(x: FloatArray, desde: Int, largo: Int): Pair<FloatArray, FloatArray> {
        val nm = marcos(largo); val mitad = nFft / 2
        val outRe = FloatArray(nm * bandas); val outIm = FloatArray(nm * bandas)
        for (f in 0 until nm) {
            for (k in 0 until nFft) {
                var p = f * hop + k - mitad
                if (p < 0) p = -p
                if (p >= largo) p = 2 * (largo - 1) - p
                re[k] = x[desde + p.coerceIn(0, largo - 1)] * ventana[k]; im[k] = 0.0
            }
            fft.directa(re, im)
            val o = f * bandas
            for (b in 0 until bandas) { outRe[o + b] = re[b].toFloat(); outIm[o + b] = im[b].toFloat() }
        }
        return outRe to outIm
    }

    /** La inversa: suma solapada con la ventana, normalizada por la suma de su cuadrado; [largo] muestras. */
    fun inversa(sRe: FloatArray, sIm: FloatArray, nm: Int, largo: Int): FloatArray {
        val total = nFft + hop * (nm - 1)
        val y = DoubleArray(total); val w = DoubleArray(total)
        for (f in 0 until nm) {
            val o = f * bandas
            for (b in 0 until bandas) { re[b] = sRe[o + b].toDouble(); im[b] = sIm[o + b].toDouble() }
            // El espectro de una señal real: la otra mitad, conjugada.
            for (b in bandas until nFft) { re[b] = re[nFft - b]; im[b] = -im[nFft - b] }
            im[0] = 0.0; if (nFft % 2 == 0) im[nFft / 2] = 0.0
            fft.inversa(re, im)
            val base = f * hop
            for (k in 0 until nFft) { y[base + k] += re[k] * ventana[k]; w[base + k] += ventana[k] * ventana[k] }
        }
        val mitad = nFft / 2
        return FloatArray(largo) { val p = it + mitad; if (p < total && w[p] > 1e-8) (y[p] / w[p]).toFloat() else 0f }
    }
}
