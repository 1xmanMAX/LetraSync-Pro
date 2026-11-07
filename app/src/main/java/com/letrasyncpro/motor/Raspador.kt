package com.letrasyncpro.motor

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.File
import java.io.RandomAccessFile

/**
 * **Oír la canción al arrastrar la onda** (7-nov-2026: «al moverla con el dedo debería sonar, para saber dónde
 * termina»), como el «scrub» de un editor de audio. Mientras el dedo se mueve, suenan granos de 45 ms justo de
 * donde está, con un fundido corto para que no chasquee; cuando el dedo se queda quieto, calla. Lee el WAV de
 * trabajo (16 bits, estéreo, 44,1 kHz), así suena lo mismo que se ve.
 */
class Raspador(private val wav: File) : AutoCloseable {
    @Volatile private var objetivo = -1
    @Volatile private var cambiado = 0L
    @Volatile private var vivo = true
    private val hercios = 44_100
    private val grano = hercios * 45 / 1000
    private val pista = AudioTrack.Builder()
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        .setAudioFormat(AudioFormat.Builder().setSampleRate(hercios).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
        .setBufferSizeInBytes(maxOf(AudioTrack.getMinBufferSize(hercios, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT), grano * 4 * 2))
        .setTransferMode(AudioTrack.MODE_STREAM).build()
    private val hilo = Thread({ bucle() }, "raspador").apply { isDaemon = true; start() }

    /** El dedo está en [ms]. */
    fun ir(ms: Int) { objetivo = ms; cambiado = System.currentTimeMillis() }

    /** El dedo se levantó. */
    fun soltar() { objetivo = -1 }

    private fun bucle() {
        val bytes = ByteArray(grano * 4)
        val muestras = ShortArray(grano * 2)
        var ultimo = -1
        var sonando = false
        RandomAccessFile(wav, "r").use { raf ->
            val total = ((raf.length() - 44) / 4).toInt()
            while (vivo) {
                val o = objetivo
                // Quieto más de 120 ms, o sin dedo: silencio.
                if (o < 0 || System.currentTimeMillis() - cambiado > 120) {
                    if (sonando) { pista.pause(); pista.flush(); sonando = false }
                    ultimo = -1
                    Thread.sleep(10); continue
                }
                // Lo que sigue a lo último que sonó si el dedo va despacio; si no, de donde está el dedo.
                var desde = (o.toLong() * hercios / 1000).toInt()
                if (ultimo >= 0 && kotlin.math.abs(desde - ultimo) < grano) desde = ultimo
                desde = desde.coerceIn(0, (total - grano).coerceAtLeast(0))
                raf.seek(44 + desde * 4L)
                val n = raf.read(bytes).coerceAtLeast(0) / 4
                for (k in 0 until n) {
                    val f = minOf(1f, k / 220f, (n - k) / 220f) // 5 ms de fundido a cada lado
                    for (c in 0..1) {
                        val v = ((bytes[k * 4 + c * 2 + 1].toInt() shl 8) or (bytes[k * 4 + c * 2].toInt() and 0xFF)).toShort()
                        muestras[k * 2 + c] = (v * f).toInt().toShort()
                    }
                }
                if (!sonando) { pista.play(); sonando = true }
                pista.write(muestras, 0, n * 2)
                ultimo = desde + n
            }
        }
    }

    override fun close() { vivo = false; runCatching { hilo.join(300) }; runCatching { pista.stop() }; pista.release() }
}
