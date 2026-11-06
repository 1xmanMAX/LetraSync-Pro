package com.letrasyncpro.motor

import com.letrasyncpro.motor.LetraDeCancion.Letra
import com.letrasyncpro.motor.LetraDeCancion.Linea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** **Retocar tiempos, SRT, deshacer y la biblioteca** de LetraSync Pro (6-nov-2026). */
class EdicionDeLetraTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun letra(vararg l: Pair<Int?, String>) = Letra(l.map { Linea(it.first, it.second) })

    @Test
    fun ajustar_mueve_una_linea_por_decimas_y_nunca_por_debajo_de_cero() {
        val l = letra(1_000 to "a", 2_000 to "b", null to "c")
        assertEquals(1_100, EdicionDeLetra.ajustar(l, 0, 100).lineas[0].ms)
        assertEquals(0, EdicionDeLetra.ajustar(l, 0, -5_000).lineas[0].ms)
        assertNull(EdicionDeLetra.ajustar(l, 2, 500).lineas[2].ms)
        assertEquals(2_000, EdicionDeLetra.ajustar(l, 0, 100).lineas[1].ms)
    }

    @Test
    fun desplazar_mueve_todas_las_que_tienen_tiempo() {
        val l = EdicionDeLetra.desplazar(letra(1_000 to "a", null to "b", 3_000 to "c"), -250)
        assertEquals(listOf(750, null, 2_750), l.lineas.map { it.ms })
    }

    @Test
    fun las_desordenadas_son_las_que_van_antes_que_una_anterior() {
        assertEquals(setOf(2), EdicionDeLetra.desordenadas(letra(1_000 to "a", 5_000 to "b", 3_000 to "c", 6_000 to "d")))
        assertTrue(EdicionDeLetra.desordenadas(letra(1_000 to "a", null to "b", 2_000 to "c")).isEmpty())
    }

    @Test
    fun rellenar_reparte_las_sin_tiempo_entre_sus_vecinas_por_lo_largas() {
        val (r, n) = EdicionDeLetra.rellenar(letra(null to "intro", 0 to "aaaa", null to "bbbb", 8_000 to "cc", null to "fin"))
        assertEquals(1, n)
        assertEquals(listOf(null, 0, 4_000, 8_000, null), r.lineas.map { it.ms })
        val (r2, n2) = EdicionDeLetra.rellenar(letra(0 to "aa", null to "aaaa", null to "aa", 8_000 to "x"))
        assertEquals(2, n2)
        assertEquals(listOf(0, 2_000, 6_000, 8_000), r2.lineas.map { it.ms })
    }

    @Test
    fun el_srt_va_de_cada_linea_a_la_siguiente_y_la_ultima_dura_lo_suyo() {
        val srt = EdicionDeLetra.aSrt(letra(1_500 to "Bésame", null to "suelta", 62_345 to "mucho"), duracionMs = 64_000)
        assertEquals(
            "1\n00:00:01,500 --> 00:01:02,345\nBésame\n\n2\n00:01:02,345 --> 00:01:04,000\nmucho\n",
            srt
        )
    }

    @Test
    fun el_texto_de_trabajo_guarda_las_decimas_y_se_lee_igual() {
        val l = letra(1_230 to "a", 2_340 to "b")
        val t = EdicionDeLetra.texto(l)
        assertEquals(listOf(1_230, 2_340), LetraDeCancion.leer(t).lineas.map { it.ms })
        assertEquals("a\nb", EdicionDeLetra.texto(letra(null to "a", null to "b")))
    }

    @Test
    fun deshacer_y_rehacer() {
        val h = Historial()
        assertFalse(h.puedeDeshacer)
        h.apuntar("uno"); h.apuntar("dos")
        assertEquals("dos", h.deshacer("tres"))
        assertEquals("uno", h.deshacer("dos"))
        assertNull(h.deshacer("uno"))
        assertEquals("dos", h.rehacer("uno"))
        h.apuntar("dos"); h.apuntar("otro")
        assertFalse(h.puedeRehacer)
    }

    @Test
    fun la_biblioteca_guarda_lee_y_pone_al_dia_la_misma_cancion() {
        val b = Biblioteca(tmp.newFolder())
        b.guardar("content://a/1", "Bésame.mp3", "[00:01.00]Bésame", cuando = 10)
        b.guardar("content://a/2", "Otra.mp3", "sin tiempos", cuando = 20)
        b.guardar("content://a/1", "Bésame.mp3", "[00:02.00]Bésame", cuando = 30)
        val t = b.todas()
        assertEquals(listOf("Bésame.mp3", "Otra.mp3"), t.map { it.nombre })
        assertEquals("[00:02.00]Bésame", t[0].letra)
        assertTrue(t[0].conTiempos)
        assertEquals("content://a/2", b.de("content://a/2")?.audio)
        b.borrar(t[1].id)
        assertEquals(1, b.todas().size)
    }

    @Test
    fun una_cancion_se_escribe_y_se_lee_igual() {
        val c = Biblioteca.Cancion("x", "content://a", "N", "#no es cabecera\n[00:01.00]a", 5)
        assertEquals(c, Biblioteca.leer("x", Biblioteca.escribir(c)))
    }
}
