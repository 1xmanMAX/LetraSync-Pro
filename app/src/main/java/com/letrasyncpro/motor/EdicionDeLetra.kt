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

    /** Una línea corrida [d] ms, con sus palabras (nunca por debajo de 0). */
    private fun correr(l: Linea, d: Int): Linea {
        val ms = l.ms ?: return l
        return l.copy(ms = (ms + d).coerceAtLeast(0), palabras = l.palabras?.map { w -> w?.let { (it + d).coerceAtLeast(0) } })
    }

    /** Mueve la línea [i] [deltaMs] (negativo: antes), con sus palabras. Sin tiempo, no hace nada. Nunca por debajo de 0. */
    fun ajustar(letra: Letra, i: Int, deltaMs: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i) correr(l, deltaMs) else l })

    /** **Mueve la línea [i] y todas las de detrás** (lo que hace la edición por lotes de LRC Editor). */
    fun ajustarDesde(letra: Letra, i: Int, deltaMs: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k >= i) correr(l, deltaMs) else l })

    /** Pone a la línea [i] el tiempo [ms] (sus palabras se corren con ella). */
    fun poner(letra: Letra, i: Int, ms: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l ->
            if (k != i) l else if (l.ms != null) correr(l, ms.coerceAtLeast(0) - l.ms) else l.copy(ms = ms.coerceAtLeast(0))
        })

    /** Quita el tiempo de la línea [i] (y el de sus palabras). */
    fun quitar(letra: Letra, i: Int): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l -> if (k == i) l.copy(ms = null, palabras = null) else l })

    /** **Mueve toda la letra** [deltaMs] (lo que en LRC es `[offset:]`): para una canción que va adelantada o atrasada. */
    fun desplazar(letra: Letra, deltaMs: Int): Letra =
        letra.copy(lineas = letra.lineas.map { correr(it, deltaMs) })

    /** Quita los tiempos de las palabras (vuelve a ir por líneas). */
    fun sinPalabras(letra: Letra): Letra = letra.copy(lineas = letra.lineas.map { it.copy(palabras = null) })

    /** El tiempo de cada palabra de la línea [i], de [tiempos] (uno por palabra). La primera, también el de la línea. */
    fun ponerPalabras(letra: Letra, i: Int, tiempos: List<Int?>): Letra =
        letra.copy(lineas = letra.lineas.mapIndexed { k, l ->
            if (k != i) l else l.copy(ms = tiempos.firstOrNull() ?: l.ms, palabras = tiempos.takeIf { t -> t.any { it != null } })
        })

    /**
     * **La palabra que suena** de la línea [l] en [ms]: la última cuyo tiempo pasó (la primera sin marca va con
     * la línea). -1 si la línea no va por palabras o aún no empezó.
     */
    fun palabraActual(l: Linea, ms: Int): Int {
        val p = l.palabras ?: return -1
        var r = -1
        p.forEachIndexed { k, t -> val tt = t ?: if (k == 0) l.ms else null; if (tt != null && tt <= ms) r = k }
        return r
    }

    /** Un SRT (`1\n00:00:01,500 --> 00:00:03,000\nTexto`) como letra; null si no lo parece. */
    fun desdeSrt(texto: String): Letra? {
        val bloques = texto.replace("\r\n", "\n").trim().split(Regex("\n\\s*\n"))
        val lineas = ArrayList<Linea>()
        for (b in bloques) {
            val rs = b.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val k = rs.indexOfFirst { it.contains("-->") }
            if (k < 0) continue
            val ini = srtAms(rs[k].substringBefore("-->").trim()) ?: continue
            val t = rs.drop(k + 1).joinToString(" ").replace(Regex("<[^>]+>"), "").trim()
            if (t.isNotEmpty()) lineas += Linea(ini, t)
        }
        return if (lineas.isEmpty()) null else Letra(lineas.sortedBy { it.ms })
    }

    private fun srtAms(t: String): Int? {
        val m = Regex("""(\d{1,2}):(\d{2}):(\d{2})[,.](\d{1,3})""").matchEntire(t) ?: return null
        val (h, mi, se, f) = m.destructured
        return ((h.toInt() * 60 + mi.toInt()) * 60 + se.toInt()) * 1000 + f.padEnd(3, '0').toInt()
    }

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
    fun texto(letra: Letra): String = if (letra.conTiempos) LetraDeCancion.aLrc(letra) else LetraDeCancion.aGuardar(letra)

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

/**
 * **A toques, palabra a palabra** (como el modo «Word-by-Word» de Chronograph y Voxen): cada toque pone el
 * tiempo de la palabra que toca; la primera de cada línea también se lo pone a la línea. Las líneas vacías no
 * cuentan. [retardoMs]: lo que tarda el dedo en reaccionar, que se resta a cada toque.
 */
class SincronizadorPorPalabras(private val original: Letra, private val retardoMs: Int = 0) {
    /** (línea, palabra) en el orden en que se cantan. */
    val orden: List<Pair<Int, Int>> = original.lineas.flatMapIndexed { i, l -> l.trozos.indices.map { i to it } }
    private val tiempos = arrayOfNulls<Int>(orden.size)
    var siguiente: Int = 0
        private set
    val terminado: Boolean get() = siguiente >= orden.size

    /** La línea y la palabra que tocan ahora (null al acabar). */
    val actual: Pair<Int, Int>? get() = orden.getOrNull(siguiente)

    fun marcar(ms: Int): Boolean {
        if (terminado) return false
        val anterior = if (siguiente > 0) tiempos[siguiente - 1] ?: 0 else 0
        tiempos[siguiente] = maxOf((ms - retardoMs).coerceAtLeast(0), anterior)
        siguiente++
        return !terminado
    }

    /** Quita la última y dice desde dónde volver a oír (2 s antes de la anterior). */
    fun deshacer(): Int? {
        if (siguiente == 0) return null
        siguiente--; tiempos[siguiente] = null
        val previa = if (siguiente > 0) tiempos[siguiente - 1] ?: 0 else 0
        return (previa - 2000).coerceAtLeast(0)
    }

    fun tiempo(n: Int): Int? = tiempos.getOrNull(n)

    /** La letra con lo marcado; las líneas que no se tocaron se quedan como estaban. */
    fun letra(): Letra {
        var r = original
        original.lineas.indices.forEach { i ->
            val ts = orden.indices.filter { orden[it].first == i }.map { tiempos[it] }
            if (ts.any { it != null }) r = EdicionDeLetra.ponerPalabras(r, i, ts)
        }
        return r
    }
}
