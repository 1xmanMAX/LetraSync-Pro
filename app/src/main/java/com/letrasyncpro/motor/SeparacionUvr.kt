package com.letrasyncpro.motor

import java.util.concurrent.atomic.AtomicBoolean

/**
 * **Separar la voz con un modelo UVR MDX-Net** (6-nov-2026; lo que hacen Ultimate Vocal Remover y sherpa-onnx,
 * `offline-source-separation-uvr-impl.h`). Puro: el modelo entra como una función (el de verdad va por
 * [Separador], en el teléfono), así se prueba en el ordenador.
 *
 * Cómo va, igual que sherpa-onnx (comprobado: 76 dB de SNR contra su salida con la misma canción):
 * - La canción (estéreo, a [hercios]) se parte en tramos de `hop · (dimT − 1)` muestras que se solapan
 *   `nFft / 2` a cada lado (de cada tramo vale el centro, `gen` muestras).
 * - De cada canal, la STFT compleja ([Stft], Hann, `center`); las `dimF` primeras bandas, en un tensor
 *   `[1, 4, dimF, dimT]` (real e imaginaria de cada canal). El modelo devuelve el espectro **de la voz**.
 * - La voz vuelve con la STFT inversa; **el instrumental es la canción menos la voz** (así suman exacto).
 * - **Fino** (como el «denoise» de UVR): también se pasa la canción cambiada de signo y la voz es la media de
 *   las dos (`(f(x) − f(−x)) / 2`): el ruido que mete el modelo se va. Tarda el doble.
 */
class SeparacionUvr(
    val nFft: Int, val hop: Int, val dimF: Int, val dimT: Int, val hercios: Int,
    private val modelo: (datos: FloatArray, forma: LongArray) -> FloatArray
) {
    val recorte = nFft / 2
    val tramo = hop * (dimT - 1)
    val util = tramo - 2 * recorte
    private val stft = Stft(nFft, hop)
    init { require(util > 0) { "tramo demasiado corto" }; require(stft.marcos(tramo) == dimT) }

    /** Cuántos tramos lleva una canción de [n] muestras (para el avance). */
    fun tramos(n: Int): Int = (n + (util - n % util)) / util

    /** La voz del centro de un tramo ([util] muestras por canal), de los dos canales de [tramo] muestras. */
    fun voz(c0: FloatArray, c1: FloatArray, signo: Float = 1f): Pair<FloatArray, FloatArray> {
        val e0 = if (signo == 1f) c0 else FloatArray(c0.size) { -c0[it] }
        val e1 = if (signo == 1f) c1 else FloatArray(c1.size) { -c1[it] }
        val (r0, i0) = stft.directa(e0, 0, tramo)
        val (r1, i1) = stft.directa(e1, 0, tramo)
        val b = stft.bandas
        val x = FloatArray(4 * dimF * dimT)
        for (f in 0 until dimF) for (t in 0 until dimT) {
            x[(0 * dimF + f) * dimT + t] = r0[t * b + f]; x[(1 * dimF + f) * dimT + t] = i0[t * b + f]
            x[(2 * dimF + f) * dimT + t] = r1[t * b + f]; x[(3 * dimF + f) * dimT + t] = i1[t * b + f]
        }
        val y = modelo(x, longArrayOf(1, 4, dimF.toLong(), dimT.toLong()))
        // El espectro de la voz: las bandas del modelo y cero por encima.
        val sr0 = FloatArray(dimT * b); val si0 = FloatArray(dimT * b); val sr1 = FloatArray(dimT * b); val si1 = FloatArray(dimT * b)
        for (f in 0 until dimF) for (t in 0 until dimT) {
            sr0[t * b + f] = y[(0 * dimF + f) * dimT + t]; si0[t * b + f] = y[(1 * dimF + f) * dimT + t]
            sr1[t * b + f] = y[(2 * dimF + f) * dimT + t]; si1[t * b + f] = y[(3 * dimF + f) * dimT + t]
        }
        val v0 = stft.inversa(sr0, si0, dimT, tramo); val v1 = stft.inversa(sr1, si1, dimT, tramo)
        val s = if (signo == 1f) 1f else -1f
        return FloatArray(util) { v0[recorte + it] * s } to FloatArray(util) { v1[recorte + it] * s }
    }

    /**
     * **Separa una canción de [n] muestras por canal.** [leer] llena `c0`/`c1` con las muestras desde `desde`
     * (puede ser negativo o pasar de `n`: ahí, ceros). [escribir] recibe, en orden, la voz de cada trozo y el
     * instrumental (canción − voz). [avance] de 0 a 1; [cancelado] lo para entre tramo y tramo (devuelve false).
     */
    fun separar(
        n: Int, leer: (desde: Int, c0: FloatArray, c1: FloatArray) -> Unit,
        escribir: (voz0: FloatArray, voz1: FloatArray, ins0: FloatArray, ins1: FloatArray, cuantas: Int) -> Unit,
        fino: Boolean = false, avance: (Float) -> Unit = {}, cancelado: AtomicBoolean = AtomicBoolean(false)
    ): Boolean {
        val c0 = FloatArray(tramo); val c1 = FloatArray(tramo)
        val total = tramos(n)
        var hecho = 0
        var i = 0
        while (i < n) {
            if (cancelado.get()) return false
            leer(i - recorte, c0, c1)
            var (v0, v1) = voz(c0, c1)
            if (fino) {
                val (w0, w1) = voz(c0, c1, -1f)
                v0 = FloatArray(util) { (v0[it] + w0[it]) / 2 }; v1 = FloatArray(util) { (v1[it] + w1[it]) / 2 }
            }
            val cuantas = minOf(util, n - i)
            val ins0 = FloatArray(cuantas) { c0[recorte + it] - v0[it] }
            val ins1 = FloatArray(cuantas) { c1[recorte + it] - v1[it] }
            escribir(v0, v1, ins0, ins1, cuantas)
            i += util; hecho++
            avance(hecho.toFloat() / total)
        }
        return true
    }
}
