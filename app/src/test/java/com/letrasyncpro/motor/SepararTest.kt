package com.letrasyncpro.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** **Separar la voz** (6-nov-2026): la FFT, la STFT y el troceado de UVR, con modelos de mentira. */
class SepararTest {

    private fun dft(re: DoubleArray, im: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val n = re.size
        val r = DoubleArray(n); val i = DoubleArray(n)
        for (k in 0 until n) for (t in 0 until n) {
            val a = -2 * PI * k * t / n
            r[k] += re[t] * cos(a) - im[t] * sin(a); i[k] += re[t] * sin(a) + im[t] * cos(a)
        }
        return r to i
    }

    @Test
    fun la_fft_de_2_a_la_m_y_de_3_por_2_a_la_m_da_lo_mismo_que_la_dft() {
        for (n in listOf(8, 16, 24, 48, 96)) {
            val rnd = Random(n)
            val re = DoubleArray(n) { rnd.nextDouble(-1.0, 1.0) }; val im = DoubleArray(n) { rnd.nextDouble(-1.0, 1.0) }
            val (er, ei) = dft(re, im)
            val r = re.copyOf(); val i = im.copyOf()
            Fft(n).directa(r, i)
            for (k in 0 until n) { assertEquals("n=$n k=$k", er[k], r[k], 1e-9); assertEquals(ei[k], i[k], 1e-9) }
            Fft(n).inversa(r, i)
            for (k in 0 until n) assertEquals(re[k], r[k], 1e-9)
        }
    }

    @Test
    fun la_stft_y_su_inversa_devuelven_la_senal() {
        val s = Stft(24, 4)
        val x = FloatArray(200) { sin(it * 0.3f) + 0.5f * cos(it * 1.1f) }
        val (re, im) = s.directa(x, 0, x.size)
        val y = s.inversa(re, im, s.marcos(x.size), x.size)
        for (k in x.indices) assertEquals("k=$k", x[k], y[k], 1e-4f)
        // Un seno puro cae en su banda.
        val t = Stft(48, 12)
        val sen = FloatArray(480) { sin(2 * PI * 6 * it / 48).toFloat() }
        val (r2, i2) = t.directa(sen, 0, sen.size)
        val f = 10 * t.bandas
        val mag = (0 until t.bandas).map { kotlin.math.hypot(r2[f + it], i2[f + it]) }
        assertEquals(6, mag.indices.maxBy { mag[it] })
    }

    private fun cancion(n: Int) = Array(2) { ch -> FloatArray(n) { (0.4 * sin(2 * PI * (220 + 110 * ch) * it / 44_100) + 0.2 * sin(2 * PI * 3_000 * it / 44_100)).toFloat() } }

    private fun separar(sep: SeparacionUvr, x: Array<FloatArray>, fino: Boolean = false): Pair<Array<FloatArray>, Array<FloatArray>> {
        val n = x[0].size
        val voz = Array(2) { FloatArray(n) }; val ins = Array(2) { FloatArray(n) }
        var escrito = 0
        assertTrue(sep.separar(n, { desde, c0, c1 ->
            for (k in c0.indices) { val p = desde + k; c0[k] = if (p in 0 until n) x[0][p] else 0f; c1[k] = if (p in 0 until n) x[1][p] else 0f }
        }, { v0, v1, i0, i1, cuantas ->
            for (k in 0 until cuantas) { voz[0][escrito + k] = v0[k]; voz[1][escrito + k] = v1[k]; ins[0][escrito + k] = i0[k]; ins[1][escrito + k] = i1[k] }
            escrito += cuantas
        }, fino = fino))
        assertEquals(n, escrito)
        return voz to ins
    }

    @Test
    fun con_un_modelo_que_lo_devuelve_todo_la_voz_es_la_cancion_y_el_karaoke_silencio() {
        // Los números del modelo de verdad (UVR-MDX-NET-Voc_FT) y una canción de algo más de dos tramos.
        val sep = SeparacionUvr(6144, 1024, 3072, 256, 44_100) { d, _ -> d }
        val x = cancion(sep.util * 2 + 12_345)
        val (voz, ins) = separar(sep, x)
        var maxErr = 0f
        for (ch in 0..1) for (k in x[0].indices) maxErr = maxOf(maxErr, abs(voz[ch][k] - x[ch][k]), abs(ins[ch][k]))
        assertTrue("error máximo $maxErr", maxErr < 1e-3f)
    }

    @Test
    fun con_un_modelo_mudo_todo_es_karaoke_y_fino_da_lo_mismo_con_un_modelo_lineal() {
        val mudo = SeparacionUvr(48, 8, 25, 21, 44_100) { d, _ -> FloatArray(d.size) }
        val x = cancion(1_000)
        val (voz, ins) = separar(mudo, x)
        for (ch in 0..1) for (k in x[0].indices) { assertEquals(0f, voz[ch][k], 1e-6f); assertEquals(x[ch][k], ins[ch][k], 1e-6f) }
        // Un modelo lineal (la mitad de todo): f(x) y −f(−x) coinciden, así que «fino» no cambia nada.
        val mitad = SeparacionUvr(48, 8, 25, 21, 44_100) { d, _ -> FloatArray(d.size) { d[it] / 2 } }
        val (a, _) = separar(mitad, x); val (b, _) = separar(mitad, x, fino = true)
        for (ch in 0..1) for (k in x[0].indices) { assertEquals(a[ch][k], b[ch][k], 1e-5f); assertEquals(x[ch][k] / 2, a[ch][k], 1e-3f) }
    }

    @Test
    fun se_para_si_se_cancela() {
        val sep = SeparacionUvr(48, 8, 25, 21, 44_100) { d, _ -> d }
        assertFalse(sep.separar(10_000, { _, _, _ -> }, { _, _, _, _, _ -> }, cancelado = AtomicBoolean(true)))
    }

    @Test
    fun la_cabecera_del_wav() {
        val c = Wav.cabecera(400, 44_100)
        assertEquals("RIFF", String(c, 0, 4)); assertEquals("WAVE", String(c, 8, 4)); assertEquals("data", String(c, 36, 4))
        assertEquals(44, c.size)
    }
}
