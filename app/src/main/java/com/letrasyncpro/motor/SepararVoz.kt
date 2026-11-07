package com.letrasyncpro.motor

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.BufferedOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

/** **El modelo de separar, en el teléfono** (ONNX Runtime por `libletrasync-separador.so`, ver `app/src/main/cpp`). */
class Separador private constructor(private var h: Long) : AutoCloseable {
    fun dato(clave: String): String? = metadato(h, clave)
    fun ejecutar(datos: FloatArray, forma: LongArray): FloatArray = ejecutar(h, datos, forma) ?: error("el modelo no devolvió nada")
    override fun close() { if (h != 0L) { liberar(h); h = 0 } }

    companion object {
        init { System.loadLibrary("onnxruntime"); System.loadLibrary("letrasync-separador") }
        fun abrir(modelo: File, hilos: Int): Separador = Separador(crear(modelo.absolutePath, hilos).also { require(it != 0L) })
        @JvmStatic private external fun crear(ruta: String, hilos: Int): Long
        @JvmStatic private external fun metadato(h: Long, clave: String): String?
        @JvmStatic private external fun ejecutar(h: Long, datos: FloatArray, forma: LongArray): FloatArray?
        @JvmStatic private external fun liberar(h: Long)
    }
}

/**
 * **Separar la voz y el instrumental de una canción** (6-nov-2026, «para poder hacer karaoke»): baja una vez el
 * modelo UVR-MDX-NET-Voc_FT (el de voz más usado de Ultimate Vocal Remover, 67 MB, el mismo que reparte
 * sherpa-onnx), pasa la canción a estéreo a 44,1 kHz y la separa con [SeparacionUvr]. Deja dos WAV en
 * `files/pistas/<canción>/`: `voz.wav` y `karaoke.wav`.
 */
object SepararVoz {
    const val MODELO = "UVR-MDX-NET-Voc_FT.onnx"
    const val URL_MODELO = "https://github.com/k2-fsa/sherpa-onnx/releases/download/source-separation-models/$MODELO"
    const val TAMANO_MODELO = 66_762_795L
    const val MB_MODELO = 67

    data class Pistas(val voz: File, val karaoke: File)

    sealed class Resultado {
        class Hecho(val pistas: Pistas) : Resultado()
        object SinModelo : Resultado()
        object Cancelado : Resultado()
        class Fallo(val detalle: String) : Resultado()
    }

    fun soportado(): Boolean = MotorWhisper.soportado()

    private fun modelo(c: Context) = File(File(c.filesDir, "separador"), MODELO)
    fun modeloListo(c: Context): Boolean = modelo(c).length() == TAMANO_MODELO

    private fun carpeta(c: Context, audio: Uri) = File(File(c.filesDir, "pistas"), Biblioteca.idDe(audio.toString()))

    /** Las pistas de [audio] si ya se separó. */
    fun pistas(c: Context, audio: Uri): Pistas? {
        val d = carpeta(c, audio)
        val p = Pistas(File(d, "voz.wav"), File(d, "karaoke.wav"))
        return p.takeIf { it.voz.length() > 44 && it.karaoke.length() > 44 }
    }

    fun borrar(c: Context, audio: Uri) {
        val d = carpeta(c, audio)
        File(d, "voz.wav").delete(); File(d, "karaoke.wav").delete()
    }

    /** La canción ya decodificada a WAV (44,1 kHz estéreo), si está. */
    fun cancionWav(c: Context, audio: Uri): File? = File(carpeta(c, audio), "cancion.wav").takeIf { it.length() > 44 }

    /**
     * **La canción de trabajo** (7-nov-2026): la canción decodificada una vez a WAV. Con ella el reproductor va
     * al milisegundo exacto (con un MP3, Android calcula la posición a ojo y la onda y lo que suena se
     * desfasaban: las marcas «no quedaban donde las puse») y la onda, el raspado y el separador leen lo mismo.
     */
    fun prepararCancion(c: Context, audio: Uri): File? {
        cancionWav(c, audio)?.let { return it }
        // Ya es una canción de trabajo (una de PinDesk guardada en la biblioteca): esa misma.
        if (audio.scheme == "file") audio.path?.let { File(it) }?.takeIf { it.name == "cancion.wav" && it.length() > 44 }?.let { return it }
        val d = carpeta(c, audio).apply { mkdirs() }
        val parte = File(d, "cancion.wav.parte")
        return runCatching {
            val n = decodificarEstereo(c, audio, parte, 44_100, cabecera = true)
            if (n <= 0) error("vacía")
            File(d, "cancion.wav").also { if (!parte.renameTo(it)) error("no se pudo dejar") }
        }.onFailure { parte.delete() }.getOrNull()
    }

    /** Baja el modelo (una vez). [avance] de 0 a 1. */
    fun bajarModelo(c: Context, avance: (Float) -> Unit, cancelado: AtomicBoolean): Boolean {
        val destino = modelo(c)
        if (modeloListo(c)) return true
        destino.parentFile?.mkdirs()
        val parte = File(destino.parentFile, "$MODELO.parte")
        return runCatching {
            val con = URL(URL_MODELO).openConnection() as HttpURLConnection
            con.instanceFollowRedirects = true; con.connectTimeout = 15_000; con.readTimeout = 60_000
            con.inputStream.use { i ->
                parte.outputStream().buffered().use { o ->
                    val buf = ByteArray(64 * 1024); var total = 0L
                    while (true) {
                        if (cancelado.get()) return false
                        val n = i.read(buf); if (n < 0) break
                        o.write(buf, 0, n); total += n; avance((total.toFloat() / TAMANO_MODELO).coerceAtMost(1f))
                    }
                }
            }
            parte.length() == TAMANO_MODELO && parte.renameTo(destino)
        }.getOrDefault(false).also { if (!it) parte.delete() }
    }

    /**
     * **Separa** [audio]. [avance] de 0 a 1 (la decodificación cuenta un 5 %). [fino]: dos pasadas, más limpia y el
     * doble de lenta.
     */
    fun separar(c: Context, audio: Uri, fino: Boolean, avance: (Float) -> Unit, cancelado: AtomicBoolean): Resultado {
        if (!modeloListo(c)) return Resultado.SinModelo
        val d = carpeta(c, audio).apply { mkdirs() }
        try {
            val pcm = prepararCancion(c, audio) ?: return Resultado.Fallo("no se pudo leer la canción")
            val n = ((pcm.length() - 44) / 4).toInt()
            if (n <= 0) return Resultado.Fallo("la canción está vacía")
            avance(0.05f)
            Separador.abrir(modelo(c), Runtime.getRuntime().availableProcessors().coerceIn(2, 4)).use { m ->
                fun entero(k: String, def: Int) = m.dato(k)?.toIntOrNull() ?: def
                val sep = SeparacionUvr(entero("n_fft", 6144), entero("hop_length", 1024), entero("dim_f", 3072), entero("dim_t", 256), entero("sample_rate", 44_100), m::ejecutar)
                val vozTmp = File(d, "voz.wav.parte"); val karTmp = File(d, "karaoke.wav.parte")
                val ok = RandomAccessFile(pcm, "r").use { raf ->
                    Wav(vozTmp, sep.hercios).use { wv -> Wav(karTmp, sep.hercios).use { wk ->
                        val bytes = ByteArray(sep.tramo * 4); val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                        sep.separar(
                            n,
                            leer = { desde, c0, c1 ->
                                c0.fill(0f); c1.fill(0f)
                                val a = desde.coerceAtLeast(0); val b = (desde + sep.tramo).coerceAtMost(n)
                                if (b > a) {
                                    raf.seek(44 + a * 4L); raf.readFully(bytes, 0, (b - a) * 4)
                                    for (k in 0 until b - a) { c0[a - desde + k] = bb.getShort(k * 4) / 32768f; c1[a - desde + k] = bb.getShort(k * 4 + 2) / 32768f }
                                }
                            },
                            escribir = { v0, v1, i0, i1, cuantas -> wv.escribir(v0, v1, cuantas); wk.escribir(i0, i1, cuantas) },
                            fino = fino, avance = { avance(0.05f + 0.95f * it) }, cancelado = cancelado
                        )
                    } }
                }
                if (!ok) { vozTmp.delete(); karTmp.delete(); return Resultado.Cancelado }
                val p = Pistas(File(d, "voz.wav"), File(d, "karaoke.wav"))
                vozTmp.renameTo(p.voz); karTmp.renameTo(p.karaoke)
                return Resultado.Hecho(p)
            }
        } catch (e: Throwable) {
            android.util.Log.e("LetraSyncSeparar", "separar", e)
            return Resultado.Fallo((e::class.java.simpleName + ": " + (e.message ?: "")).take(140))
        }
    }

    /**
     * La canción a PCM de 16 bits **estéreo** a [hercios] (intercalado, sin cabecera) en [salida]. Mono: el mismo en
     * los dos; más de dos canales: los dos primeros. Devuelve las muestras por canal.
     */
    fun decodificarEstereo(c: Context, audio: Uri, salida: File, hercios: Int, cabecera: Boolean = false): Int {
        val ex = MediaExtractor()
        ex.setDataSource(c, audio, null)
        val pista = (0 until ex.trackCount).firstOrNull { ex.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
            ?: run { ex.release(); error("sin audio") }
        ex.selectTrack(pista)
        val formato = ex.getTrackFormat(pista)
        val codec = MediaCodec.createDecoderByType(formato.getString(MediaFormat.KEY_MIME)!!)
        codec.configure(formato, null, null, 0); codec.start()
        var origen = formato.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var canales = formato.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val info = MediaCodec.BufferInfo()
        var fin = false; var acabada = false
        var escritas = 0
        // Remuestreo lineal: [pos] es dónde cae la siguiente salida entre la muestra anterior (0) y la actual (1).
        var pos = 0.0; var prevL = 0f; var prevR = 0f; var hayPrev = false
        val out = BufferedOutputStream(salida.outputStream(), 1 shl 16)
        if (cabecera) out.write(ByteArray(44))
        val par = ByteArray(4)
        fun poner(l: Float, r: Float) {
            val a = (l * 32767f).toInt().coerceIn(-32768, 32767); val b = (r * 32767f).toInt().coerceIn(-32768, 32767)
            par[0] = a.toByte(); par[1] = (a shr 8).toByte(); par[2] = b.toByte(); par[3] = (b shr 8).toByte()
            out.write(par); escritas++
        }
        try {
            while (!acabada) {
                var algo = false
                if (!fin) {
                    val i = codec.dequeueInputBuffer(0)
                    if (i >= 0) {
                        algo = true
                        val n = ex.readSampleData(codec.getInputBuffer(i)!!, 0)
                        if (n < 0) { codec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); fin = true }
                        else { codec.queueInputBuffer(i, 0, n, ex.sampleTime, 0); ex.advance() }
                    }
                }
                val o = codec.dequeueOutputBuffer(info, 0)
                if (o >= 0) {
                    algo = true
                    val buf = codec.getOutputBuffer(o)!!.order(ByteOrder.LITTLE_ENDIAN)
                    buf.position(info.offset); buf.limit(info.offset + info.size)
                    val sb = buf.asShortBuffer()
                    val marcos = sb.remaining() / canales.coerceAtLeast(1)
                    val paso = origen.toDouble() / hercios
                    for (f in 0 until marcos) {
                        val l = sb.get(f * canales) / 32768f
                        val r = if (canales > 1) sb.get(f * canales + 1) / 32768f else l
                        if (!hayPrev) { prevL = l; prevR = r; hayPrev = true; continue }
                        // Las salidas entre la muestra anterior (0) y esta (1).
                        while (pos < 1.0) { val t = pos.toFloat(); poner(prevL + (l - prevL) * t, prevR + (r - prevR) * t); pos += paso }
                        pos -= 1.0; prevL = l; prevR = r
                    }
                    codec.releaseOutputBuffer(o, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) acabada = true
                } else if (o == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    algo = true
                    codec.outputFormat.let { origen = it.getInteger(MediaFormat.KEY_SAMPLE_RATE); canales = it.getInteger(MediaFormat.KEY_CHANNEL_COUNT) }
                }
                if (!algo) Thread.sleep(1)
            }
            if (hayPrev) poner(prevL, prevR)
        } finally {
            out.close()
            if (cabecera) RandomAccessFile(salida, "rw").use { it.seek(0); it.write(Wav.cabecera(escritas * 4L, hercios)) }
            runCatching { codec.stop() }; runCatching { codec.release() }; runCatching { ex.release() }
        }
        return escritas
    }
}

/** Un WAV de 16 bits estéreo que se va escribiendo; la cabecera se completa al cerrarlo. */
class Wav(private val archivo: File, private val hercios: Int) : AutoCloseable {
    private val raf = RandomAccessFile(archivo, "rw").apply { setLength(0); write(ByteArray(44)) }
    private var datos = 0L
    private var buf = ByteArray(0)

    fun escribir(c0: FloatArray, c1: FloatArray, cuantas: Int) {
        if (buf.size < cuantas * 4) buf = ByteArray(cuantas * 4)
        for (k in 0 until cuantas) {
            val a = (c0[k] * 32767f).toInt().coerceIn(-32768, 32767); val b = (c1[k] * 32767f).toInt().coerceIn(-32768, 32767)
            buf[k * 4] = a.toByte(); buf[k * 4 + 1] = (a shr 8).toByte(); buf[k * 4 + 2] = b.toByte(); buf[k * 4 + 3] = (b shr 8).toByte()
        }
        raf.write(buf, 0, cuantas * 4); datos += cuantas * 4L
    }

    override fun close() {
        raf.seek(0); raf.write(cabecera(datos, hercios)); raf.close()
    }

    companion object {
        fun cabecera(datos: Long, hercios: Int, canales: Int = 2): ByteArray {
            val b = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            b.put("RIFF".toByteArray()); b.putInt((36 + datos).toInt()); b.put("WAVE".toByteArray())
            b.put("fmt ".toByteArray()); b.putInt(16); b.putShort(1); b.putShort(canales.toShort()); b.putInt(hercios)
            b.putInt(hercios * canales * 2); b.putShort((canales * 2).toShort()); b.putShort(16)
            b.put("data".toByteArray()); b.putInt(datos.toInt())
            return b.array()
        }
    }
}
