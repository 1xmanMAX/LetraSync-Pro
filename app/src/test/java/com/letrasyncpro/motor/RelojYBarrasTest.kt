package com.letrasyncpro.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** La onda sin temblar (7-nov-2026): el reloj suave y las barras fijas al tiempo. */
class RelojYBarrasTest {

    @Test
    fun el_reloj_avanza_liso_aunque_el_reproductor_de_saltos_y_vaya_para_atras() {
        val r = RelojSuave()
        r.ahora(1_000, 0, 1f, true)
        var prev = 1_000
        // El reproductor: a saltos de 0 o 100 ms, y a veces 30 ms para atrás.
        val reales = listOf(1_000, 1_100, 1_100, 1_070, 1_200, 1_200, 1_330, 1_300, 1_400, 1_500)
        reales.forEachIndexed { i, real ->
            val t = r.ahora(real, (i + 1) * 50L, 1f, true)
            assertTrue("nunca para atrás ($prev → $t)", t >= prev)
            assertTrue("cerca del reloj (t=$t)", kotlin.math.abs(t - (1_000 + (i + 1) * 50)) < 120)
            prev = t
        }
        // Un salto grande (ir a otro sitio) se coge de golpe.
        assertEquals(30_000, r.ahora(30_000, 600, 1f, true))
        // Tras ir a otro sitio, no hace caso al reproductor (que aún da el sitio viejo) y avanza con el reloj,
        // a media velocidad la mitad.
        r.reiniciar(10_000)
        assertEquals(10_000, r.ahora(29_000, 1_000, 0.5f, true))
        assertEquals(10_050, r.ahora(29_000, 1_100, 0.5f, true))
    }

    @Test
    fun la_barra_se_arrastra_desde_donde_iba_como_en_tiktok() {
        // Canción de 200 s en una barra de 1000 px, yendo por el 50 s: 100 px a la derecha son 20 s más.
        assertEquals(70_000, com.letrasyncpro.ui.arrastreRelativo(50_000, 100f, 1_000f, 200_000))
        assertEquals(30_000, com.letrasyncpro.ui.arrastreRelativo(50_000, -100f, 1_000f, 200_000))
        // Nunca fuera de la canción.
        assertEquals(200_000, com.letrasyncpro.ui.arrastreRelativo(190_000, 500f, 1_000f, 200_000))
        assertEquals(0, com.letrasyncpro.ui.arrastreRelativo(5_000, -500f, 1_000f, 200_000))
    }

    @Test
    fun las_barras_cubren_siempre_el_mismo_trozo_al_moverse() {
        val e = FloatArray(4_000) { (it % 37) / 37f } // 20 s a 5 ms
        val (ancho, a) = Onda.barras(e, 1_000f, 4_000f, 1_000f)
        // 4 s en 1000 px a 7 px por barra: 28 ms → 20 ms (potencia de dos de 5 ms).
        assertEquals(20, ancho)
        val (_, b) = Onda.barras(e, 1_013f, 4_000f, 1_000f) // la onda movida 13 ms
        val altosA = a.toMap(); val altosB = b.toMap()
        for ((ms, h) in altosB) altosA[ms]?.let { assertEquals("la barra de $ms ms no cambia de alto", it, h, 0f) }
        assertTrue(b.first().first <= 1_013 && b.last().first >= 5_000)
    }
}
