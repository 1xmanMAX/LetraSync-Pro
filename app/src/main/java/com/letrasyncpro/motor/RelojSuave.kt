package com.letrasyncpro.motor

import kotlin.math.abs

/**
 * **La posición de la canción, sin tirones** (7-nov-2026: «la onda tiembla, como que la jala para atrás y
 * vuelve»). El reproductor de Android da la posición a saltos irregulares (y a veces un poco para atrás); si la
 * onda la sigue tal cual, vibra. Aquí la posición avanza sola con el reloj (a la velocidad que suene) y solo se
 * corrige poco a poco hacia la del reproductor; un salto grande (ir a otro sitio) se coge de golpe. Nunca va
 * hacia atrás mientras suena.
 */
class RelojSuave(private val salto: Int = 250) {
    private var ms = 0.0
    private var antes = -1L
    /** Hasta cuándo no se hace caso al reproductor tras ir a otro sitio (al principio aún dice el sitio viejo). */
    private var sordoHasta = -1L

    /** La posición estimada en [ahoraMs] (reloj del sistema) con lo que dice el reproductor ([real]) y su [velocidad]. */
    fun ahora(real: Int, ahoraMs: Long, velocidad: Float, sonando: Boolean): Int {
        if (!sonando || (antes < 0 && sordoHasta < 0)) { ms = real.toDouble(); antes = ahoraMs; return real }
        if (antes < 0) { antes = ahoraMs; sordoHasta = ahoraMs + 300 }
        val previsto = ms + (ahoraMs - antes) * velocidad
        antes = ahoraMs
        if (ahoraMs < sordoHasta) { ms = previsto; return ms.toInt() }
        val error = real - previsto
        ms = if (abs(error) > salto) real.toDouble() else maxOf(ms, previsto + error * 0.08)
        return ms.toInt()
    }

    /** Tras ir a otro sitio: empieza desde ahí (y no cree al reproductor unos 300 ms, que aún da el sitio viejo). */
    fun reiniciar(real: Int) { ms = real.toDouble(); antes = -1; sordoHasta = 0 }
}
