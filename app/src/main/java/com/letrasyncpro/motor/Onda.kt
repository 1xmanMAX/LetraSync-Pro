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
    /** Un punto cada 5 ms: al estirar la onda se ve dónde arranca cada sílaba. */
    const val MS_POR_PUNTO = 5
    /** Los arranques se buscan con puntos de 20 ms (más finos, cualquier golpe parecería uno). */
    private const val MS_ARRANQUES = 20

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
    fun inicios(fina: FloatArray, subida: Float = 0.18f, factor: Float = 1.8f): List<Int> {
        val e = reducir(fina, MS_ARRANQUES / MS_POR_PUNTO)
        val r = ArrayList<Int>()
        val ventana = 200 / MS_ARRANQUES
        var ultimo = -10_000
        for (i in ventana until e.size) {
            var s = 0f
            for (k in i - ventana until i) s += e[k]
            val antes = s / ventana
            if (e[i] - antes >= subida && e[i] >= antes * factor && e[i] > e[i - 1]) {
                // Dentro del punto de 20 ms, el primer punto fino que ya sube: el arranque, al ms.
                val ms = primeraSubida(fina, i * MS_ARRANQUES / MS_POR_PUNTO, MS_ARRANQUES / MS_POR_PUNTO, antes) * MS_POR_PUNTO
                if (ms - ultimo >= 150) { r += ms; ultimo = ms }
            }
        }
        return r
    }

    /**
     * **Las barras de la onda, fijas al tiempo** (7-nov-2026: «las ondas vibran»): cada barra cubre siempre el mismo
     * trozo de canción (`ms` múltiplo de su ancho), así al moverse la onda las barras se desplazan sin cambiar de
     * alto. El ancho de barra (en ms) es una potencia de dos de [MS_POR_PUNTO] que da unos [pxPorBarra] píxeles,
     * y solo cambia al acercar o alejar. Devuelve (ms de la barra, alto de 0 a 1) de las que se ven.
     */
    fun barras(e: FloatArray, inicioMs: Float, ventanaMs: Float, anchoPx: Float, pxPorBarra: Float = 7f): Pair<Int, List<Pair<Int, Float>>> {
        val quiere = ventanaMs / anchoPx * pxPorBarra
        var ancho = MS_POR_PUNTO
        while (ancho * 2 <= quiere) ancho *= 2
        val puntos = ancho / MS_POR_PUNTO
        val primera = (maxOf(0f, inicioMs) / ancho).toInt()
        val ultima = ((inicioMs + ventanaMs) / ancho).toInt() + 1
        val r = ArrayList<Pair<Int, Float>>()
        for (b in primera..ultima) {
            val desde = b * puntos
            if (desde >= e.size) break
            var m = 0f
            for (k in desde until minOf(e.size, desde + puntos)) if (e[k] > m) m = e[k]
            r += (b * ancho) to m
        }
        return ancho to r
    }

    /** Una envolvente más gruesa: el máximo de cada [n] puntos. */
    fun reducir(e: FloatArray, n: Int): FloatArray =
        if (n <= 1) e else FloatArray((e.size + n - 1) / n) { k -> var m = 0f; for (j in k * n until minOf(e.size, k * n + n)) if (e[j] > m) m = e[j]; m }

    /** El primer punto fino desde [desde] (de [cuantos]) que pasa de lo que sonaba [antes]: donde arranca. */
    private fun primeraSubida(fina: FloatArray, desde: Int, cuantos: Int, antes: Float): Int {
        val umbral = antes + (0.5f * (fina.getOrElse(desde + cuantos - 1) { antes } - antes)).coerceAtLeast(0.02f)
        for (k in desde until minOf(fina.size, desde + cuantos)) if (fina[k] >= umbral) return k
        return desde
    }

    /** El inicio más cercano a [ms] a menos de [margenMs]; si no hay, [ms] tal cual. */
    fun pegar(ms: Int, inicios: List<Int>, margenMs: Int = 300): Int =
        inicios.minByOrNull { kotlin.math.abs(it - ms) }?.takeIf { kotlin.math.abs(it - ms) <= margenMs } ?: ms

    /**
     * **Dónde parece empezar la siguiente frase**: el primer arranque de sonido después de [despuesDe] más
     * [huecoMs] (lo que dura al menos una frase). Null si no hay ninguno: entonces se marca a mano.
     */
    fun sugerencia(inicios: List<Int>, despuesDe: Int, huecoMs: Int = 600): Int? = inicios.firstOrNull { it >= despuesDe + huecoMs }

    /** Guardada en caché (un float por punto) para no decodificar cada vez. */
    fun guardar(f: File, e: FloatArray) {
        f.parentFile?.mkdirs()
        java.io.DataOutputStream(f.outputStream().buffered()).use { o -> o.writeInt(e.size); e.forEach { o.writeFloat(it) } }
    }

    fun leer(f: File): FloatArray? = runCatching {
        java.io.DataInputStream(f.inputStream().buffered()).use { i -> FloatArray(i.readInt()) { i.readFloat() } }
    }.getOrNull()
}
