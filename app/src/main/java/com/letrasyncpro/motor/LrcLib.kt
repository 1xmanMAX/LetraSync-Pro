package com.letrasyncpro.motor

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * **Buscar la letra en LRCLib** (lrclib.net, la base abierta de letras sincronizadas que usan Chronograph y
 * muchos reproductores libres). Sin clave. Devuelve las que encuentre, con LRC si la tienen.
 */
object LrcLib {
    const val SERVIDOR = "https://lrclib.net"

    data class Resultado(
        val id: Long, val titulo: String, val artista: String, val album: String?, val duracionS: Double?,
        val instrumental: Boolean, val llana: String?, val sincronizada: String?
    )

    /** La consulta que se hace: con título (y artista si se sabe), o con lo escrito suelto. */
    fun url(titulo: String, artista: String): String {
        fun c(s: String) = URLEncoder.encode(s.trim(), "UTF-8")
        return if (titulo.isBlank()) "$SERVIDOR/api/search?q=${c(artista)}"
        else "$SERVIDOR/api/search?track_name=${c(titulo)}" + (if (artista.isNotBlank()) "&artist_name=${c(artista)}" else "")
    }

    /** La respuesta de `/api/search` (una lista JSON). */
    fun leer(json: String): List<Resultado> {
        val a = JSONArray(json)
        return (0 until a.length()).map { k ->
            val o = a.getJSONObject(k)
            fun t(n: String) = if (o.isNull(n)) null else o.optString(n).takeIf { it.isNotBlank() }
            Resultado(
                o.optLong("id"), t("trackName").orEmpty(), t("artistName").orEmpty(), t("albumName"),
                if (o.isNull("duration")) null else o.optDouble("duration"), o.optBoolean("instrumental"),
                t("plainLyrics"), t("syncedLyrics")
            )
        }
    }

    /**
     * Ordena: primero las sincronizadas y, entre ellas, las que duran lo que la canción ([duracionMs], 0 si no
     * se sabe). Las instrumentales, al final.
     */
    fun ordenar(r: List<Resultado>, duracionMs: Int): List<Resultado> = r.sortedWith(
        compareBy<Resultado>({ it.instrumental }, { it.sincronizada == null }, { x ->
            if (duracionMs <= 0 || x.duracionS == null) 0.0 else kotlin.math.abs(x.duracionS * 1000 - duracionMs)
        })
    )

    /** «Artista - Título.mp3» → (título, artista). Sin guion, todo es el título. */
    fun deNombre(nombre: String): Pair<String, String> {
        val base = nombre.substringBeforeLast('.').replace('_', ' ').trim()
        val p = base.split(" - ", " – ", " — ").map { it.trim() }.filter { it.isNotEmpty() }
        return if (p.size >= 2) p.drop(1).joinToString(" - ") to p[0] else base to ""
    }

    /** Busca (en este hilo, no en el principal). */
    fun buscar(titulo: String, artista: String, version: String): List<Resultado> {
        val c = URL(url(titulo, artista)).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000; c.readTimeout = 15_000
        c.setRequestProperty("User-Agent", "LetraSync Pro $version (https://github.com/1xmanMAX/LetraSync-Pro)")
        try {
            if (c.responseCode != 200) throw java.io.IOException("LRCLib respondió ${c.responseCode}")
            return leer(c.inputStream.use { it.readBytes().decodeToString() })
        } finally { c.disconnect() }
    }
}
