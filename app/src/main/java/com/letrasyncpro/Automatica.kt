package com.letrasyncpro

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.letrasyncpro.motor.AlinearLetra
import com.letrasyncpro.motor.MotorWhisper
import com.letrasyncpro.motor.OirCancion
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **La sincronización automática, en su propio proceso** (7-nov-2026: «al darle a automática, la app se cierra»).
 * El reconocedor (Whisper, nativo) corre en el proceso `:reconocedor`: si se cae, cae él solo y la app lo ve, lo
 * dice y guarda un informe para mandarlo. Se hablan por archivos en `files/auto/`: la petición, el avance, el
 * resultado y una marca para pararlo.
 */
object Automatica {
    private const val PROCESO = ":reconocedor"

    sealed class Estado {
        class EnCurso(val avance: Float, val bajando: Boolean) : Estado()
        class Hecho(val texto: String, val confianzas: List<Float>, val aviso: String) : Estado()
        class Fallo(val aviso: String) : Estado()
        object Cancelado : Estado()
        /** El proceso del reconocedor se cerró sin terminar (un fallo de la biblioteca nativa, o sin memoria). */
        object SeCayo : Estado()
    }

    fun carpeta(c: Context) = File(c.filesDir, "auto").apply { mkdirs() }

    fun empezar(c: Context, cancion: File, letra: String) {
        val d = carpeta(c)
        d.listFiles()?.forEach { it.delete() }
        File(d, "letra.txt").writeText(letra)
        File(d, "cancion.txt").writeText(cancion.absolutePath)
        c.startService(Intent(c, ServicioAutomatico::class.java))
    }

    fun parar(c: Context) { File(carpeta(c), "parar").createNewFile() }

    fun vivo(c: Context): Boolean =
        (c.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses.orEmpty().any { it.processName == c.packageName + PROCESO }

    /** Cómo va. [vivo]: si el proceso sigue (si no y no hay resultado, se cayó). */
    fun estado(c: Context, vivo: Boolean): Estado {
        val d = carpeta(c)
        File(d, "resultado.txt").takeIf { it.exists() }?.readText()?.let { r ->
            val l = r.split('\n', limit = 4)
            return when (l[0]) {
                "HECHO" -> Estado.Hecho(l.getOrElse(3) { "" }, l.getOrElse(2) { "" }.split(',').mapNotNull { it.toFloatOrNull() }, l.getOrElse(1) { "" })
                "CANCELADO" -> Estado.Cancelado
                else -> Estado.Fallo(l.getOrElse(1) { "No se pudo" })
            }
        }
        if (!vivo) return Estado.SeCayo
        val e = File(d, "avance.txt").takeIf { it.exists() }?.readText().orEmpty().split(' ')
        return Estado.EnCurso(e.getOrNull(0)?.toFloatOrNull() ?: 0f, e.getOrNull(1) == "bajando")
    }

    /** El informe de lo último que pasó (el registro de la app), para mandarlo si se cayó. */
    fun informe(c: Context): String = buildString {
        append("LetraSync Pro ${BuildConfig.VERSION_NAME} · Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
        append("Modelo: ${MotorWhisper.modelo(c)}\n\n")
        runCatching {
            val p = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-t", "600"))
            append(p.inputStream.bufferedReader().readText().lines().filter { l ->
                listOf("LetraSync", "sherpa", "DEBUG", "libc", "AndroidRuntime", "onnx", "crash", "Fatal", "SIGSEGV", "SIGABRT", "lowmemory").any { l.contains(it, true) }
            }.takeLast(300).joinToString("\n"))
        }.onFailure { append("(sin registro: ${it.message})") }
    }
}

/** El proceso `:reconocedor`: oye la canción con Whisper y alinea la letra (ver [Automatica]). */
class ServicioAutomatico : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread({ trabajar(); stopSelf() }, "automatica").start()
        return START_NOT_STICKY
    }

    private fun trabajar() {
        val d = Automatica.carpeta(this)
        val resultado = File(d, "resultado.txt")
        val parar = AtomicBoolean(false)
        fun avance(a: Float, bajando: Boolean = false) {
            File(d, "avance.txt").writeText("$a ${if (bajando) "bajando" else ""}")
            if (File(d, "parar").exists()) parar.set(true)
        }
        fun fin(estado: String, aviso: String, conf: String = "", texto: String = "") = resultado.writeText("$estado\n$aviso\n$conf\n$texto")
        try {
            val letra = File(d, "letra.txt").readText()
            val cancion = File(File(d, "cancion.txt").readText())
            val lineas = AlinearLetra.lineasDeLaLetra(letra)
            if (!MotorWhisper.modeloListo(this)) {
                avance(0f, true)
                if (MotorWhisper.asegurarModelo(this) { avance(it, true) } == null) return fin("FALLO", "No se pudo bajar el reconocedor: mira la conexión")
            }
            avance(0f)
            when (val o = OirCancion.oir(this, cancion, lineas, { avance(it) }, parar)) {
                is OirCancion.Oido.Texto -> {
                    val r = AlinearLetra.conTiempos(letra, o.trozos, o.duracionMs) ?: return fin("FALLO", "No se reconoció la letra en la canción")
                    val n = r.alineado.ancladas; val t = r.alineado.lineas.size
                    val aviso = if (r.alineado.aOjo) "Lista: $n de $t líneas se oyeron. Revisa las de color ámbar." else "Lista: $n de $t líneas se oyeron"
                    fin("HECHO", aviso, r.alineado.lineas.joinToString(",") { (if (it.anclada) it.confianza else 0f).toString() }, r.texto)
                }
                OirCancion.Oido.SinModelo -> fin("FALLO", "Falta el reconocedor y no se pudo bajar")
                OirCancion.Oido.Nada -> fin("FALLO", "No se entendió nada en la canción")
                OirCancion.Oido.Cancelada -> fin("CANCELADO", "")
                is OirCancion.Oido.Fallo -> fin("FALLO", "No se pudo oír la canción" + (o.detalle?.let { ": $it" } ?: ""))
            }
        } catch (e: Throwable) {
            android.util.Log.e("LetraSyncAuto", "automática", e)
            fin("FALLO", "Falló: " + (e::class.java.simpleName + ": " + (e.message ?: "")).take(160))
        }
    }
}
