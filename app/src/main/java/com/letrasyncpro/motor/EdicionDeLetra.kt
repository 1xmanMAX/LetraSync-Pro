package com.letrasyncpro.motor

import com.letrasyncpro.motor.LetraDeCancion.Letra
import com.letrasyncpro.motor.LetraDeCancion.Linea
import java.util.Locale

/**
 * **Retocar los tiempos de una letra** (6-nov-2026), lo que hace una app profesional de LRC:
 * mover una línea por décimas, mover toda la letra, rellenar las que quedaron sin tiempo, ver las que van
 * desordenadas y sacar subtítulos SRT. Puro: la pantalla le da la letra y le pide la nueva.
 */
object EdicionDeLetra {

    /** Lo que dura como mucho una línea en el SRT cuando no hay otra detrás. */
    const val DURACION_DE_LA_ULTIMA = 4_000

    /** Mueve la línea [i] [deltaMs] (negativo: antes). Sin tiempo, no hace nada. Nunca por debajo de 0. */
    fun ajustar(letra: Letra, i: Int, deltaMs: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i && l.ms != null) l.copy(ms = (l.ms + deltaMs).coerceAtLeast(0)) else l })

    /** Pone a la línea [i] el tiempo [ms]. */
    fun poner(letra: Letra, i: Int, ms: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i) l.copy(ms = ms.coerceAtLeast(0)) else l })

    /** Quita el tiempo de la línea [i]. */
    fun quitar(letra: Letra, i: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i) l.copy(ms = null) else l })

    /** **Mueve toda la letra** [deltaMs] (lo que en LRC es `[offset:]`): para una canción que va adelantada o atrasada. */
    fun desplazar(letra: Letra, deltaMs: Int): Letra =
        letra.copy(lineas = letra.lineas.map { l -> l.ms?.let { l.copy(ms = (it + deltaMs).coerceAtLeast(0)) } ?: l })

    /** Las líneas cuyo tiempo es anterior al de una que va delante: casi siempre, una marca que cayó mal. */
    fun desordenadas(letra: Letra): Set<Int> {
        val r = mutableSetOf<Int>()
        var maximo = -1
        letra.lineas.forEachIndexed { i, l -> val ms = l.ms ?: return@forEachIndexed; if (ms < maximo) r += i else maximo = ms }
        return r
    }

    /**
     * **Rellena las que no tienen tiempo** repartiéndolas entre la anterior y la siguiente que sí lo tienen (por
     * lo largo de cada línea: una larga se canta más rato). Las de antes de la primera o después de la última
     * con tiempo se quedan sin él: no hay entre qué repartir. Devuelve la letra y cuántas se rellenaron.
     */
    fun rellenar(letra: Letra): Pair<Letra, Int> {
        val l = letra.lineas.toMutableList()
        var puestas = 0
        var i = 0
        while (i < l.size) {
            if (l[i].ms != null) { i++; continue }
            val antes = (i - 1 downTo 0).firstOrNull { l[it].ms != null }
            val despues = (i until l.size).firstOrNull { l[it].ms != null }
            if (antes == null || despues == null) { i = (despues ?: l.size); continue }
            val desde = l[antes].ms!!; val hasta = l[despues].ms!!
            // El tramo de la anterior también cuenta: la anterior se canta antes que estas.
            val pesos = (antes until despues).map { l[it].texto.length.coerceAtLeast(1) }
            val total = pesos.sum().toFloat()
            var acumulado = 0
            for (k in antes + 1 until despues) {
                acumulado += pesos[k - 1 - antes]
                l[k] = l[k].copy(ms = desde + ((hasta - desde) * (acumulado / total)).toInt())
                puestas++
            }
            i = despues + 1
        }
        return letra.copy(lineas = l) to puestas
    }

    /**
     * **La letra como subtítulos SRT**: cada línea con tiempo, hasta que empieza la siguiente (o
     * [DURACION_DE_LA_ULTIMA] la última, sin pasar de [duracionMs]). Las que no tienen tiempo no salen.
     */
    fun aSrt(letra: Letra, duracionMs: Int = 0): String {
        val con = letra.lineas.filter { it.ms != null }.sortedBy { it.ms }
        return buildString {
            con.forEachIndexed { n, l ->
                val ini = l.ms!!
                var fin = con.getOrNull(n + 1)?.ms ?: (ini + DURACION_DE_LA_ULTIMA).let { if (duracionMs > ini) minOf(it, duracionMs) else it }
                if (fin <= ini) fin = ini + 500
                append(n + 1).append('\n')
                append(srt(ini)).append(" --> ").append(srt(fin)).append('\n')
                append(l.texto.lines().filter { it.isNotBlank() }.joinToString("\n")).append("\n\n")
            }
        }.trimEnd() + "\n"
    }

    /** `00:01:02,345` */
    fun srt(ms: Int): String =
        String.format(Locale.ROOT, "%02d:%02d:%02d,%03d", ms / 3_600_000, (ms / 60_000) % 60, (ms / 1000) % 60, ms % 1000)

    /** El tiempo con décimas, para afinar: `1:02.3`. */
    fun conDecimas(ms: Int): String =
        String.format(Locale.ROOT, "%d:%02d.%d", ms / 60_000, (ms / 1000) % 60, (ms % 1000) / 100)

    /**
     * **La letra guardada con toda su precisión**: el formato de PinDesk redondea al segundo, el LRC guarda
     * centésimas. Mientras se edita se usa este; a PinDesk se le devuelve el suyo.
     */
    fun texto(letra: Letra): String = if (letra.conTiempos) LetraDeCancion.aLrc(letra, null, null) else LetraDeCancion.aGuardar(letra)

    /** Una línea nueva vacía detrás de [i] (con el tiempo que se le dé). */
    fun insertarDetras(letra: Letra, i: Int, texto: String, ms: Int? = null): Letra =
        letra.copy(lineas = letra.lineas.toMutableList().apply { add((i + 1).coerceIn(0, size), Linea(ms, texto)) })

    fun borrar(letra: Letra, i: Int): Letra =
        letra.copy(lineas = letra.lineas.filterIndexed { k, _ -> k != i })

    fun cambiarTexto(letra: Letra, i: Int, texto: String): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i) l.copy(texto = texto) else l })
}

/**
 * **Deshacer y rehacer** las ediciones de la letra (guarda los textos, que es lo que se edita). Hasta [maximo]
 * pasos; un cambio nuevo borra lo que se podía rehacer.
 */
class Historial(private val maximo: Int = 100) {
    private val atras = ArrayDeque<String>()
    private val adelante = ArrayDeque<String>()

    val puedeDeshacer: Boolean get() = atras.isNotEmpty()
    val puedeRehacer: Boolean get() = adelante.isNotEmpty()

    /** Antes de cambiar [actual] por otro: lo apunta para poder volver. Un cambio igual al de antes no cuenta. */
    fun apuntar(actual: String) {
        if (atras.lastOrNull() == actual) return
        atras.addLast(actual); if (atras.size > maximo) atras.removeFirst()
        adelante.clear()
    }

    fun deshacer(actual: String): String? = atras.removeLastOrNull()?.also { adelante.addLast(actual) }
    fun rehacer(actual: String): String? = adelante.removeLastOrNull()?.also { atras.addLast(actual) }
}
