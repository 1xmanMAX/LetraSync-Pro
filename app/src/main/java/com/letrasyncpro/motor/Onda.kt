package com.letrasyncpro.motor

import java.io.File
import java.io.InputStream
import kotlin.math.sqrt

/**
 * **La onda de la canción** (6-nov-2026, como la línea de tiempo de Composer o wavelyric): lo fuerte que suena
 * cada [MS_POR_PUNTO] ms, de 0 a 1, para dibujarla y ver dónde empieza la voz. Y **dónde arranca un sonido**
 * ([inicios]), para que una marca arrastrada se pegue a él. Puro sobre el PCM (16 bits, mono,
 * [Transcriptor.HERCIOS]).
 */
object Onda {
    const val MS_POR_PUNTO = 20

    /** La envolvente de un PCM de 16 bits little-endian, mono. */
    fun envolvente(pcm: InputStream, hercios: Int = Transcriptor.HERCIOS): FloatArray {
        val muestrasPorPunto = hercios * MS_POR_PUNTO / 1000
        val salida = ArrayList<Float>()
        val buf = ByteArray(muestrasPorPunto * 2 * 64)
        var suma = 0.0; var n = 0
        var resto = -1
        while (true) {
            val leidos = pcm.read(buf)
            if (leidos <= 0) break
            var k = 0
            if (resto >= 0 && leidos > 0) {
                val v = ((buf[0].toInt() shl 8) or resto).toShort().toDouble()
                suma += v * v; n++; k = 1; resto = -1
                if (n == muestrasPorPunto) { salida += sqrt(suma / n).toFloat(); suma = 0.0; n = 0 }
            }
            while (k + 1 < leidos) {
                val v = ((buf[k + 1].toInt() shl 8) or (buf[k].toInt() and 0xFF)).toShort().toDouble()
                suma += v * v; n++
                if (n == muestrasPorPunto) { salida += sqrt(suma / n).toFloat(); suma = 0.0; n = 0 }
                k += 2
            }
            if (k < leidos) resto = buf[k].toInt() and 0xFF
        }
        if (n > 0) salida += sqrt(suma / n).toFloat()
        return normalizar(salida.toFloatArray())
    }

    /** De 0 a 1 respecto al percentil 99 (un golpe suelto no aplasta el resto). */
    fun normalizar(e: FloatArray): FloatArray {
        if (e.isEmpty()) return e
        val tope = e.sorted()[((e.size - 1) * 0.99).toInt()].takeIf { it > 0f } ?: return FloatArray(e.size)
        return FloatArray(e.size) { (e[it] / tope).coerceIn(0f, 1f) }
    }

    /**
     * **Dónde arranca un sonido**: un punto que sube claramente sobre lo que sonaba justo antes (la media de
     * los 200 ms previos), con un hueco mínimo de 150 ms entre uno y otro. En ms.
     */
    fun inicios(e: FloatArray, subida: Float = 0.18f, factor: Float = 1.8f): List<Int> {
        val r = ArrayList<Int>()
        val ventana = 200 / MS_POR_PUNTO
        var ultimo = -10_000
        for (i in ventana until e.size) {
            var s = 0f
            for (k in i - ventana until i) s += e[k]
            val antes = s / ventana
            if (e[i] - antes >= subida && e[i] >= antes * factor && e[i] > e[i - 1]) {
                val ms = i * MS_POR_PUNTO
                if (ms - ultimo >= 150) { r += ms; ultimo = ms }
            }
        }
        return r
    }

    /** El inicio más cercano a [ms] a menos de [margenMs]; si no hay, [ms] tal cual. */
    fun pegar(ms: Int, inicios: List<Int>, margenMs: Int = 300): Int =
        inicios.minByOrNull { kotlin.math.abs(it - ms) }?.takeIf { kotlin.math.abs(it - ms) <= margenMs } ?: ms

    /** Guardada en caché (un float por punto) para no decodificar cada vez. */
    fun guardar(f: File, e: FloatArray) {
        f.parentFile?.mkdirs()
        java.io.DataOutputStream(f.outputStream().buffered()).use { o -> o.writeInt(e.size); e.forEach { o.writeFloat(it) } }
    }

    fun leer(f: File): FloatArray? = runCatching {
        java.io.DataInputStream(f.inputStream().buffered()).use { i -> FloatArray(i.readInt()) { i.readFloat() } }
    }.getOrNull()
}
