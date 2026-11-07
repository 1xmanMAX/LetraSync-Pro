package com.letrasyncpro.motor

import java.io.File

/**
 * **Las canciones recientes** (6-nov-2026): cada una con su audio, su nombre y la letra tal como se dejó, para
 * seguir otro día. Una carpeta con un archivo por canción (`<id>.cancion`): unas cabeceras `#clave: valor` y
 * después la letra, en LRC. Sin base de datos y fácil de leer a mano.
 */
class Biblioteca(private val carpeta: File) {

    /**
     * [origen]: de qué app vino («PinDesk») y [idExterno] su id allí: al retocarla aquí, la letra se le manda de vuelta
     * (`Integracion.ACCION_DEVUELTA`).
     */
    data class Cancion(
        val id: String, val audio: String, val nombre: String, val letra: String, val cuando: Long,
        val origen: String? = null, val idExterno: String? = null
    ) {
        val lineas: Int get() = LetraDeCancion.leer(letra).lineas.size
        val conTiempos: Boolean get() = LetraDeCancion.leer(letra).conTiempos
    }

    /** Las canciones, la última tocada primero. */
    fun todas(): List<Cancion> =
        (carpeta.listFiles { f -> f.name.endsWith(EXT) } ?: emptyArray()).mapNotNull { leer(it) }.sortedByDescending { it.cuando }

    /** Guarda (o pone al día) la canción de [audio]: el mismo audio es la misma canción. Deja solo las [MAXIMO] últimas. */
    fun guardar(audio: String, nombre: String, letra: String, cuando: Long = System.currentTimeMillis(), origen: String? = null, idExterno: String? = null): Cancion {
        carpeta.mkdirs()
        // Lo que ya se sabía de dónde vino no se pierde al guardar sin decirlo.
        val antes = de(audio)
        val c = Cancion(idDe(audio), audio, nombre, letra, cuando, origen ?: antes?.origen, idExterno ?: antes?.idExterno)
        File(carpeta, c.id + EXT).writeText(escribir(c))
        todas().drop(MAXIMO).forEach { borrar(it.id) }
        return c
    }

    fun de(audio: String): Cancion? = File(carpeta, idDe(audio) + EXT).takeIf { it.exists() }?.let { leer(it) }

    fun borrar(id: String) { File(carpeta, id + EXT).delete() }

    private fun leer(f: File): Cancion? = runCatching { leer(f.nameWithoutExtension, f.readText()) }.getOrNull()

    companion object {
        const val EXT = ".cancion"
        const val MAXIMO = 50

        /** El nombre del archivo de un audio: estable y sin caracteres raros. */
        fun idDe(audio: String): String = "c" + audio.hashCode().toUInt().toString(16) + "-" + audio.length

        fun escribir(c: Cancion): String = buildString {
            append("#audio: ").append(c.audio.replace("\n", " ")).append('\n')
            append("#nombre: ").append(c.nombre.replace("\n", " ")).append('\n')
            append("#cuando: ").append(c.cuando).append('\n')
            c.origen?.let { append("#origen: ").append(it.replace("\n", " ")).append('\n') }
            c.idExterno?.let { append("#id: ").append(it.replace("\n", " ")).append('\n') }
            append('\n').append(c.letra)
        }

        fun leer(id: String, texto: String): Cancion? {
            val cab = mutableMapOf<String, String>()
            val lineas = texto.replace("\r\n", "\n").split('\n')
            var i = 0
            while (i < lineas.size && lineas[i].startsWith("#")) {
                val l = lineas[i]; val p = l.indexOf(':')
                if (p > 1) cab[l.substring(1, p).trim()] = l.substring(p + 1).trim()
                i++
            }
            if (i < lineas.size && lineas[i].isEmpty()) i++
            val audio = cab["audio"]?.takeIf { it.isNotBlank() } ?: return null
            return Cancion(id, audio, cab["nombre"].orEmpty(), lineas.drop(i).joinToString("\n"), cab["cuando"]?.toLongOrNull() ?: 0L, cab["origen"], cab["id"])
        }
    }
}
