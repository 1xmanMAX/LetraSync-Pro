package com.letrasyncpro.ui

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.letrasyncpro.Integracion
import com.letrasyncpro.Automatica
import com.letrasyncpro.Mandos
import com.letrasyncpro.motor.AlinearLetra
import com.letrasyncpro.motor.Biblioteca
import com.letrasyncpro.motor.EdicionDeLetra
import com.letrasyncpro.motor.Historial
import com.letrasyncpro.motor.LetraDeCancion
import com.letrasyncpro.motor.MotorWhisper
import com.letrasyncpro.motor.OirCancion
import com.letrasyncpro.motor.Onda
import com.letrasyncpro.motor.Preferencias
import com.letrasyncpro.motor.SincronizadorDeLetra
import com.letrasyncpro.motor.SincronizadorPorPalabras
import com.letrasyncpro.motor.SepararVoz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **LetraSync Pro**, al estilo de Apple (6-nov-2026).
 *
 * Tres pasos en un control segmentado: **Letra** (pegarla, buscarla, abrirla), **Sincronizar** y **Revisar**.
 * La forma principal de sincronizar es **En la onda** ([MarcarEnLaOnda]): se elige dónde empieza a cantarse cada
 * frase, la app propone el punto y la letra se guarda con un **anticipo** (aparece un poco antes de cantarse,
 * `Preferencias.anticipo`). También a toques (por líneas o por palabras) y automática.
 *
 * En Revisar, la letra como en Apple Music y, al tocar una línea, su tiempo grande con −0.1 / +0.1 (mantener
 * repite), «Aquí», «Oír» y «Bucle». Mover una línea nunca mueve otra ni la deja cruzarse.
 */
class LetraSyncActivity : ComponentActivity() {

    private var reproductor: MediaPlayer? = null
    private val biblioteca by lazy { Biblioteca(File(filesDir, "biblioteca")) }
    private val cancelar = AtomicBoolean(false)

    enum class Paso { LETRA, SINCRONIZAR, AFINAR }

    private val archivoDeError get() = File(filesDir, "ultimo-error.txt")
    private fun errorAnterior(): String? = archivoDeError.takeIf { it.exists() }?.readText()
    private fun borrarErrorAnterior() { archivoDeError.delete() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Un fallo sin atrapar: se apunta (para enseñarlo al volver y poder mandarlo) y se deja seguir su curso.
        val previo = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { hilo, e ->
            runCatching {
                archivoDeError.writeText("LetraSync Pro ${com.letrasyncpro.BuildConfig.VERSION_NAME} · Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MODEL}\n" +
                    "Hilo: ${hilo.name}\n" + android.util.Log.getStackTraceString(e))
            }
            previo?.uncaughtException(hilo, e)
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val desdePinDesk = intent?.action == Integracion.ACCION
        val audioInicial = intent?.data ?: (intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        val letraInicial = intent?.getStringExtra(Integracion.EXTRA_LETRA).orEmpty()
        val titulo = intent?.getStringExtra(Integracion.EXTRA_TITULO)
        val quien = intent?.getStringExtra(Integracion.EXTRA_QUIEN_LLAMA) ?: "PinDesk"
        val idExterno = if (desdePinDesk) intent?.getStringExtra(Integracion.EXTRA_ID) else null
        setContent {
            TemaLetraSync {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Pantalla(audioInicial, letraInicial, titulo, if (desdePinDesk) quien else null, idExterno)
                }
            }
        }
    }

    // Ya no se pausa al salir (7-nov-2026): la música sigue de fondo, con sus mandos en la notificación ([Mandos]).

    override fun onDestroy() {
        cancelar.set(true)
        Mandos.quitar()
        reproductor?.release(); reproductor = null
        super.onDestroy()
    }

    private fun nombreDe(uri: Uri): String = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Canción"

    /** La canción, copiada a la caché: el reconocedor lee un archivo. */
    private fun copiaDe(uri: Uri, nombre: String = "cancion"): File? = runCatching {
        val f = File(cacheDir, nombre)
        contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { i.copyTo(it) } } ?: return null
        f
    }.getOrNull()

    private fun compartirArchivo(nombre: String, contenido: String, titulo: String) {
        val f = File(File(cacheDir, "share").apply { mkdirs() }, nombre)
        f.writeText(contenido)
        val u = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_STREAM, u)
            .putExtra(Intent.EXTRA_TEXT, contenido).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { clipData = ClipData.newRawUri(f.name, u) }, titulo))
    }

    /** El paso por el que conviene empezar con [texto]. */
    private fun pasoPara(texto: String): Paso {
        val l = LetraDeCancion.leer(texto)
        return when { l.lineas.isEmpty() -> Paso.LETRA; !l.conTiempos -> Paso.SINCRONIZAR; else -> Paso.AFINAR }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Pantalla(audioInicial: Uri?, letraInicial: String, tituloInicial: String?, quienLlama: String?, idDePinDesk: String?) {
        val contexto = LocalContext.current
        val alcance = rememberCoroutineScope()
        val vibrar = LocalHapticFeedback.current
        var audio by rememberSaveable { mutableStateOf(audioInicial) }
        var nombre by rememberSaveable { mutableStateOf(tituloInicial ?: audioInicial?.let { nombreDe(it) }.orEmpty()) }
        var texto by rememberSaveable { mutableStateOf(letraInicial) }
        var paso by rememberSaveable { mutableStateOf(pasoPara(letraInicial)) }
        var verBiblioteca by rememberSaveable { mutableStateOf(audioInicial == null && quienLlama == null) }
        var marcando by rememberSaveable { mutableStateOf(false) }
        // De qué canción de PinDesk es esta (su id allí), venga de PinDesk ahora o de la biblioteca.
        var idExterno by rememberSaveable { mutableStateOf(idDePinDesk) }
        // Reproducir la biblioteca como un reproductor: la lista, y si al cargar se arranca sola.
        var lista by remember { mutableStateOf<List<Biblioteca.Cancion>>(emptyList()) }
        var sonarAlCargar by remember { mutableStateOf(false) }
        var terminadas by remember { mutableIntStateOf(0) }
        var saltos by remember { mutableIntStateOf(0) }
        var sonando by remember { mutableStateOf(false) }
        var posicion by remember { mutableIntStateOf(0) }
        var duracion by remember { mutableIntStateOf(0) }
        var velocidad by rememberSaveable { mutableFloatStateOf(1f) }
        var avance by remember { mutableStateOf<Float?>(null) }
        var bajando by remember { mutableStateOf(false) }
        var confianzas by remember { mutableStateOf<List<Float>?>(null) }
        var preguntarBajar by remember { mutableStateOf(false) }
        var porLineas by remember { mutableStateOf<SincronizadorDeLetra?>(null) }
        var porPalabras by remember { mutableStateOf<SincronizadorPorPalabras?>(null) }
        var marcadas by remember { mutableIntStateOf(0) }
        // A toques: tocar al terminar cada frase (lo natural: se oye, acaba y se toca) o al empezar.
        var alTerminarFrase by remember { mutableStateOf(Preferencias.alTerminar(contexto)) }
        var finDeLaUltima by remember { mutableStateOf<Int?>(null) }
        var elegida by remember { mutableStateOf<Int?>(null) }
        var bucle by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var onda by remember { mutableStateOf<FloatArray?>(null) }
        var inicios by remember { mutableStateOf<List<Int>>(emptyList()) }
        var iman by remember { mutableStateOf(Preferencias.iman(contexto)) }
        var anticipo by remember { mutableIntStateOf(Preferencias.anticipo(contexto)) }
        var mas by remember { mutableStateOf(false) }
        var moverTodo by remember { mutableStateOf(false) }
        var ajustes by remember { mutableStateOf(false) }
        var vistaPrevia by remember { mutableStateOf(false) }
        var buscarLetra by remember { mutableStateOf(false) }
        var datos by remember { mutableStateOf(false) }
        val historial = remember { Historial() }
        var pasos by remember { mutableIntStateOf(0) } // para que deshacer/rehacer se repinten
        var parada by remember { mutableStateOf<Job?>(null) }
        // El informe de un fallo (el reconocedor que se cayó, o la app la última vez) para mandarlo.
        var informe by remember { mutableStateOf<String?>(null) }
        var cayoLaApp by remember { mutableStateOf(errorAnterior()) }
        // Voz y música separadas: las pistas, cuál suena (0 la canción, 1 solo la voz, 2 el karaoke) y el avance.
        var pistas by remember { mutableStateOf<SepararVoz.Pistas?>(null) }
        var fuente by rememberSaveable { mutableIntStateOf(0) }
        var separando by remember { mutableStateOf<Float?>(null) }
        var bajandoSeparador by remember { mutableStateOf(false) }
        var preguntarSeparar by remember { mutableStateOf(false) }
        val pararSeparar = remember { AtomicBoolean(false) }
        androidx.compose.runtime.DisposableEffect(Unit) { onDispose { pararSeparar.set(true) } }

        val letra = remember(texto) { LetraDeCancion.leer(texto) }
        val desordenadas = remember(letra) { EdicionDeLetra.desordenadas(letra) }
        val tocando = porLineas != null || porPalabras != null

        /** Todo cambio de la letra pasa por aquí: se apunta para deshacerlo. */
        fun cambiar(nueva: LetraDeCancion.Letra) {
            val t = EdicionDeLetra.texto(nueva)
            if (t == texto) return
            historial.apuntar(texto); texto = t; pasos++
        }
        fun ponerTexto(t: String) { if (t != texto) { historial.apuntar(texto); texto = t; pasos++ } }
        fun deshacer() { historial.deshacer(texto)?.let { texto = it; pasos++ } }
        fun rehacer() { historial.rehacer(texto)?.let { texto = it; pasos++ } }

        // Lo escrito a mano cuenta como un solo paso al salir de «Letra».
        var antesDeEditar by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(paso) {
            if (paso == Paso.LETRA) antesDeEditar = texto
            else antesDeEditar?.let { if (it != texto) { historial.apuntar(it); pasos++ }; antesDeEditar = null }
        }

        // Al cambiar de canción: sus pistas separadas, si las hay.
        var cancionWav by remember { mutableStateOf<File?>(null) }
        LaunchedEffect(audio) {
            bucle = null; cancionWav = null
            pistas = audio?.let { u -> withContext(Dispatchers.IO) { SepararVoz.pistas(contexto, u) } }
            if (pistas == null) fuente = 0
            // La canción de trabajo (WAV): el reproductor va al ms exacto y la onda lee lo mismo que suena.
            cancionWav = audio?.let { u -> withContext(Dispatchers.IO) { SepararVoz.prepararCancion(contexto, u) } }
        }
        // Lo que suena: la canción, solo la voz o el karaoke. Al cambiar, sigue por donde iba.
        val origen = remember(audio, pistas, fuente) {
            when { pistas == null || fuente == 0 -> audio; fuente == 1 -> Uri.fromFile(pistas!!.voz); else -> Uri.fromFile(pistas!!.karaoke) }
        }.let { o -> if (o == audio && cancionWav != null) Uri.fromFile(cancionWav) else o }
        // El WAV de lo que suena, para oírlo al arrastrar la onda.
        val wavQueSuena = when { pistas != null && fuente == 1 -> pistas!!.voz; pistas != null && fuente == 2 -> pistas!!.karaoke; else -> cancionWav }
        val raspador = remember(wavQueSuena) { wavQueSuena?.let { runCatching { com.letrasyncpro.motor.Raspador(it) }.getOrNull() } }
        androidx.compose.runtime.DisposableEffect(raspador) { onDispose { raspador?.close() } }
        var audioDelReproductor by remember { mutableStateOf<Uri?>(null) }
        LaunchedEffect(origen) {
            // Misma canción (otra pista, o ya su WAV): sigue por donde iba; canción nueva: desde el principio.
            val misma = audioDelReproductor == audio
            audioDelReproductor = audio
            val era = if (misma) posicion else 0; val sonaba = misma && sonando
            reproductor?.release(); reproductor = null; sonando = false
            val u = origen ?: run { posicion = 0; duracion = 0; return@LaunchedEffect }
            reproductor = withContext(Dispatchers.IO) {
                runCatching { MediaPlayer().apply { setDataSource(contexto, u); prepare() } }.getOrNull()
            }?.apply { setOnCompletionListener { sonando = false; terminadas++ } }
            duracion = reproductor?.duration ?: 0
            if (reproductor == null) { Toast.makeText(contexto, "No se puede reproducir esta canción", Toast.LENGTH_SHORT).show(); return@LaunchedEffect }
            if (era > 0) { reproductor?.seekTo(era.coerceAtMost(duracion).toLong(), MediaPlayer.SEEK_CLOSEST); posicion = era.coerceAtMost(duracion) }
            if (sonaba || sonarAlCargar) { sonarAlCargar = false; reproductor?.start(); runCatching { reproductor?.playbackParams = reproductor!!.playbackParams.setSpeed(velocidad) }; sonando = true }
        }
        // La onda: de la voz sola si está separada (así se ve dónde empieza a cantar), si no de la canción.
        LaunchedEffect(audio, pistas) {
            onda = null; inicios = emptyList()
            val u = audio ?: return@LaunchedEffect
            val voz = pistas?.voz
            val e = withContext(Dispatchers.IO) {
                val cache = File(File(cacheDir, "ondas"), Biblioteca.idDe(u.toString()) + (if (voz != null) "-voz" else "") + ".onda5")
                Onda.leer(cache) ?: runCatching {
                    val archivo = voz ?: withContext(Dispatchers.IO) { SepararVoz.prepararCancion(contexto, u) } ?: return@runCatching null
                    val pcm = File(cacheDir, "pcm-onda.raw")
                    try {
                        pcm.outputStream().buffered().use { com.letrasyncpro.motor.Transcriptor.decodificar(archivo, it) }
                        pcm.inputStream().buffered().use { Onda.envolvente(it) }.also { Onda.guardar(cache, it) }
                    } finally { pcm.delete() }
                }.getOrNull()
            }
            onda = e
            if (e != null) inicios = withContext(Dispatchers.Default) { Onda.inicios(e) }
        }
        // La posición, a cada fotograma y sin tirones ([RelojSuave]): la onda y la letra se mueven lisas.
        val reloj = remember { com.letrasyncpro.motor.RelojSuave() }
        LaunchedEffect(sonando) {
            if (sonando) reloj.reiniciar(reproductor?.currentPosition ?: posicion)
            while (sonando) {
                androidx.compose.runtime.withFrameMillis { t -> posicion = reloj.ahora(reproductor?.currentPosition ?: posicion, t, velocidad, true) }
            }
            posicion = reproductor?.currentPosition ?: posicion
        }
        // **La biblioteca se pone al día sola** (7-nov-2026, también lo que llega de PinDesk, como el escáner): la de
        // PinDesk se guarda con su canción de trabajo (el audio prestado se va) y su id; retocada aquí luego, la letra
        // se le manda de vuelta sola ([Integracion.ACCION_DEVUELTA]).
        var mandadaAPinDesk by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(texto, audio, nombre, cancionWav) {
            val u = audio ?: return@LaunchedEffect
            if (texto.isBlank()) return@LaunchedEffect
            delay(1_200)
            val clave = if (quienLlama != null) cancionWav?.let { Uri.fromFile(it).toString() } ?: return@LaunchedEffect else u.toString()
            val id = idExterno
            withContext(Dispatchers.IO) { runCatching { biblioteca.guardar(clave, nombre, texto, origen = if (id != null) "PinDesk" else null, idExterno = id) } }
            if (id != null && quienLlama == null && texto != mandadaAPinDesk && letra.lineas.isNotEmpty()) {
                mandadaAPinDesk = texto
                runCatching {
                    contexto.sendBroadcast(Intent(Integracion.ACCION_DEVUELTA).setPackage(Integracion.PAQUETE_PINDESK)
                        .putExtra(Integracion.EXTRA_ID, id).putExtra(Integracion.EXTRA_LETRA, LetraDeCancion.aGuardar(letra)))
                }
            }
        }

        /** Abre una canción de la biblioteca; [sonar]: y que suene (como en un reproductor). */
        fun abrirCancion(c: Biblioteca.Cancion, sonar: Boolean) {
            if (!sonar) lista = emptyList()
            audio = Uri.parse(c.audio); nombre = c.nombre; texto = c.letra; paso = pasoPara(c.letra)
            idExterno = c.idExterno; mandadaAPinDesk = c.letra
            verBiblioteca = false; confianzas = null; elegida = null; marcando = false
            sonarAlCargar = sonar
        }
        fun otraDeLaLista(paso: Int) {
            val i = lista.indexOfFirst { it.audio == audio.toString() }
            lista.getOrNull(i + paso)?.let { abrirCancion(it, true) }
        }
        // Acabó una canción de la lista: la siguiente.
        LaunchedEffect(terminadas) { if (terminadas > 0 && lista.isNotEmpty()) otraDeLaLista(1) }

        fun tocar() {
            val r = reproductor ?: return
            parada?.cancel(); parada = null
            if (r.isPlaying) { r.pause(); sonando = false } else {
                r.start()
                runCatching { r.playbackParams = r.playbackParams.setSpeed(velocidad) }
                sonando = true
            }
        }
        fun sonar() { if (reproductor?.isPlaying != true) tocar() }
        fun ir(ms: Int) {
            saltos++
            val m = ms.coerceIn(0, duracion.coerceAtLeast(0))
            reproductor?.seekTo(m.toLong(), MediaPlayer.SEEK_CLOSEST); posicion = m
            reloj.reiniciar(m)
        }
        /** Arrastrando la onda: se mueve y suena lo que hay bajo el dedo (si está parada). */
        fun raspar(ms: Int) { ir(ms); if (!sonando) raspador?.ir(posicion) }
        fun soltarRaspado() { raspador?.soltar() }
        // Los mandos del sistema (notificación, pantalla de bloqueo, auriculares): lo que hacen y lo que se ve.
        Mandos.alSonar = { sonar() }
        Mandos.alPausar = { if (sonando) tocar() }
        Mandos.alIr = { ir(it) }
        Mandos.alSiguiente = { if (lista.isNotEmpty()) otraDeLaLista(1) else ir(posicion + 10_000) }
        Mandos.alAnterior = { if (lista.isNotEmpty() && posicion < 3_000) otraDeLaLista(-1) else ir(if (lista.isNotEmpty()) 0 else posicion - 10_000) }
        LaunchedEffect(sonando, origen, duracion, velocidad, nombre, saltos) {
            if (audio == null || duracion <= 0) return@LaunchedEffect
            Mandos.poner(contexto, Mandos.Estado(
                nombre.substringBeforeLast('.').ifBlank { "Canción" }, if (idExterno != null) "LetraSync Pro · de PinDesk" else "LetraSync Pro",
                sonando, posicion, duracion, velocidad, lista.isNotEmpty()
            ))
        }
        /** Oír un trozo: desde [desde] durante [durante] ms, y para. */
        fun oir(desde: Int, durante: Long = 2_600) {
            ir(desde); sonar()
            parada?.cancel()
            parada = alcance.launch { delay((durante / velocidad).toLong()); if (reproductor?.isPlaying == true) { reproductor?.pause(); sonando = false } }
        }
        fun ponerVelocidad(v: Float) {
            velocidad = v
            reproductor?.takeIf { it.isPlaying }?.let { r -> runCatching { r.playbackParams = r.playbackParams.setSpeed(velocidad) } }
        }
        fun cambiarVelocidad() = ponerVelocidad(when (velocidad) { 1f -> 0.75f; 0.75f -> 0.5f; else -> 1f })

        /** Separar la voz y la música: baja el modelo la primera vez y deja las dos pistas. */
        fun separar() {
            val u = audio ?: return
            if (!SepararVoz.soportado()) { Toast.makeText(contexto, "Este teléfono no puede separar la voz (hace falta uno de 64 bits)", Toast.LENGTH_LONG).show(); return }
            pararSeparar.set(false); separando = 0f; bajandoSeparador = !SepararVoz.modeloListo(contexto)
            val fino = Preferencias.separarFino(contexto)
            alcance.launch {
                val aviso = withContext(Dispatchers.IO) {
                    if (bajandoSeparador) {
                        val ok = SepararVoz.bajarModelo(contexto, { separando = it }, pararSeparar)
                        bajandoSeparador = false; separando = 0f
                        if (!ok) return@withContext if (pararSeparar.get()) null else "No se pudo bajar el separador: mira la conexión"
                    }
                    when (val r = SepararVoz.separar(contexto, u, fino, { separando = it }, pararSeparar)) {
                        is SepararVoz.Resultado.Hecho -> { withContext(Dispatchers.Main) { pistas = r.pistas }; "Listo: ya puedes oír solo la voz o el karaoke" }
                        SepararVoz.Resultado.SinModelo -> "Falta el separador"
                        SepararVoz.Resultado.Cancelado -> null
                        is SepararVoz.Resultado.Fallo -> "No se pudo separar: ${r.detalle}"
                    }
                }
                separando = null
                aviso?.let { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
            }
        }
        // El bucle de una línea: al llegar a su final, vuelve a su principio.
        LaunchedEffect(bucle, posicion) {
            val b = bucle ?: return@LaunchedEffect
            if (sonando && posicion >= b.second) ir(b.first)
        }

        val elegirAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) {
                runCatching { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                audio = u; nombre = nombreDe(u); verBiblioteca = false; marcando = false
                texto = biblioteca.de(u.toString())?.letra.orEmpty()
                paso = pasoPara(texto); confianzas = null; elegida = null
            }
        }
        val elegirLetra = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) runCatching { contentResolver.openInputStream(u)?.use { it.readBytes().decodeToString() } }.getOrNull()?.let { leido ->
                // Un SRT se pasa a letra con tiempos; lo demás (LRC, texto) se lee tal cual.
                val t = if (leido.contains("-->")) EdicionDeLetra.desdeSrt(leido)?.let { EdicionDeLetra.texto(it) } ?: leido else leido
                ponerTexto(t); paso = pasoPara(t)
            }
        }
        var aGuardar by remember { mutableStateOf<(() -> String)?>(null) }
        val guardarArchivo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { u ->
            val g = aGuardar; aGuardar = null
            if (u != null && g != null) runCatching {
                contentResolver.openOutputStream(u)?.use { it.write(g().toByteArray()) }
                Toast.makeText(contexto, "Guardada", Toast.LENGTH_SHORT).show()
            }
        }
        var wavAGuardar by remember { mutableStateOf<File?>(null) }
        val guardarWav = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")) { u ->
            val f = wavAGuardar; wavAGuardar = null
            if (u != null && f != null) alcance.launch {
                val ok = withContext(Dispatchers.IO) { runCatching { contentResolver.openOutputStream(u)?.use { o -> f.inputStream().use { it.copyTo(o) } } }.isSuccess }
                Toast.makeText(contexto, if (ok) "Guardada" else "No se pudo guardar", Toast.LENGTH_SHORT).show()
            }
        }
        val base = nombre.substringBeforeLast('.').ifBlank { "letra" }
        fun guardar(ext: String, contenido: () -> String) { aGuardar = contenido; guardarArchivo.launch("$base.$ext") }

        /** La automática, en el proceso `:reconocedor` ([Automatica]): si el reconocedor se cae, la app no. */
        fun sincronizarSola() {
            val u = audio ?: return
            if (AlinearLetra.lineasDeLaLetra(texto).isEmpty()) { paso = Paso.LETRA; return }
            if (!MotorWhisper.soportado()) { Toast.makeText(contexto, "Este teléfono no puede cargar el reconocedor (hace falta uno de 64 bits)", Toast.LENGTH_LONG).show(); return }
            avance = 0f; bajando = false
            val pegada = texto
            val a = anticipo
            alcance.launch {
                val cancion = withContext(Dispatchers.IO) { SepararVoz.prepararCancion(contexto, u) }
                if (cancion == null) { avance = null; Toast.makeText(contexto, "No se pudo leer la canción", Toast.LENGTH_LONG).show(); return@launch }
                Automatica.empezar(contexto, cancion, pegada)
                delay(1_500) // lo que tarda en arrancar el proceso
                var muerto = 0
                while (true) {
                    val vivo = withContext(Dispatchers.IO) { Automatica.vivo(contexto) }
                    // Muerto dos veces seguidas y sin resultado: se cayó (no un instante entre arrancar y escribir).
                    val e = withContext(Dispatchers.IO) { Automatica.estado(contexto, vivo || muerto < 2) }
                    if (!vivo) muerto++ else muerto = 0
                    when (e) {
                        is Automatica.Estado.EnCurso -> { avance = e.avance; bajando = e.bajando }
                        is Automatica.Estado.Hecho -> {
                            // Lo oído es cuando se canta: con su anticipo, aparece antes.
                            cambiar(EdicionDeLetra.desplazar(LetraDeCancion.leer(e.texto), -a)); paso = Paso.AFINAR
                            confianzas = e.confianzas
                            avance = null; Toast.makeText(contexto, e.aviso, Toast.LENGTH_LONG).show(); return@launch
                        }
                        is Automatica.Estado.Fallo -> { avance = null; Toast.makeText(contexto, e.aviso, Toast.LENGTH_LONG).show(); return@launch }
                        Automatica.Estado.Cancelado -> { avance = null; return@launch }
                        Automatica.Estado.SeCayo -> {
                            avance = null
                            informe = withContext(Dispatchers.IO) { Automatica.informe(contexto) }
                            return@launch
                        }
                    }
                    delay(400)
                }
            }
        }

        fun empezarATocar(palabras: Boolean) {
            // El dedo llega tarde (retardo) y la letra tiene que verse antes (anticipo): las dos se restan.
            val resta = Preferencias.retardo(contexto) + anticipo
            if (palabras) {
                val s = SincronizadorPorPalabras(letra, resta)
                if (s.orden.isEmpty()) { paso = Paso.LETRA; return }
                porPalabras = s
            } else {
                val s = SincronizadorDeLetra.de(letra).also { it.retardoMs = resta }
                if (s.textos.isEmpty()) { paso = Paso.LETRA; return }
                porLineas = s
            }
            marcadas = 0; confianzas = null; elegida = null; bucle = null
            ir(0); sonar()
        }
        fun acabarDeTocar(guardar: Boolean = true) {
            var nueva = porLineas?.letra() ?: porPalabras?.letra()
            // «Al terminar»: el último toque cierra la última frase con una marca vacía (la letra se apaga ahí).
            val fin = finDeLaUltima
            if (nueva != null && fin != null) nueva = nueva.copy(lineas = nueva.lineas + LetraDeCancion.Linea(fin, ""))
            porLineas = null; porPalabras = null; finDeLaUltima = null
            reproductor?.pause(); sonando = false
            if (guardar && nueva != null && nueva.conTiempos) { cambiar(nueva); paso = Paso.AFINAR }
        }
        fun marcar() {
            vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val s = porLineas
            // «Al terminar cada frase»: tras la última, un toque más dice dónde acaba.
            if (s != null && alTerminarFrase && s.terminado) {
                finDeLaUltima = (posicion - Preferencias.retardo(contexto)).coerceAtLeast(0); acabarDeTocar(); return
            }
            val sigue = s?.marcar(posicion) ?: porPalabras?.marcar(posicion) ?: return
            marcadas++
            if (!sigue && !(s != null && alTerminarFrase)) acabarDeTocar()
        }
        fun atrasAlTocar() {
            (porLineas?.deshacer() ?: porPalabras?.deshacer())?.let { ir(it) }
            marcadas++
        }
        fun elegir(i: Int?) { elegida = i; bucle = null }
        fun devolver() {
            setResult(Activity.RESULT_OK, Intent()
                .putExtra(Integracion.EXTRA_LETRA, LetraDeCancion.aGuardar(letra))
                .putExtra(Integracion.EXTRA_LRC, LetraDeCancion.aLrc(letra)))
            finish()
        }
        fun acabarDeMarcar() { marcando = false; parada?.cancel(); reproductor?.pause(); sonando = false; if (letra.conTiempos) paso = Paso.AFINAR }

        BackHandler(enabled = vistaPrevia || elegida != null || tocando || marcando || (verBiblioteca && audio != null)) {
            when {
                vistaPrevia -> vistaPrevia = false
                tocando -> acabarDeTocar()
                marcando -> acabarDeMarcar()
                elegida != null -> elegir(null)
                else -> verBiblioteca = false
            }
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // ── La barra de navegación ─────────────────────────────────────────────────────────
            @Suppress("UNUSED_EXPRESSION") pasos
            BarraDeNavegacion(
                titulo = when { verBiblioteca -> ""; marcando -> "En la onda"; tocando -> "Al ritmo"; else -> nombre.substringBeforeLast('.').ifBlank { "LetraSync Pro" } },
                subtitulo = if (!verBiblioteca && !marcando && !tocando && audio != null) (if (quienLlama != null) "Desde $quienLlama · ${letra.lineas.count { it.ms != null }} de ${letra.lineas.size} con tiempo" else "${letra.lineas.count { it.ms != null }} de ${letra.lineas.size} con tiempo") else null,
                grande = !verBiblioteca && !marcando && !tocando && audio != null,
                izquierda = {
                    when {
                        marcando -> BotonDeBarra("Atrás", icono = Icons.Filled.ChevronLeft) { acabarDeMarcar() }
                        tocando -> BotonDeBarra("Cancelar") { acabarDeTocar(guardar = false) }
                        quienLlama != null -> BotonDeBarra("Cancelar") { setResult(Activity.RESULT_CANCELED); finish() }
                        verBiblioteca -> if (audio != null) BotonDeBarra("Volver", icono = Icons.Filled.ChevronLeft) { verBiblioteca = false }
                        else -> BotonDeBarra("Biblioteca", icono = Icons.Filled.ChevronLeft) { verBiblioteca = true }
                    }
                },
                derecha = {
                    when {
                        marcando -> BotonDeBarra("Listo", principal = true) { acabarDeMarcar() }
                        tocando -> BotonDeBarra("Terminar", principal = true) { acabarDeTocar() }
                        verBiblioteca -> BotonDeBarra("", icono = Icons.Filled.Add) { elegirAudio.launch(arrayOf("audio/*")) }
                        else -> {
                            BotonDeBarra("", icono = Icons.AutoMirrored.Filled.Undo, activo = historial.puedeDeshacer) { deshacer() }
                            if (historial.puedeRehacer) BotonDeBarra("", icono = Icons.AutoMirrored.Filled.Redo) { rehacer() }
                            BotonDeBarra("", icono = Icons.Filled.MoreHoriz) { mas = true }
                            if (quienLlama != null) BotonDeBarra("Guardar", principal = true, activo = letra.lineas.isNotEmpty()) { devolver() }
                        }
                    }
                }
            )

            separando?.let { a ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer).padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (bajandoSeparador) "Bajando el separador… ${(a * 100).toInt()} %" else "Separando voz y música… ${(a * 100).toInt()} %", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        LinearProgressIndicator(progress = { a }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(3.dp).clip(RoundedCornerShape(2.dp)), trackColor = relleno())
                    }
                    BotonDeBarra("Parar") { pararSeparar.set(true) }
                }
            }
            if (verBiblioteca) {
                Box(Modifier.weight(1f)) {
                    ListaDeLaBiblioteca(
                        alAbrir = { c -> abrirCancion(c, false) },
                        alReproducir = { c, todas -> lista = todas; abrirCancion(c, true) },
                        alNueva = { elegirAudio.launch(arrayOf("audio/*")) }
                    )
                }
            } else if (audio == null) {
                Vacio(Icons.Filled.MusicNote, "Elige una canción", "Abre un audio del teléfono para ponerle la letra a tiempo.", "Abrir canción") { elegirAudio.launch(arrayOf("audio/*")) }
            } else {
                if (!tocando && !marcando) Segmentado(
                    listOf("Letra", "Sincronizar", "Revisar"), paso.ordinal,
                    activas = listOf(true, letra.lineas.isNotEmpty(), letra.conTiempos),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) { paso = Paso.entries[it]; elegir(null) }

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        tocando -> PadDeToques(
                            porLineas, porPalabras, letra, marcadas, alTerminarFrase,
                            alModo = { alTerminarFrase = it; Preferencias.ponerAlTerminar(contexto, it) },
                            alTocar = { marcar() }, alAtras = { atrasAlTocar() }
                        )
                        marcando -> MarcarEnLaOnda(
                            letra, onda, anticipo, posicion, sonando, duracion, velocidad, pistas != null,
                            alVelocidad = { ponerVelocidad(it) },
                            alRaspar = { raspar(it) }, alSoltar = { soltarRaspado() }, alTocar = { tocar() },
                            alMarcar = { i ->
                                vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                // Sonando, el dedo llega un poco tarde: se le resta su retardo. Parada, va justo en la raya.
                                val voz = if (sonando) posicion - Preferencias.retardo(contexto) else posicion
                                cambiar(EdicionDeLetra.marcarVoz(letra, i, voz, anticipo))
                            },
                            alTerminar = { acabarDeMarcar() }
                        )
                        paso == Paso.LETRA -> PasoLetra(
                            texto, alCambiar = { texto = it },
                            alPegar = {
                                val cm = getSystemService(android.content.ClipboardManager::class.java)
                                cm?.primaryClip?.getItemAt(0)?.coerceToText(contexto)?.toString()?.takeIf { it.isNotBlank() }?.let { ponerTexto(it) }
                                    ?: Toast.makeText(contexto, "No hay nada copiado", Toast.LENGTH_SHORT).show()
                            },
                            alBuscar = { buscarLetra = true },
                            alAbrir = { elegirLetra.launch(arrayOf("text/*", "application/octet-stream", "application/x-subrip")) },
                            alSeguir = { paso = if (letra.conTiempos) Paso.AFINAR else Paso.SINCRONIZAR }
                        )
                        paso == Paso.SINCRONIZAR -> PasoSincronizar(
                            avance, bajando, anticipo,
                            alEnLaOnda = { marcando = true; elegir(null) },
                            alRitmo = { empezarATocar(false) },
                            alPalabras = { empezarATocar(true) },
                            alSola = { if (!MotorWhisper.modeloListo(contexto)) preguntarBajar = true else sincronizarSola() },
                            alParar = { cancelar.set(true); Automatica.parar(contexto) }
                        )
                        else -> Column(Modifier.fillMaxSize()) {
                            VistaDeOnda(
                                onda, letra.lineas, posicion, duracion, elegida, iman, inicios,
                                limites = { EdicionDeLetra.limites(letra, it) }, anticipo = anticipo,
                                alIr = { raspar(it) },
                                alMover = { i, voz -> cambiar(EdicionDeLetra.llevar(letra, i, voz - anticipo)); elegida = i },
                                alElegir = { elegir(it) }, alSoltar = { soltarRaspado() },
                                modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(14.dp))
                            )
                            Box(Modifier.weight(1f)) {
                                Karaoke(letra, posicion, confianzas, desordenadas, elegida) { i ->
                                    if (elegida == i) elegir(null) else { elegir(i); letra.lineas[i].ms?.let { ir(it) } }
                                }
                            }
                            val e = elegida
                            if (e != null && e in letra.lineas.indices) AfinarLinea(
                                linea = letra.lineas[e], fuera = e in desordenadas,
                                alMover = { d -> cambiar(EdicionDeLetra.ajustar(letra, e, d)) },
                                alAqui = {
                                    vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    cambiar(EdicionDeLetra.poner(letra, e, EdicionDeLetra.alAparecer(posicion, anticipo)))
                                    confianzas = confianzas?.toMutableList()?.also { if (e in it.indices) it[e] = 1f }
                                    if (e < letra.lineas.lastIndex) elegir(e + 1)
                                },
                                alOir = { letra.lineas[e].ms?.let { oir((it - 1_500).coerceAtLeast(0), 3_500) } },
                                alCerrar = { elegir(null) }
                            )
                        }
                    }
                }

                // ── El reproductor ─────────────────────────────────────────────────────────────
                if (pistas != null && !marcando && !tocando) Segmentado(
                    listOf("Canción", "Solo voz", "Karaoke"), fuente, alto = 32.dp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
                ) { fuente = it }
                if (!marcando) Reproductor(
                    sonando, posicion, duracion, velocidad, compacto = tocando,
                    alTocar = { tocar() }, alIr = { ir(it) }, alVelocidad = { cambiarVelocidad() },
                    alRaspar = { raspar(it) }, alSoltar = { soltarRaspado() },
                    alVistaPrevia = if (letra.conTiempos && !tocando && !marcando) ({ vistaPrevia = true; sonar() }) else null
                )
            }
        }

        if (mas) ModalBottomSheet(onDismissRequest = { mas = false }, containerColor = MaterialTheme.colorScheme.background) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {
                fun cerrar(accion: () -> Unit) { mas = false; accion() }
                val con = letra.conTiempos
                Grupo("Exportar") {
                    Fila("Guardar LRC", if (letra.porPalabras) "Con el tiempo de cada palabra" else "Para reproductores y karaoke", Icons.Filled.Download, Tema.AZUL, con, alTocar = { cerrar { guardar("lrc") { LetraDeCancion.aLrc(letra, letra.titulo ?: base) } } })
                    Fila("Guardar subtítulos SRT", "Para vídeos", Icons.Filled.Download, Tema.AZUL, con, alTocar = { cerrar { guardar("srt") { EdicionDeLetra.aSrt(letra, duracion) } } })
                    Fila("Compartir", null, Icons.Filled.Share, Tema.VERDE, con, separador = false, alTocar = { cerrar { compartirArchivo("$base.lrc", LetraDeCancion.aLrc(letra, letra.titulo ?: base), "Compartir la letra") } })
                }
                Grupo("Voz y música", pie = if (pistas == null) "Separa la voz del resto para cantar encima (karaoke) y para ver solo la voz en la onda. Tarda unos minutos; la primera vez baja el separador (${SepararVoz.MB_MODELO} MB)." else null) {
                    if (pistas == null) Fila("Separar voz y música", null, Icons.Filled.GraphicEq, Tema.ACENTO, separando == null, separador = false, alTocar = { cerrar { if (!SepararVoz.modeloListo(contexto)) preguntarSeparar = true else separar() } })
                    else {
                        Fila("Guardar el karaoke", "La música sin la voz (WAV)", Icons.Filled.Download, Tema.AZUL, alTocar = { cerrar { wavAGuardar = pistas!!.karaoke; guardarWav.launch("$base (karaoke).wav") } })
                        Fila("Guardar la voz", "Solo la voz (WAV)", Icons.Filled.Download, Tema.AZUL, alTocar = { cerrar { wavAGuardar = pistas!!.voz; guardarWav.launch("$base (voz).wav") } })
                        Fila("Quitar las pistas separadas", null, Icons.Filled.DeleteSweep, Tema.ROJO, separador = false, alTocar = { cerrar { audio?.let { SepararVoz.borrar(contexto, it) }; fuente = 0; pistas = null } })
                    }
                }
                Grupo("Toda la letra") {
                    Fila("Adelantar o atrasar todo", null, Icons.Filled.SwapHoriz, Tema.NARANJA, con, alTocar = { cerrar { moverTodo = true } })
                    Fila("Rellenar las que faltan", "Reparte las líneas sin tiempo entre sus vecinas", Icons.Filled.AutoFixHigh, Tema.MORADO, con, alTocar = {
                        cerrar {
                            val (r, n) = EdicionDeLetra.rellenar(letra)
                            if (n > 0) cambiar(r)
                            Toast.makeText(contexto, if (n > 0) "$n líneas con tiempo nuevo" else "No falta ninguna entre dos con tiempo", Toast.LENGTH_SHORT).show()
                        }
                    })
                    Fila("Datos de la canción", null, Icons.Filled.Info, Color(0xFF8E8E93), true, alTocar = { cerrar { datos = true } })
                    Fila("Borrar los tiempos", null, Icons.Filled.DeleteSweep, Tema.ROJO, con, separador = false, alTocar = {
                        cerrar { cambiar(letra.copy(lineas = letra.lineas.map { it.copy(ms = null, palabras = null) })); confianzas = null; paso = Paso.SINCRONIZAR }
                    })
                }
                Grupo {
                    if (quienLlama == null) Fila("Abrir otra canción", null, Icons.Filled.FolderOpen, Tema.AZUL, true, alTocar = { cerrar { elegirAudio.launch(arrayOf("audio/*")) } })
                    Fila("Ajustes", null, Icons.Filled.Settings, Color(0xFF8E8E93), true, separador = false, alTocar = { cerrar { ajustes = true } })
                }
            }
        }

        if (ajustes) ModalBottomSheet(onDismissRequest = { ajustes = false; iman = Preferencias.iman(contexto); anticipo = Preferencias.anticipo(contexto) }, containerColor = MaterialTheme.colorScheme.background) {
            Ajustes()
        }

        if (preguntarBajar) AlertDialog(
            onDismissRequest = { preguntarBajar = false },
            title = { Text("Bajar el reconocedor") },
            text = { Text("Para sincronizar sola, LetraSync Pro baja una vez el reconocedor de voz (unos ${MotorWhisper.MODELOS[MotorWhisper.modelo(contexto)] ?: 160} MB). Después funciona sin conexión.") },
            confirmButton = { TextButton(onClick = { preguntarBajar = false; sincronizarSola() }) { Text("Bajar", fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { preguntarBajar = false }) { Text("Ahora no") } }
        )
        informe?.let { inf ->
            AlertDialog(
                onDismissRequest = { informe = null },
                title = { Text("El reconocedor se cerró") },
                text = { Text("La sincronización automática se cerró antes de terminar (la app sigue bien). Suele ser por memoria: prueba con el reconocedor «Rápida» en Ajustes. Si quieres, manda el informe para arreglarlo.") },
                confirmButton = { TextButton(onClick = { informe = null; compartirArchivo("informe-letrasync.txt", inf, "Mandar el informe") }) { Text("Mandar el informe", fontWeight = FontWeight.SemiBold) } },
                dismissButton = { TextButton(onClick = { informe = null }) { Text("Cerrar") } }
            )
        }
        cayoLaApp?.let { inf ->
            AlertDialog(
                onDismissRequest = { cayoLaApp = null; borrarErrorAnterior() },
                title = { Text("La app se cerró la última vez") },
                text = { Text("Perdona. Si mandas el informe se puede arreglar.") },
                confirmButton = { TextButton(onClick = { cayoLaApp = null; borrarErrorAnterior(); compartirArchivo("informe-letrasync.txt", inf, "Mandar el informe") }) { Text("Mandar el informe", fontWeight = FontWeight.SemiBold) } },
                dismissButton = { TextButton(onClick = { cayoLaApp = null; borrarErrorAnterior() }) { Text("Cerrar") } }
            )
        }
        if (preguntarSeparar) AlertDialog(
            onDismissRequest = { preguntarSeparar = false },
            title = { Text("Separar voz y música") },
            text = { Text("La primera vez se baja el separador (UVR MDX-Net, unos ${SepararVoz.MB_MODELO} MB). Después funciona sin conexión. Una canción tarda unos minutos; puedes seguir usando la app mientras.") },
            confirmButton = { TextButton(onClick = { preguntarSeparar = false; separar() }) { Text("Bajar y separar", fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { preguntarSeparar = false }) { Text("Ahora no") } }
        )
        if (moverTodo) MoverTodaLaLetra(
            alAplicar = { d -> moverTodo = false; if (d != 0) cambiar(EdicionDeLetra.desplazar(letra, d)) },
            alCancelar = { moverTodo = false }
        )
        if (buscarLetra) BuscarEnLrcLib(
            nombre = letra.titulo?.let { t -> letra.artista?.let { "$it - $t" } ?: t } ?: nombre, duracionMs = duracion,
            version = com.letrasyncpro.BuildConfig.VERSION_NAME,
            alElegir = { t -> buscarLetra = false; ponerTexto(t); paso = pasoPara(t); confianzas = null; elegida = null },
            alCerrar = { buscarLetra = false }
        )
        if (datos) DatosDeLaCancion(letra, nombre, alGuardar = { datos = false; cambiar(it) }, alCerrar = { datos = false })
        if (vistaPrevia) androidx.compose.ui.window.Dialog(
            onDismissRequest = { vistaPrevia = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) { VistaPrevia(letra, posicion) { vistaPrevia = false } }
    }

    // ── Los pasos ──────────────────────────────────────────────────────────────────────────────

    /** Paso 1: la letra, con tres botones para traerla. */
    @Composable
    private fun PasoLetra(texto: String, alCambiar: (String) -> Unit, alPegar: () -> Unit, alBuscar: () -> Unit, alAbrir: () -> Unit, alSeguir: () -> Unit) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonIos("Pegar", Modifier.weight(1f), Estilo.GRIS, Icons.Filled.ContentPaste, alto = 46.dp, alTocar = alPegar)
                BotonIos("Buscar", Modifier.weight(1f), Estilo.GRIS, Icons.Filled.Search, alto = 46.dp, alTocar = alBuscar)
                BotonIos("Archivo", Modifier.weight(1f), Estilo.GRIS, Icons.Filled.FolderOpen, alto = 46.dp, alTocar = alAbrir)
            }
            TextField(
                texto, alCambiar,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp).clip(RoundedCornerShape(14.dp)),
                placeholder = { Text("Escribe o pega la letra, una línea por verso.\n\nTambién vale un LRC o un SRT.", lineHeight = 22.sp) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp, letterSpacing = (-0.3).sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                )
            )
            BotonIos("Seguir", Modifier.fillMaxWidth().padding(top = 12.dp), activo = texto.isNotBlank(), alTocar = alSeguir)
        }
    }

    /** Paso 2: las formas de sincronizar; la principal en grande y las demás como lista agrupada. */
    @Composable
    private fun PasoSincronizar(
        avance: Float?, bajando: Boolean, anticipo: Int,
        alEnLaOnda: () -> Unit, alRitmo: () -> Unit, alPalabras: () -> Unit, alSola: () -> Unit, alParar: () -> Unit
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFA2D48), Color(0xFFA4133C))))
                    .clickable(enabled = avance == null, onClick = alEnLaOnda).padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.GraphicEq, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.weight(1f))
                    Text("RECOMENDADO", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Text("En la onda", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp, modifier = Modifier.padding(top = 14.dp))
                Text(
                    "Eliges en la onda dónde empieza cada frase y la app te propone el punto. La letra aparecerá ${"%.1f".format(anticipo / 1000f)} s antes de cantarse.",
                    color = Color.White.copy(alpha = 0.88f), fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 6.dp)
                )
                Row(Modifier.padding(top = 16.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Empezar", color = Color(0xFFD0103A), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFD0103A), modifier = Modifier.size(20.dp))
                }
            }
            Grupo("O a toques, mientras suena") {
                Fila("Al ritmo", "Un toque cuando acaba cada frase", Icons.Filled.TouchApp, Tema.NARANJA, avance == null, alto = 60.dp, alTocar = alRitmo)
                Fila("Palabra a palabra", "Un toque por palabra, para karaoke", Icons.Filled.TextFields, Tema.MORADO, avance == null, alto = 60.dp, separador = false, alTocar = alPalabras)
            }
            Grupo("O que lo haga la app", pie = if (avance == null) "Escucha la canción en el teléfono, sin conexión. Después repásala en «Revisar»." else null) {
                if (avance == null) Fila("Automática", "Pone los tiempos sola", Icons.Filled.AutoAwesome, Tema.AZUL, alto = 60.dp, separador = false, alTocar = alSola)
                else Column(Modifier.padding(16.dp)) {
                    Text(if (bajando) "Bajando el reconocedor… ${(avance * 100).toInt()} %" else "Escuchando la canción… ${(avance * 100).toInt()} %", fontSize = 15.sp)
                    LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(4.dp).clip(RoundedCornerShape(2.dp)), trackColor = relleno())
                    BotonIos("Parar", Modifier.fillMaxWidth(), Estilo.GRIS, alto = 44.dp, alTocar = alParar)
                }
            }
        }
    }

    /**
     * **A toques**: la letra como en Apple Music y abajo, donde cae el pulgar, una tecla grande que marca al bajar
     * el dedo y vibra. Tocar la letra también marca.
     */
    @Composable
    private fun PadDeToques(
        porLineas: SincronizadorDeLetra?, porPalabras: SincronizadorPorPalabras?, letra: LetraDeCancion.Letra, cambio: Int,
        alTerminar: Boolean, alModo: (Boolean) -> Unit, alTocar: () -> Unit, alAtras: () -> Unit
    ) {
        @Suppress("UNUSED_EXPRESSION") cambio
        val acento = MaterialTheme.colorScheme.primary
        val (hechas, total) = porLineas?.let { it.siguiente to it.textos.size } ?: porPalabras!!.let { it.siguiente to it.orden.size }
        val anterior: String; val actual: AnnotatedString; val siguiente: String
        val porFinal = porLineas != null && alTerminar
        // Al terminar: la grande es la que suena (la anterior a la que toca marcar); al empezar, la que va a empezar.
        val k = if (porFinal && porLineas!!.siguiente > 0) porLineas.siguiente - 1 else porLineas?.siguiente ?: 0
        val instruccion = when {
            porPalabras != null -> "Toca al empezar cada palabra"
            !porFinal -> "Toca al empezar cada frase"
            porLineas!!.siguiente == 0 -> "Toca cuando empiece a cantar"
            else -> "Toca cuando termine esta frase"
        }
        if (porLineas != null) {
            anterior = porLineas.textos.getOrNull(k - 1).orEmpty()
            actual = AnnotatedString(porLineas.textos.getOrNull(k) ?: "¡Listo!")
            siguiente = porLineas.textos.getOrNull(k + 1).orEmpty()
        } else {
            val (li, pi) = porPalabras!!.actual ?: (letra.lineas.lastIndex to Int.MAX_VALUE)
            val l = letra.lineas.getOrNull(li)
            anterior = letra.lineas.getOrNull(li - 1)?.texto.orEmpty()
            siguiente = letra.lineas.getOrNull(li + 1)?.texto.orEmpty()
            val tenue = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            actual = if (l == null) AnnotatedString("¡Listo!") else buildAnnotatedString {
                l.trozos.forEachIndexed { k, w ->
                    if (k > 0) append(' ')
                    when {
                        k == pi -> withStyle(SpanStyle(color = acento)) { append(w) }
                        k < pi -> append(w)
                        else -> withStyle(SpanStyle(color = tenue)) { append(w) }
                    }
                }
            }
        }
        val fuente = remember { MutableInteractionSource() }
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else hechas.toFloat() / total },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(3.dp).clip(RoundedCornerShape(2.dp)), trackColor = relleno()
            )
            if (porLineas != null && hechas == 0) Segmentado(
                listOf("Toco al terminar", "Toco al empezar"), if (alTerminar) 0 else 1, alto = 34.dp, modifier = Modifier.padding(top = 4.dp)
            ) { alModo(it == 0) }
            Column(Modifier.weight(1f).fillMaxWidth().clickable(interactionSource = fuente, indication = null, onClick = alTocar), verticalArrangement = Arrangement.Center) {
                Text(anterior, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), maxLines = 2, letterSpacing = (-0.4).sp)
                Spacer(Modifier.height(16.dp))
                AnimatedContent(actual, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "linea") { a ->
                    Text(a, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(siguiente, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), maxLines = 2, letterSpacing = (-0.4).sp)
            }
            Text("$hechas de $total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp))
            val pulsado = remember { mutableStateOf(false) }
            val escala by animateFloatAsState(if (pulsado.value) 0.97f else 1f, label = "toque")
            Box(
                Modifier.fillMaxWidth().height(120.dp).padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(28.dp)).background(acento.copy(alpha = if (pulsado.value) 0.8f else 1f))
                    .pointerInput(Unit) {
                        // Al bajar el dedo, no al levantarlo: así el tiempo es el del toque.
                        awaitEachGesture { awaitFirstDown(); pulsado.value = true; alTocar(); waitForUpOrCancellation(); pulsado.value = false }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Toca", color = Color.White, fontSize = (30 * escala).sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                    Text(instruccion.removePrefix("Toca ").replaceFirstChar { it }, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                }
            }
            BotonIos("Atrás", Modifier.fillMaxWidth().padding(vertical = 8.dp), Estilo.GRIS, Icons.AutoMirrored.Filled.Undo, activo = hechas > 0, alto = 46.dp, alTocar = alAtras)
        }
    }

    /** Paso 3: la letra como en Apple Music (la que suena entera, las demás tenues), con su tiempo encima. */
    @Composable
    private fun Karaoke(letra: LetraDeCancion.Letra, posicion: Int, confianzas: List<Float>?, desordenadas: Set<Int>, elegida: Int?, alTocar: (Int) -> Unit) {
        val actual = LetraDeCancion.lineaActual(letra.lineas, posicion)
        val lista = rememberLazyListState()
        LaunchedEffect(actual, elegida) {
            val objetivo = elegida ?: actual
            if (objetivo >= 0) lista.animateScrollToItem((objetivo - 1).coerceAtLeast(0))
        }
        LazyColumn(Modifier.fillMaxSize(), state = lista, contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
            itemsIndexed(letra.lineas) { i, l ->
                val suena = i == actual
                val dudosa = confianzas?.getOrNull(i)?.let { it < 0.4f } == true
                val mal = i in desordenadas
                val esElegida = i == elegida
                val fondo by animateColorAsState(if (esElegida) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent, label = "fondo")
                val alfa by animateFloatAsState(if (suena || esElegida) 1f else 0.38f, label = "alfa")
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 1.dp).heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(14.dp)).background(fondo).clickable { alTocar(i) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        (l.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "sin tiempo") + when { mal -> "  · va antes que la anterior"; dudosa -> "  · revisar"; else -> "" },
                        fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                        color = when { mal -> Tema.ROJO; dudosa -> Tema.AMBAR; esElegida -> Tema.NARANJA; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                    )
                    Text(
                        if (suena && l.palabras != null) conPalabras(l, posicion, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onSurface) else AnnotatedString(l.texto),
                        fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alfa)
                    )
                }
            }
        }
    }

    /**
     * Afinar la línea elegida, en poco sitio (7-nov-2026: «el botón de en medio llena mucho»): su tiempo entre
     * − y + (mantener repite) y debajo «Oír» y «Aquí». Se cierra tocando la línea otra vez o la ✕.
     */
    @Composable
    private fun AfinarLinea(linea: LetraDeCancion.Linea, fuera: Boolean, alMover: (Int) -> Unit, alAqui: () -> Unit, alOir: () -> Unit, alCerrar: () -> Unit) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp)) {
                val con = linea.ms != null
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonRepetible("−", con, Modifier.width(64.dp)) { alMover(-100) }
                    Text(
                        linea.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "—", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace,
                        color = if (fuera) Tema.ROJO else Tema.NARANJA, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
                    )
                    BotonRepetible("+", con, Modifier.width(64.dp)) { alMover(100) }
                    BotonDeBarra("", icono = Icons.Filled.Close, alTocar = alCerrar)
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonIos("Oír", Modifier.weight(1f), Estilo.TINTADO, Icons.Filled.Headphones, activo = con, alto = 46.dp, alTocar = alOir)
                    BotonIos("Aquí", Modifier.weight(1f), Estilo.LLENO, Icons.Filled.TouchApp, alto = 46.dp, alTocar = alAqui)
                }
            }
        }
    }

    /** Un botón que, mantenido, repite (para afinar de décima en décima sin cansar el dedo). */
    @Composable
    private fun BotonRepetible(texto: String, activo: Boolean, modifier: Modifier, accion: () -> Unit) {
        val a by rememberUpdatedState(accion)
        val vibrar = LocalHapticFeedback.current
        var pulsado by remember { mutableStateOf(false) }
        val alcance = rememberCoroutineScope()
        val fondo by animateColorAsState(if (pulsado) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else relleno(), label = "rep")
        Box(
            modifier.height(Tema.BOTON).clip(RoundedCornerShape(14.dp)).background(fondo)
                .then(if (!activo) Modifier else Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(); pulsado = true
                        a(); vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val repetir = alcance.launch { delay(400); while (true) { a(); delay(90) } }
                        waitForUpOrCancellation()
                        repetir.cancel()
                        pulsado = false
                    }
                }),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = if (activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }

    /** El reproductor de Apple Music: la barra fina y, debajo, los mandos sin fondo, el de reproducir más grande. */
    @Composable
    private fun Reproductor(
        sonando: Boolean, posicion: Int, duracion: Int, velocidad: Float, compacto: Boolean,
        alTocar: () -> Unit, alIr: (Int) -> Unit, alVelocidad: () -> Unit, alVistaPrevia: (() -> Unit)?,
        alRaspar: (Int) -> Unit = alIr, alSoltar: () -> Unit = {}
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (compacto) 2.dp else 6.dp)) {
            BarraDeTiempo(posicion, duracion, alRaspar, alSoltar = alSoltar)
            Row(Modifier.fillMaxWidth().height(if (compacto) 52.dp else 72.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(width = 56.dp, height = 44.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = alVelocidad), contentAlignment = Alignment.Center) {
                    Text(if (velocidad == 1f) "1×" else "$velocidad×", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (velocidad != 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Mando(Icons.Filled.Replay5, "5 s atrás", 34.dp, { alIr(posicion - 5000) })
                Mando(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir", if (compacto) 44.dp else 56.dp, alTocar)
                Mando(Icons.Filled.Forward5, "5 s adelante", 34.dp, { alIr(posicion + 5000) })
                Box(Modifier.size(width = 56.dp, height = 44.dp), contentAlignment = Alignment.Center) {
                    if (alVistaPrevia != null) Mando(Icons.Filled.Fullscreen, "Vista previa", 28.dp, alVistaPrevia, tenue = true)
                }
            }
        }
    }

    @Composable
    private fun Mando(icono: ImageVector, descripcion: String, tamano: Dp, alTocar: () -> Unit, tenue: Boolean = false) {
        val fuente = remember { MutableInteractionSource() }
        val pulsado by fuente.collectIsPressedAsState()
        val escala by animateFloatAsState(if (pulsado) 0.85f else 1f, label = "mando")
        Box(
            Modifier.size(tamano + 12.dp).clip(RoundedCornerShape(50)).clickable(interactionSource = fuente, indication = null, onClick = alTocar),
            contentAlignment = Alignment.Center
        ) { Icon(icono, descripcion, Modifier.size(tamano * escala), tint = if (tenue) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface) }
    }

    @Composable
    private fun Vacio(icono: ImageVector, titulo: String, texto: String, boton: String, alTocar: () -> Unit) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFFFA2D48), Color(0xFFA4133C)))), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = Color.White, modifier = Modifier.size(46.dp))
            }
            Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, modifier = Modifier.padding(top = 20.dp))
            Text(texto, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            BotonIos(boton, Modifier.fillMaxWidth(), icono = Icons.Filled.Add, alTocar = alTocar)
        }
    }

    /** Adelantar o atrasar toda la letra. */
    @Composable
    private fun MoverTodaLaLetra(alAplicar: (Int) -> Unit, alCancelar: () -> Unit) {
        var d by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = alCancelar,
            title = { Text("Adelantar o atrasar todo") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mueve todas las líneas a la vez.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        (if (d > 0) "+" else if (d < 0) "−" else "") + String.format(java.util.Locale.ROOT, "%.1f s", kotlin.math.abs(d) / 1000f),
                        fontSize = 40.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = Tema.NARANJA, modifier = Modifier.padding(vertical = 14.dp)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotonRepetible("Antes", true, Modifier.weight(1f)) { d -= 100 }
                        BotonRepetible("Después", true, Modifier.weight(1f)) { d += 100 }
                    }
                    Text("Mantén pulsado para ir más deprisa.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                }
            },
            confirmButton = { TextButton(onClick = { alAplicar(d) }, enabled = d != 0) { Text("Aplicar", fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
        )
    }

    /** Ajustes, como una hoja de iOS: lo de marcar arriba (anticipo, retardo, imán) y la automática debajo. */
    @Composable
    private fun Ajustes() {
        val contexto = LocalContext.current
        var anticipo by remember { mutableFloatStateOf(Preferencias.anticipo(contexto).toFloat()) }
        var retardo by remember { mutableFloatStateOf(Preferencias.retardo(contexto).toFloat()) }
        var iman by remember { mutableStateOf(Preferencias.iman(contexto)) }
        var modelo by remember { mutableStateOf(MotorWhisper.modelo(contexto)) }
        val colores = SliderDefaults.colors(inactiveTrackColor = relleno())
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {
            Text("Ajustes", fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            Grupo("Al marcar", pie = "El anticipo es cuánto antes de cantarse aparece cada línea. El retardo compensa lo que tarda el dedo al tocar a ritmo.") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row { Text("Anticipo", fontSize = 17.sp, modifier = Modifier.weight(1f)); Text("${"%.1f".format(anticipo / 1000f)} s", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Slider(anticipo, { anticipo = (it / 50).toInt() * 50f; Preferencias.ponerAnticipo(contexto, anticipo.toInt()) }, valueRange = 0f..1000f, colors = colores)
                    Row { Text("Retardo al tocar", fontSize = 17.sp, modifier = Modifier.weight(1f)); Text("${retardo.toInt()} ms", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Slider(retardo, { retardo = (it / 10).toInt() * 10f; Preferencias.ponerRetardo(contexto, retardo.toInt()) }, valueRange = 0f..400f, colors = colores)
                }
                HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                Fila("Imán", "Pegar la marca a donde empieza el sonido", flecha = false, separador = false, final = {
                    Switch(iman, { iman = it; Preferencias.ponerIman(contexto, it) }, colors = SwitchDefaults.colors(checkedTrackColor = Tema.VERDE, checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent))
                })
            }
            var fino by remember { mutableStateOf(Preferencias.separarFino(contexto)) }
            Grupo("Separar voz y música", pie = "Fina: dos pasadas, la voz sale más limpia y tarda el doble.") {
                Fila("Separación fina", null, flecha = false, separador = false, final = {
                    Switch(fino, { fino = it; Preferencias.ponerSepararFino(contexto, it) }, colors = SwitchDefaults.colors(checkedTrackColor = Tema.VERDE, checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent))
                })
            }
            Grupo("Sincronización automática") {
                MotorWhisper.MODELOS.entries.forEachIndexed { k, (m, mb) ->
                    Fila(
                        when (m) { "tiny" -> "Rápida"; "base" -> "Equilibrada"; else -> "Precisa" },
                        "$mb MB · " + if (MotorWhisper.modeloListo(contexto, m)) "bajada" else "se baja la primera vez",
                        flecha = false, separador = k < MotorWhisper.MODELOS.size - 1,
                        alTocar = { modelo = m; Preferencias.poner(contexto, modelo = m) },
                        final = { if (modelo == m) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
            Text("LetraSync Pro ${com.letrasyncpro.BuildConfig.VERSION_NAME}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }

    /** La biblioteca: título grande y la lista de canciones como Apple Music. */
    @Composable
    private fun ListaDeLaBiblioteca(alAbrir: (Biblioteca.Cancion) -> Unit, alReproducir: (Biblioteca.Cancion, List<Biblioteca.Cancion>) -> Unit, alNueva: () -> Unit) {
        var canciones by remember { mutableStateOf<List<Biblioteca.Cancion>?>(null) }
        var borrar by remember { mutableStateOf<Biblioteca.Cancion?>(null) }
        LaunchedEffect(Unit) { canciones = withContext(Dispatchers.IO) { biblioteca.todas() } }
        val l = canciones
        Column(Modifier.fillMaxSize()) {
            Text("Biblioteca", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
            if (l != null && l.isEmpty()) {
                Vacio(Icons.Filled.LibraryMusic, "Aún no hay canciones", "Aquí aparecerán las que sincronices, tal como las dejes.", "Abrir canción", alNueva)
            } else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(l.orEmpty(), key = { _, c -> c.id }) { k, c ->
                    Row(
                        Modifier.fillMaxWidth().combinedClickable(onClick = { alAbrir(c) }, onLongClick = { borrar = c }).padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                                .background(if (c.conTiempos) Brush.linearGradient(listOf(Color(0xFFFA2D48), Color(0xFFA4133C))) else Brush.linearGradient(listOf(Color(0xFF636366), Color(0xFF3A3A3C)))),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.MusicNote, null, tint = Color.White) }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(c.nombre.substringBeforeLast('.').ifBlank { "Canción" }, fontSize = 17.sp, letterSpacing = (-0.4).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Las de PinDesk, como las del escáner: se ve de dónde vinieron (y a dónde vuelve la letra).
                                if (c.origen != null) Text(
                                    c.origen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tema.AZUL,
                                    modifier = Modifier.padding(end = 6.dp).clip(RoundedCornerShape(5.dp)).background(Tema.AZUL.copy(alpha = 0.15f)).padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                                Text(
                                    (if (c.conTiempos) "Sincronizada" else "Sin tiempos") + " · " + android.text.format.DateUtils.getRelativeTimeSpanString(c.cuando),
                                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                                )
                            }
                        }
                        // Reproducir desde aquí (y seguir con las de debajo), como un reproductor de música.
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).background(relleno()).clickable { alReproducir(c, l.orEmpty().drop(k).plus(l.orEmpty().take(k))) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.PlayArrow, "Reproducir", modifier = Modifier.size(24.dp)) }
                    }
                    if (k < (l?.size ?: 0) - 1) HorizontalDivider(Modifier.padding(start = 86.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
        borrar?.let { c ->
            AlertDialog(
                onDismissRequest = { borrar = null },
                title = { Text("Quitar de la biblioteca") },
                text = { Text("«${c.nombre.substringBeforeLast('.')}» se quita de la lista. La canción no se borra del teléfono.") },
                confirmButton = { TextButton(onClick = { biblioteca.borrar(c.id); canciones = canciones?.filter { it.id != c.id }; borrar = null }) { Text("Quitar", color = Tema.ROJO, fontWeight = FontWeight.SemiBold) } },
                dismissButton = { TextButton(onClick = { borrar = null }) { Text("Cancelar") } }
            )
        }
    }

    companion object {
        fun tiempo(ms: Int): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }
    }
}
