package com.letrasyncpro.motor

import com.letrasyncpro.motor.LetraDeCancion.Letra
import com.letrasyncpro.motor.LetraDeCancion.Linea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** **Lo tomado de otras apps de LRC** (6-nov-2026): por palabras, la onda, LRCLib, SRT y mover por lotes. */
class ProTest {

    @Test
    fun a_toques_por_palabras_pone_cada_palabra_y_la_linea_con_la_primera() {
        val l = Letra(listOf(Linea(null, "Hola mundo"), Linea(null, "adiós")))
        val s = SincronizadorPorPalabras(l, retardoMs = 100)
        assertEquals(listOf(0 to 0, 0 to 1, 1 to 0), s.orden)
        s.marcar(1_100); s.marcar(1_600)
        assertEquals(1 to 0, s.actual)
        assertEquals(0, s.deshacer()) // vuelve a oír desde 2 s antes de la anterior
        s.marcar(1_700); s.marcar(3_100)
        assertTrue(s.terminado)
        val r = s.letra()
        assertEquals(Linea(1_000, "Hola mundo", listOf(1_000, 1_600)), r.lineas[0])
        assertEquals(Linea(3_000, "adiós", listOf(3_000)), r.lineas[1])
        assertEquals("[00:01.00]<00:01.00>Hola <00:01.60>mundo\n[00:03.00]<00:03.00>adiós", LetraDeCancion.aLrc(r))
    }

    @Test
    fun mover_una_linea_mueve_sus_palabras_y_mover_desde_aqui_mueve_las_de_detras() {
        val l = Letra(listOf(Linea(1_000, "a b", listOf(1_000, 1_500)), Linea(2_000, "c"), Linea(3_000, "d")))
        assertEquals(listOf(1_200, 1_700), EdicionDeLetra.ajustar(l, 0, 200).lineas[0].palabras)
        assertEquals(listOf(1_000, 2_500, 3_500), EdicionDeLetra.ajustarDesde(l, 1, 500).lineas.map { it.ms })
        assertEquals(listOf(4_000, 4_500), EdicionDeLetra.poner(l, 0, 4_000).lineas[0].palabras)
        assertNull(EdicionDeLetra.quitar(l, 0).lineas[0].palabras)
    }

    @Test
    fun mover_una_linea_nunca_mueve_otra_ni_la_cruza() {
        val l = Letra(listOf(Linea(1_000, "a"), Linea(2_000, "b"), Linea(3_000, "c")))
        // Pasarse de la siguiente: se queda justo antes de ella; las otras, igual.
        assertEquals(listOf(1_000, 2_900, 3_000), EdicionDeLetra.ajustar(l, 1, 5_000).lineas.map { it.ms })
        assertEquals(listOf(1_000, 1_100, 3_000), EdicionDeLetra.llevar(l, 1, 0).lineas.map { it.ms })
        assertEquals(1_100..2_900, EdicionDeLetra.limites(l, 1))
        // Poner «aquí» más allá de la siguiente no la reordena: se queda en su sitio (y se ve en rojo).
        val r = LetraDeCancion.leer(EdicionDeLetra.texto(EdicionDeLetra.poner(l, 0, 2_500)))
        assertEquals(listOf("a", "b", "c"), r.lineas.map { it.texto })
        assertEquals(setOf(1), EdicionDeLetra.desordenadas(r))
    }

    @Test
    fun marcar_en_la_onda_guarda_la_voz_con_su_anticipo_y_sugiere_la_siguiente() {
        val l = Letra(listOf(Linea(null, "a"), Linea(null, "b"), Linea(null, "c")))
        val r = EdicionDeLetra.marcarVoz(EdicionDeLetra.marcarVoz(l, 0, 1_000, 300), 1, 900, 300)
        // La primera aparece 0,3 s antes de cantarse; la segunda, marcada antes que la primera, no la cruza.
        assertEquals(listOf(700, 800, null), r.lineas.map { it.ms })
        assertEquals(0, EdicionDeLetra.alAparecer(100, 300))
        assertEquals(2_000, Onda.sugerencia(listOf(500, 1_200, 2_000), despuesDe = 1_000))
        assertNull(Onda.sugerencia(listOf(500), despuesDe = 1_000))
    }

    @Test
    fun marcar_la_primera_no_pega_las_demas_a_ella() {
        val l = LetraDeCancion.leer("uno\ndos\ntres")
        val r = LetraDeCancion.leer(EdicionDeLetra.texto(EdicionDeLetra.marcarVoz(l, 0, 1_000, 300)))
        assertEquals(listOf("uno", "dos", "tres"), r.lineas.map { it.texto })
        assertEquals(listOf(700, null, null), r.lineas.map { it.ms })
        // Y con los datos de la canción y palabras, vuelve igual.
        val c = Letra(listOf(Linea(1_000, "a b", listOf(1_000, 1_500)), Linea(null, "c")), titulo = "T", artista = "A")
        assertEquals(c, LetraDeCancion.leer(EdicionDeLetra.texto(c)))
    }

    @Test
    fun la_palabra_que_suena() {
        val l = Linea(1_000, "a b c", listOf(null, 1_500, 2_000))
        assertEquals(-1, EdicionDeLetra.palabraActual(l, 900))
        assertEquals(0, EdicionDeLetra.palabraActual(l, 1_200))
        assertEquals(2, EdicionDeLetra.palabraActual(l, 5_000))
        assertEquals(-1, EdicionDeLetra.palabraActual(Linea(0, "x"), 100))
    }

    @Test
    fun un_srt_se_lee_como_letra() {
        val l = EdicionDeLetra.desdeSrt("1\r\n00:00:01,500 --> 00:00:03,000\r\n<i>Bésame</i>\r\nmucho\r\n\r\n2\r\n00:01:02,345 --> 00:01:04,000\r\notra\r\n")!!
        assertEquals(listOf(Linea(1_500, "Bésame mucho"), Linea(62_345, "otra")), l.lineas)
        assertNull(EdicionDeLetra.desdeSrt("solo texto\nsin tiempos"))
        // Y vuelve igual por SRT.
        assertEquals(l.lineas, EdicionDeLetra.desdeSrt(EdicionDeLetra.aSrt(l))!!.lineas)
    }

    private fun pcm(vararg tramos: Pair<Int, Short>): ByteArrayInputStream {
        val n = tramos.sumOf { it.first } * Transcriptor.HERCIOS / 1000
        val b = ByteBuffer.allocate(n * 2).order(ByteOrder.LITTLE_ENDIAN)
        tramos.forEach { (ms, v) -> repeat(ms * Transcriptor.HERCIOS / 1000) { b.putShort(if (it % 2 == 0) v else (-v).toShort()) } }
        return ByteArrayInputStream(b.array())
    }

    @Test
    fun la_onda_y_donde_arranca_la_voz() {
        val e = Onda.envolvente(pcm(1_000 to 50.toShort(), 1_000 to 8_000.toShort(), 500 to 50.toShort(), 500 to 8_000.toShort()))
        assertEquals(150, e.size)
        assertTrue(e[10] < 0.05f); assertTrue(e[60] > 0.9f)
        val ini = Onda.inicios(e)
        assertEquals(listOf(1_000, 2_500), ini)
        assertEquals(1_000, Onda.pegar(1_180, ini))
        assertEquals(1_800, Onda.pegar(1_800, ini))
    }

    @Test
    fun la_onda_se_lee_aunque_lleguen_bytes_sueltos() {
        val todo = pcm(100 to 1_000.toShort()).readBytes()
        val troceado = object : java.io.InputStream() {
            var i = 0
            override fun read(): Int = if (i < todo.size) todo[i++].toInt() and 0xFF else -1
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (i >= todo.size) return -1
                val n = minOf(3, len, todo.size - i); System.arraycopy(todo, i, b, off, n); i += n; return n
            }
        }
        val a = Onda.envolvente(ByteArrayInputStream(todo)); val b = Onda.envolvente(troceado)
        assertEquals(a.toList(), b.toList())
    }

    @Test
    fun lrclib_lee_ordena_y_saca_titulo_y_artista_del_nombre() {
        val json = """[
          {"id":1,"trackName":"Bésame mucho","artistName":"X","albumName":null,"duration":200.0,"instrumental":false,"plainLyrics":"a","syncedLyrics":null},
          {"id":2,"trackName":"Bésame mucho","artistName":"X","albumName":"D","duration":240.0,"instrumental":false,"plainLyrics":"a","syncedLyrics":"[00:01.00]a"},
          {"id":3,"trackName":"Bésame mucho","artistName":"X","albumName":"D","duration":180.0,"instrumental":false,"plainLyrics":"a","syncedLyrics":"[00:01.00]a"}
        ]"""
        val r = LrcLib.leer(json)
        assertEquals(listOf(2L, 3L, 1L), LrcLib.ordenar(r, 239_000).map { it.id })
        assertNull(r[0].album)
        assertFalse(r[0].instrumental)
        assertEquals("Bésame mucho" to "Luis Miguel", LrcLib.deNombre("Luis Miguel - Bésame mucho.mp3"))
        assertEquals("cancion 1" to "", LrcLib.deNombre("cancion_1.m4a"))
        assertTrue(LrcLib.url("Bésame mucho", "X").contains("track_name=B%C3%A9same+mucho&artist_name=X"))
    }
}
