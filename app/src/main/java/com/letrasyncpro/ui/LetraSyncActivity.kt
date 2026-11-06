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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **LetraSync Pro** (rediseño del 6-nov-2026: «botones optimizados para pantalla táctil, más intuitivo y con
 * menos opciones, fácil pero con nivel profesional»).
 *
 * Tres pasos, siempre a la vista:
 * 1. **Letra**: pegarla, buscarla en internet (LRCLib) o abrir un archivo.
 * 2. **Sincronizar**: a toques (un botón enorme donde cae el pulgar), por líneas o por palabras, o automática.
 * 3. **Afinar**: la onda con las líneas encima (se arrastran por su asa) y, al elegir una, −0.1 / +0.1
 *    (mantener repite), «Aquí», «Oír» y «Bucle». **Mover una línea nunca mueve otra** ni la deja cruzarse.
 *
 * Lo poco frecuente (guardar, compartir, mover toda la letra, ajustes…) va en «Más». Todo lo que se toca mide
 * 48 dp o más.
 */
class LetraSyncActivity : ComponentActivity() {

    private var reproductor: MediaPlayer? = null
    private val biblioteca by lazy { Biblioteca(File(filesDir, "biblioteca")) }
    private val cancelar = AtomicBoolean(false)

    enum class Paso { LETRA, SINCRONIZAR, AFINAR }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val desdePinDesk = intent?.action == Integracion.ACCION
        val audioInicial = intent?.data ?: (intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        val letraInicial = intent?.getStringExtra(Integracion.EXTRA_LETRA).orEmpty()
        val titulo = intent?.getStringExtra(Integracion.EXTRA_TITULO)
        val quien = intent?.getStringExtra(Integracion.EXTRA_QUIEN_LLAMA) ?: "PinDesk"
        setContent {
            TemaLetraSync {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Pantalla(audioInicial, letraInicial, titulo, if (desdePinDesk) quien else null)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        reproductor?.takeIf { it.isPlaying }?.pause()
    }

    override fun onDestroy() {
        cancelar.set(true)
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
    private fun Pantalla(audioInicial: Uri?, letraInicial: String, tituloInicial: String?, quienLlama: String?) {
        val contexto = LocalContext.current
        val alcance = rememberCoroutineScope()
        val vibrar = LocalHapticFeedback.current
        var audio by rememberSaveable { mutableStateOf(audioInicial) }
        var nombre by rememberSaveable { mutableStateOf(tituloInicial ?: audioInicial?.let { nombreDe(it) }.orEmpty()) }
        var texto by rememberSaveable { mutableStateOf(letraInicial) }
        var paso by rememberSaveable { mutableStateOf(pasoPara(letraInicial)) }
        var verBiblioteca by rememberSaveable { mutableStateOf(audioInicial == null && quienLlama == null) }
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
        var modoPalabras by rememberSaveable { mutableStateOf(false) }
        var marcadas by remember { mutableIntStateOf(0) }
        var elegida by remember { mutableStateOf<Int?>(null) }
        var bucle by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var onda by remember { mutableStateOf<FloatArray?>(null) }
        var inicios by remember { mutableStateOf<List<Int>>(emptyList()) }
        var iman by remember { mutableStateOf(Preferencias.iman(contexto)) }
        var mas by remember { mutableStateOf(false) }
        var moverTodo by remember { mutableStateOf(false) }
        var ajustes by remember { mutableStateOf(false) }
        var vistaPrevia by remember { mutableStateOf(false) }
        var buscarLetra by remember { mutableStateOf(false) }
        var datos by remember { mutableStateOf(false) }
        val historial = remember { Historial() }
        var pasos by remember { mutableIntStateOf(0) } // para que deshacer/rehacer se repinten

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

        // El reproductor y la onda, al tener canción.
        LaunchedEffect(audio) {
            reproductor?.release(); reproductor = null; sonando = false; posicion = 0; duracion = 0
            onda = null; inicios = emptyList(); bucle = null
            val u = audio ?: return@LaunchedEffect
            reproductor = runCatching {
                MediaPlayer().apply { setDataSource(contexto, u); prepare(); setOnCompletionListener { sonando = false } }
            }.getOrNull()
            duracion = reproductor?.duration ?: 0
            if (reproductor == null) Toast.makeText(contexto, "No se puede reproducir esta canción", Toast.LENGTH_SHORT).show()
            val e = withContext(Dispatchers.IO) {
                val cache = File(File(cacheDir, "ondas"), Biblioteca.idDe(u.toString()) + ".onda")
                Onda.leer(cache) ?: runCatching {
                    val archivo = copiaDe(u, "cancion-onda") ?: return@runCatching null
                    val pcm = File(cacheDir, "pcm-onda.raw")
                    try {
                        pcm.outputStream().buffered().use { com.letrasyncpro.motor.Transcriptor.decodificar(archivo, it) }
                        pcm.inputStream().buffered().use { Onda.envolvente(it) }.also { Onda.guardar(cache, it) }
                    } finally { pcm.delete(); archivo.delete() }
                }.getOrNull()
            }
            onda = e
            if (e != null) inicios = withContext(Dispatchers.Default) { Onda.inicios(e) }
        }
        LaunchedEffect(sonando) {
            while (sonando) { posicion = reproductor?.currentPosition ?: 0; delay(40) }
            posicion = reproductor?.currentPosition ?: posicion
        }
        // La biblioteca se pone al día sola (no con lo que llega de PinDesk: su audio es prestado).
        LaunchedEffect(texto, audio, nombre) {
            val u = audio ?: return@LaunchedEffect
            if (quienLlama != null || texto.isBlank()) return@LaunchedEffect
            delay(700)
            withContext(Dispatchers.IO) { runCatching { biblioteca.guardar(u.toString(), nombre, texto) } }
        }

        fun tocar() {
            val r = reproductor ?: return
            if (r.isPlaying) { r.pause(); sonando = false } else {
                r.start()
                runCatching { r.playbackParams = r.playbackParams.setSpeed(velocidad) }
                sonando = true
            }
        }
        fun sonar() { if (reproductor?.isPlaying != true) tocar() }
        fun ir(ms: Int) { val m = ms.coerceIn(0, duracion.coerceAtLeast(0)); reproductor?.seekTo(m); posicion = m }
        fun cambiarVelocidad() {
            velocidad = when (velocidad) { 1f -> 0.75f; 0.75f -> 0.5f; else -> 1f }
            reproductor?.takeIf { it.isPlaying }?.let { r -> runCatching { r.playbackParams = r.playbackParams.setSpeed(velocidad) } }
        }
        // El bucle de una línea: al llegar a su final, vuelve a su principio.
        LaunchedEffect(bucle, posicion) {
            val b = bucle ?: return@LaunchedEffect
            if (sonando && posicion >= b.second) ir(b.first)
        }

        val elegirAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) {
                runCatching { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                audio = u; nombre = nombreDe(u); verBiblioteca = false
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
        val base = nombre.substringBeforeLast('.').ifBlank { "letra" }
        fun guardar(ext: String, contenido: () -> String) { aGuardar = contenido; guardarArchivo.launch("$base.$ext") }

        fun sincronizarSola() {
            val u = audio ?: return
            val lineas = AlinearLetra.lineasDeLaLetra(texto)
            if (lineas.isEmpty()) { Toast.makeText(contexto, "Pon antes la letra", Toast.LENGTH_SHORT).show(); return }
            if (!MotorWhisper.soportado()) { Toast.makeText(contexto, "Este teléfono no puede cargar el reconocedor (hace falta uno de 64 bits)", Toast.LENGTH_LONG).show(); return }
            cancelar.set(false)
            avance = 0f; bajando = !MotorWhisper.modeloListo(contexto)
            val pegada = texto
            alcance.launch {
                val aviso = withContext(Dispatchers.IO) {
                    if (bajando) {
                        val ok = MotorWhisper.asegurarModelo(contexto) { avance = it } != null
                        bajando = false; avance = 0f
                        if (!ok) return@withContext "No se pudo bajar el reconocedor: mira la conexión"
                    }
                    val archivo = copiaDe(u) ?: return@withContext "No se pudo leer la canción"
                    when (val o = OirCancion.oir(contexto, archivo, lineas, { avance = it }, cancelar)) {
                        is OirCancion.Oido.Texto -> {
                            val r = AlinearLetra.conTiempos(pegada, o.trozos, o.duracionMs) ?: return@withContext "No se reconoció la letra en la canción"
                            withContext(Dispatchers.Main) {
                                cambiar(LetraDeCancion.leer(r.texto)); paso = Paso.AFINAR
                                confianzas = r.alineado.lineas.map { if (it.anclada) it.confianza else 0f }
                            }
                            val n = r.alineado.ancladas; val t = r.alineado.lineas.size
                            if (r.alineado.aOjo) "Lista: $n de $t líneas se oyeron. Revisa las de color ámbar." else "Lista: $n de $t líneas se oyeron"
                        }
                        OirCancion.Oido.SinModelo -> "Falta el reconocedor y no se pudo bajar"
                        OirCancion.Oido.Nada -> "No se entendió nada en la canción"
                        OirCancion.Oido.Cancelada -> null
                        is OirCancion.Oido.Fallo -> "No se pudo oír la canción" + (o.detalle?.let { ": $it" } ?: "")
                    }
                }
                avance = null
                aviso?.let { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
            }
        }

        fun empezarATocar() {
            val retardo = Preferencias.retardo(contexto)
            if (modoPalabras) {
                val s = SincronizadorPorPalabras(letra, retardo)
                if (s.orden.isEmpty()) { paso = Paso.LETRA; return }
                porPalabras = s
            } else {
                val s = SincronizadorDeLetra.de(letra).also { it.retardoMs = retardo }
                if (s.textos.isEmpty()) { paso = Paso.LETRA; return }
                porLineas = s
            }
            marcadas = 0; confianzas = null; elegida = null; bucle = null
            ir(0); sonar()
        }
        fun acabarDeTocar(guardar: Boolean = true) {
            val nueva = porLineas?.letra() ?: porPalabras?.letra()
            porLineas = null; porPalabras = null
            reproductor?.pause(); sonando = false
            if (guardar && nueva != null && nueva.conTiempos) { cambiar(nueva); paso = Paso.AFINAR }
        }
        fun marcar() {
            vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val sigue = porLineas?.marcar(posicion) ?: porPalabras?.marcar(posicion) ?: return
            marcadas++
            if (!sigue) acabarDeTocar()
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

        BackHandler(enabled = vistaPrevia || elegida != null || tocando || (verBiblioteca && audio != null)) {
            when {
                vistaPrevia -> vistaPrevia = false
                tocando -> acabarDeTocar()
                elegida != null -> elegir(null)
                else -> verBiblioteca = false
            }
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // ── Cabecera ──────────────────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (quienLlama == null) IconButton(onClick = { if (!tocando) verBiblioteca = !verBiblioteca }, modifier = Modifier.size(Tema.TOQUE)) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, "Biblioteca", tint = if (verBiblioteca) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                } else Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(
                        if (verBiblioteca) "Biblioteca" else nombre.substringBeforeLast('.').ifBlank { "LetraSync Pro" },
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (!verBiblioteca && audio != null) Text(
                        if (quienLlama != null) "Desde $quienLlama" else "${tiempo(duracion)} · ${letra.lineas.count { it.ms != null }}/${letra.lineas.size} con tiempo",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                    )
                }
                @Suppress("UNUSED_EXPRESSION") pasos
                if (!verBiblioteca && !tocando) {
                    IconButton(onClick = { deshacer() }, enabled = historial.puedeDeshacer, modifier = Modifier.size(Tema.TOQUE)) { Icon(Icons.AutoMirrored.Filled.Undo, "Deshacer") }
                    if (historial.puedeRehacer) IconButton(onClick = { rehacer() }, modifier = Modifier.size(Tema.TOQUE)) { Icon(Icons.AutoMirrored.Filled.Redo, "Rehacer") }
                    IconButton(onClick = { mas = true }, modifier = Modifier.size(Tema.TOQUE)) { Icon(Icons.Filled.MoreHoriz, "Más") }
                }
                if (quienLlama != null && !tocando) Button(
                    onClick = { devolver() }, enabled = letra.lineas.isNotEmpty(),
                    modifier = Modifier.height(44.dp), contentPadding = PaddingValues(horizontal = 16.dp)
                ) { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Guardar") }
            }

            if (verBiblioteca) {
                Box(Modifier.weight(1f)) {
                    ListaDeLaBiblioteca(
                        alAbrir = { c ->
                            audio = Uri.parse(c.audio); nombre = c.nombre; texto = c.letra; paso = pasoPara(c.letra)
                            verBiblioteca = false; confianzas = null; elegida = null
                        },
                        alNueva = { elegirAudio.launch(arrayOf("audio/*")) }
                    )
                }
            } else if (audio == null) {
                Vacio(Icons.Filled.LibraryMusic, "Elige una canción", "Abre un audio del teléfono para ponerle la letra a tiempo.", "Abrir canción") { elegirAudio.launch(arrayOf("audio/*")) }
            } else {

            // ── Los tres pasos ─────────────────────────────────────────────────────────────────
            if (!tocando) Pasos(paso, letra.lineas.isNotEmpty(), letra.conTiempos) { p -> paso = p; elegir(null) }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    tocando -> PadDeToques(
                        porLineas, porPalabras, letra, posicion, marcadas,
                        alTocar = { marcar() }, alAtras = { atrasAlTocar() }, alTerminar = { acabarDeTocar() }
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
                        modoPalabras, { modoPalabras = it }, avance, bajando,
                        yaTiene = letra.conTiempos,
                        alTocar = { empezarATocar() },
                        alSola = { if (!MotorWhisper.modeloListo(contexto)) preguntarBajar = true else sincronizarSola() },
                        alParar = { cancelar.set(true) }
                    )
                    else -> Column(Modifier.fillMaxSize()) {
                        VistaDeOnda(
                            onda, letra.lineas, posicion, duracion, elegida, iman, inicios,
                            limites = { EdicionDeLetra.limites(letra, it) },
                            alIr = { ir(it) },
                            alMover = { i, ms -> cambiar(EdicionDeLetra.llevar(letra, i, ms)); elegida = i },
                            alElegir = { elegir(it) },
                            modifier = Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp))
                        )
                        Box(Modifier.weight(1f)) {
                            Karaoke(letra, posicion, confianzas, desordenadas, elegida) { i ->
                                if (elegida == i) elegir(null) else { elegir(i); letra.lineas[i].ms?.let { ir(it) } }
                            }
                        }
                        val e = elegida
                        if (e != null && e in letra.lineas.indices) AfinarLinea(
                            linea = letra.lineas[e], numero = e + 1, total = letra.lineas.size, enBucle = bucle != null,
                            fuera = e in desordenadas,
                            alMover = { d -> cambiar(EdicionDeLetra.ajustar(letra, e, d)) },
                            alAqui = {
                                vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                cambiar(EdicionDeLetra.poner(letra, e, posicion))
                                confianzas = confianzas?.toMutableList()?.also { if (e in it.indices) it[e] = 1f }
                                if (e < letra.lineas.lastIndex) elegir(e + 1)
                            },
                            alOir = { letra.lineas[e].ms?.let { ir((it - 2000).coerceAtLeast(0)); sonar() } },
                            alBucle = {
                                val ini = letra.lineas[e].ms
                                bucle = if (bucle != null || ini == null) null else {
                                    val fin = letra.lineas.drop(e + 1).firstNotNullOfOrNull { it.ms }?.takeIf { it > ini } ?: (ini + 4000)
                                    val desde = (ini - 500).coerceAtLeast(0)
                                    ir(desde); sonar(); desde to fin
                                }
                            },
                            alAnterior = { elegir((e - 1).coerceAtLeast(0)) },
                            alSiguiente = { elegir((e + 1).coerceAtMost(letra.lineas.lastIndex)) },
                            alCerrar = { elegir(null) }
                        )
                    }
                }
            }

            // ── El reproductor ─────────────────────────────────────────────────────────────────
            Reproductor(
                sonando, posicion, duracion, velocidad, compacto = tocando,
                alTocar = { tocar() }, alIr = { ir(it) }, alVelocidad = { cambiarVelocidad() },
                alVistaPrevia = if (letra.conTiempos && !tocando) ({ vistaPrevia = true; sonar() }) else null
            )
            }
        }

        if (mas) ModalBottomSheet(onDismissRequest = { mas = false }, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.padding(bottom = 24.dp)) {
                fun cerrar(accion: () -> Unit) { mas = false; accion() }
                val con = letra.conTiempos
                Text("Exportar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp))
                OpcionDeHoja(Icons.Filled.Download, "Guardar LRC", if (letra.porPalabras) "Con los tiempos de cada palabra" else "Para reproductores y karaoke", con) { cerrar { guardar("lrc") { LetraDeCancion.aLrc(letra, letra.titulo ?: base) } } }
                OpcionDeHoja(Icons.Filled.Download, "Guardar subtítulos SRT", "Para vídeos", con) { cerrar { guardar("srt") { EdicionDeLetra.aSrt(letra, duracion) } } }
                OpcionDeHoja(Icons.Filled.Share, "Compartir", "El LRC, por cualquier app", con) { cerrar { compartirArchivo("$base.lrc", LetraDeCancion.aLrc(letra, letra.titulo ?: base), "Compartir la letra") } }
                HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                Text("Toda la letra", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp, bottom = 4.dp))
                OpcionDeHoja(Icons.Filled.SwapHoriz, "Adelantar o atrasar todo", "Si todas las líneas salen antes o después", con) { cerrar { moverTodo = true } }
                OpcionDeHoja(Icons.Filled.AutoFixHigh, "Rellenar las que faltan", "Reparte las líneas sin tiempo entre sus vecinas", con) {
                    cerrar {
                        val (r, n) = EdicionDeLetra.rellenar(letra)
                        if (n > 0) cambiar(r)
                        Toast.makeText(contexto, if (n > 0) "$n líneas con tiempo nuevo" else "No falta ninguna entre dos con tiempo", Toast.LENGTH_SHORT).show()
                    }
                }
                OpcionDeHoja(Icons.Filled.Info, "Datos de la canción", "Título, artista y álbum del LRC", true) { cerrar { datos = true } }
                OpcionDeHoja(Icons.Filled.DeleteSweep, "Borrar los tiempos", "Para sincronizarla de nuevo", con) {
                    cerrar { cambiar(letra.copy(lineas = letra.lineas.map { it.copy(ms = null, palabras = null) })); confianzas = null; paso = Paso.SINCRONIZAR }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                if (quienLlama == null) OpcionDeHoja(Icons.Filled.FolderOpen, "Abrir otra canción", null, true) { cerrar { elegirAudio.launch(arrayOf("audio/*")) } }
                OpcionDeHoja(Icons.Filled.Settings, "Ajustes", "Reconocedor, retardo al tocar e imán", true) { cerrar { ajustes = true } }
            }
        }

        if (preguntarBajar) AlertDialog(
            onDismissRequest = { preguntarBajar = false },
            title = { Text("Bajar el reconocedor") },
            text = { Text("Para sincronizar sola, LetraSync Pro baja una vez el reconocedor de voz (unos ${MotorWhisper.MODELOS[MotorWhisper.modelo(contexto)] ?: 160} MB). Después funciona sin conexión.") },
            confirmButton = { Button(onClick = { preguntarBajar = false; sincronizarSola() }) { Text("Bajar y sincronizar") } },
            dismissButton = { TextButton(onClick = { preguntarBajar = false }) { Text("Ahora no") } }
        )
        if (moverTodo) MoverTodaLaLetra(
            alAplicar = { d -> moverTodo = false; if (d != 0) cambiar(EdicionDeLetra.desplazar(letra, d)) },
            alCancelar = { moverTodo = false }
        )
        if (ajustes) DialogoDeAjustes { ajustes = false; iman = Preferencias.iman(contexto) }
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

    // ── Piezas ───────────────────────────────────────────────────────────────────────────────────

    /** Los tres pasos, como una pastilla con tres segmentos grandes. */
    @Composable
    private fun Pasos(paso: Paso, hayLetra: Boolean, hayTiempos: Boolean, alElegir: (Paso) -> Unit) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).height(52.dp)
                .clip(RoundedCornerShape(26.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(4.dp)
        ) {
            listOf(Triple(Paso.LETRA, "Letra", true), Triple(Paso.SINCRONIZAR, "Sincronizar", hayLetra), Triple(Paso.AFINAR, "Afinar", hayTiempos)).forEachIndexed { k, (p, t, activo) ->
                val esta = p == paso
                val fondo by animateColorAsState(if (esta) MaterialTheme.colorScheme.primary else Color.Transparent, label = "paso")
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(22.dp)).background(fondo)
                        .clickable(enabled = activo) { alElegir(p) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${k + 1}  $t", fontWeight = if (esta) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp,
                        color = when { esta -> MaterialTheme.colorScheme.onPrimary; activo -> MaterialTheme.colorScheme.onSurface; else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) }
                    )
                }
            }
        }
    }

    /** Paso 1: la letra, con tres botones grandes para traerla. */
    @Composable
    private fun PasoLetra(texto: String, alCambiar: (String) -> Unit, alPegar: () -> Unit, alBuscar: () -> Unit, alAbrir: () -> Unit, alSeguir: () -> Unit) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Baldosa(Icons.Filled.ContentPaste, "Pegar", alPegar)
                Baldosa(Icons.Filled.Search, "Buscar", alBuscar)
                Baldosa(Icons.Filled.FolderOpen, "Archivo", alAbrir)
            }
            TextField(
                texto, alCambiar,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp).clip(RoundedCornerShape(20.dp)),
                placeholder = { Text("Escribe o pega la letra, una línea por verso.\n\nTambién vale un LRC o un SRT.", lineHeight = 22.sp) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                )
            )
            Button(
                onClick = alSeguir, enabled = texto.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(Tema.BOTON), shape = RoundedCornerShape(28.dp)
            ) { Text("Seguir", fontSize = 17.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.ChevronRight, null) }
        }
    }

    @Composable
    private fun RowScope.Baldosa(icono: ImageVector, texto: String, alTocar: () -> Unit) {
        Column(
            Modifier.weight(1f).height(76.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable(onClick = alTocar),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }

    /** Paso 2: tocar al ritmo (por líneas o palabras) o dejar que la oiga sola. */
    @Composable
    private fun PasoSincronizar(
        palabras: Boolean, alPalabras: (Boolean) -> Unit, avance: Float?, bajando: Boolean, yaTiene: Boolean,
        alTocar: () -> Unit, alSola: () -> Unit, alParar: () -> Unit
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // A toques: la principal.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Tema.VIOLETA_FUERTE)))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TouchApp, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Toca al ritmo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (palabras) "Suena la canción y tocas al empezar cada palabra. Para karaoke." else "Suena la canción y tocas al empezar cada línea. Lo más rápido.",
                    color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                )
                // Líneas o palabras: dos segmentos grandes.
                Row(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.18f)).padding(4.dp)) {
                    listOf(false to "Por líneas", true to "Por palabras").forEach { (p, t) ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (p == palabras) Color.White else Color.Transparent).clickable { alPalabras(p) },
                            contentAlignment = Alignment.Center
                        ) { Text(t, color = if (p == palabras) Tema.VIOLETA_FUERTE else Color.White, fontWeight = FontWeight.SemiBold) }
                    }
                }
                Button(
                    onClick = alTocar, enabled = avance == null,
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(Tema.BOTON), shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Tema.VIOLETA_FUERTE)
                ) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(if (yaTiene) "Empezar de nuevo" else "Empezar", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
            // Automática.
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Automática", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text("La app escucha la canción y pone los tiempos sola, sin conexión. Luego repásala en «Afinar».", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
                if (avance != null) {
                    Text(if (bajando) "Bajando el reconocedor… ${(avance * 100).toInt()} %" else "Escuchando… ${(avance * 100).toInt()} %", fontWeight = FontWeight.Medium)
                    LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(8.dp).clip(RoundedCornerShape(4.dp)))
                    OutlinedButton(onClick = alParar, modifier = Modifier.fillMaxWidth().height(Tema.TOQUE), shape = RoundedCornerShape(24.dp)) { Text("Parar") }
                } else FilledTonalButton(onClick = alSola, modifier = Modifier.fillMaxWidth().height(Tema.BOTON), shape = RoundedCornerShape(28.dp)) {
                    Text("Sincronizar sola", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    /**
     * **Tocando**: la línea que toca, grande, y un botón enorme abajo, donde cae el pulgar. También vale tocar en
     * cualquier sitio de la letra. Cada toque vibra un poco.
     */
    @Composable
    private fun PadDeToques(
        porLineas: SincronizadorDeLetra?, porPalabras: SincronizadorPorPalabras?, letra: LetraDeCancion.Letra, posicion: Int, cambio: Int,
        alTocar: () -> Unit, alAtras: () -> Unit, alTerminar: () -> Unit
    ) {
        @Suppress("UNUSED_EXPRESSION") cambio
        val primario = MaterialTheme.colorScheme.primary
        val (hechas, total) = porLineas?.let { it.siguiente to it.textos.size } ?: porPalabras!!.let { it.siguiente to it.orden.size }
        val anterior: String; val actual: AnnotatedString; val siguiente: String
        if (porLineas != null) {
            anterior = porLineas.textos.getOrNull(porLineas.siguiente - 1).orEmpty()
            actual = AnnotatedString(porLineas.textos.getOrNull(porLineas.siguiente) ?: "¡Listo!")
            siguiente = porLineas.textos.getOrNull(porLineas.siguiente + 1).orEmpty()
        } else {
            val (li, pi) = porPalabras!!.actual ?: (letra.lineas.lastIndex to Int.MAX_VALUE)
            val l = letra.lineas.getOrNull(li)
            anterior = letra.lineas.getOrNull(li - 1)?.texto.orEmpty()
            siguiente = letra.lineas.getOrNull(li + 1)?.texto.orEmpty()
            actual = if (l == null) AnnotatedString("¡Listo!") else buildAnnotatedString {
                l.trozos.forEachIndexed { k, w ->
                    if (k > 0) append(' ')
                    when {
                        k == pi -> withStyle(SpanStyle(color = Color.White, background = Tema.CORAL, fontWeight = FontWeight.ExtraBold)) { append(" $w ") }
                        k < pi -> withStyle(SpanStyle(color = primario)) { append(w) }
                        else -> append(w)
                    }
                }
            }
        }
        val fuente = remember { MutableInteractionSource() }
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Barra de avance.
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(progress = { if (total == 0) 0f else hechas.toFloat() / total }, modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)))
                Text("  $hechas / $total", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // La letra (tocar aquí también marca).
            Column(
                Modifier.weight(1f).fillMaxWidth().clickable(interactionSource = fuente, indication = null, onClick = alTocar),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                Text(anterior, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center, maxLines = 2)
                Spacer(Modifier.height(18.dp))
                AnimatedContent(actual, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "linea") { a ->
                    Text(a, fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.height(18.dp))
                Text(siguiente, fontSize = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
            }
            // El botón grande, en la zona del pulgar.
            val pulsado = remember { mutableStateOf(false) }
            val escala by animateFloatAsState(if (pulsado.value) 0.96f else 1f, label = "toque")
            Box(
                Modifier.fillMaxWidth().height(132.dp).padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.verticalGradient(listOf(Tema.CORAL, Color(0xFFFF5F6D))))
                    .pointerInput(Unit) {
                        // Al bajar el dedo, no al levantarlo: así el tiempo es el del toque.
                        awaitEachGesture { awaitFirstDown(); pulsado.value = true; alTocar(); waitForUpOrCancellation(); pulsado.value = false }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                    Text("TOCA", color = Color.White, fontSize = (28 * escala).sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Text(if (porPalabras != null) "al empezar cada palabra" else "al empezar cada línea", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = alAtras, enabled = hechas > 0, modifier = Modifier.weight(1f).height(Tema.BOTON), shape = RoundedCornerShape(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Undo, null); Spacer(Modifier.width(6.dp)); Text("Atrás", fontSize = 16.sp)
                }
                FilledTonalButton(onClick = alTerminar, modifier = Modifier.weight(1f).height(Tema.BOTON), shape = RoundedCornerShape(28.dp)) {
                    Icon(Icons.Filled.Check, null); Spacer(Modifier.width(6.dp)); Text("Terminar", fontSize = 16.sp)
                }
            }
        }
    }

    /** La letra con la línea que suena resaltada y centrada. Tocar una la elige para afinarla. */
    @Composable
    private fun Karaoke(
        letra: LetraDeCancion.Letra, posicion: Int, confianzas: List<Float>?, desordenadas: Set<Int>, elegida: Int?,
        alTocar: (Int) -> Unit
    ) {
        val actual = LetraDeCancion.lineaActual(letra.lineas, posicion)
        val lista = rememberLazyListState()
        LaunchedEffect(actual, elegida) {
            val objetivo = elegida ?: actual
            if (objetivo >= 0) lista.animateScrollToItem((objetivo - 2).coerceAtLeast(0))
        }
        LazyColumn(Modifier.fillMaxSize(), state = lista, contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(letra.lineas) { i, l ->
                val suena = i == actual
                val dudosa = confianzas?.getOrNull(i)?.let { it < 0.4f } == true
                val mal = i in desordenadas
                val esElegida = i == elegida
                val fondo by animateColorAsState(if (esElegida) Tema.CORAL.copy(alpha = 0.16f) else Color.Transparent, label = "fondo")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp).heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(16.dp)).background(fondo).clickable { alTocar(i) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.width(62.dp).clip(RoundedCornerShape(8.dp))
                            .background(
                                when { mal -> Tema.ROJO.copy(alpha = 0.18f); dudosa -> Tema.AMBAR.copy(alpha = 0.18f); esElegida -> Tema.CORAL.copy(alpha = 0.2f); else -> MaterialTheme.colorScheme.surfaceContainerHigh }
                            ).padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            l.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "—", fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                            color = when { mal -> Tema.ROJO; dudosa -> Tema.AMBAR; esElegida -> Tema.CORAL; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (suena && l.palabras != null) conPalabras(l, posicion, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) else AnnotatedString(l.texto),
                        fontSize = if (suena) 22.sp else 18.sp, lineHeight = if (suena) 30.sp else 24.sp,
                        fontWeight = if (suena) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            suena && l.palabras == null -> MaterialTheme.colorScheme.primary
                            suena -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        }
                    )
                }
            }
            item {
                Text(
                    "Toca una línea para afinarla. En la onda, arrastra el asa de una línea para moverla: las demás no se mueven.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), textAlign = TextAlign.Center
                )
            }
        }
    }

    /**
     * Afinar la línea elegida: su tiempo bien grande entre ‹ y ›, −0.1 / +0.1 (mantener repite) y debajo «Oír»,
     * «Aquí» y «Bucle». Solo se mueve esta línea, y nunca más allá de sus vecinas.
     */
    @Composable
    private fun AfinarLinea(
        linea: LetraDeCancion.Linea, numero: Int, total: Int, enBucle: Boolean, fuera: Boolean,
        alMover: (Int) -> Unit, alAqui: () -> Unit, alOir: () -> Unit, alBucle: () -> Unit,
        alAnterior: () -> Unit, alSiguiente: () -> Unit, alCerrar: () -> Unit
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Qué línea es, y cerrar.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Línea $numero de $total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = alCerrar, modifier = Modifier.height(Tema.TOQUE)) { Text("Listo") }
                }
                Text(linea.texto, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                // El tiempo, grande, entre la anterior y la siguiente.
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = alAnterior, enabled = numero > 1, modifier = Modifier.size(Tema.BOTON)) { Icon(Icons.Filled.ChevronLeft, "Anterior", Modifier.size(32.dp)) }
                    Text(
                        linea.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "sin tiempo", fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                        color = if (fuera) Tema.ROJO else Tema.CORAL, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = alSiguiente, enabled = numero < total, modifier = Modifier.size(Tema.BOTON)) { Icon(Icons.Filled.ChevronRight, "Siguiente", Modifier.size(32.dp)) }
                }
                if (fuera) Text("Va antes que la anterior: pulsa «Aquí» en su momento.", fontSize = 12.sp, color = Tema.ROJO, modifier = Modifier.padding(bottom = 4.dp))
                val con = linea.ms != null
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonRepetible("− 0.1 s", con, Modifier.weight(1f)) { alMover(-100) }
                    BotonRepetible("+ 0.1 s", con, Modifier.weight(1f)) { alMover(100) }
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonDeAccion(Icons.Filled.Headphones, "Oír", con, false, Modifier.weight(1f), alOir)
                    BotonDeAccion(Icons.Filled.TouchApp, "Aquí", true, true, Modifier.weight(1.3f), alAqui)
                    BotonDeAccion(Icons.Filled.Repeat, if (enBucle) "En bucle" else "Bucle", con, enBucle, Modifier.weight(1f), alBucle)
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
        val fondo by animateColorAsState(if (pulsado) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerHighest, label = "rep")
        Box(
            modifier.height(Tema.BOTON).clip(RoundedCornerShape(28.dp)).background(fondo)
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
            Text(texto, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }

    @Composable
    private fun BotonDeAccion(icono: ImageVector, texto: String, activo: Boolean, destacado: Boolean, modifier: Modifier, alTocar: () -> Unit) {
        val fondo = if (destacado) Tema.CORAL else MaterialTheme.colorScheme.surfaceContainerHighest
        val tinta = if (destacado) Color.White else MaterialTheme.colorScheme.onSurface
        Column(
            modifier.height(64.dp).clip(RoundedCornerShape(20.dp)).background(if (activo) fondo else fondo.copy(alpha = 0.4f)).clickable(enabled = activo, onClick = alTocar),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, null, tint = tinta.copy(alpha = if (activo) 1f else 0.4f), modifier = Modifier.size(22.dp))
            Text(texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tinta.copy(alpha = if (activo) 1f else 0.4f))
        }
    }

    /** El reproductor: la barra del tiempo y, debajo, play grande con ±5 s, la velocidad y la vista previa. */
    @Composable
    private fun Reproductor(
        sonando: Boolean, posicion: Int, duracion: Int, velocidad: Float, compacto: Boolean,
        alTocar: () -> Unit, alIr: (Int) -> Unit, alVelocidad: () -> Unit, alVistaPrevia: (() -> Unit)?
    ) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 16.dp, vertical = if (compacto) 4.dp else 8.dp)) {
            Slider(
                value = if (duracion > 0) posicion.toFloat() / duracion else 0f,
                onValueChange = { v -> alIr((v * duracion).toInt()) },
                modifier = Modifier.fillMaxWidth().height(32.dp),
                colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            )
            Row(Modifier.fillMaxWidth()) {
                Text(EdicionDeLetra.conDecimas(posicion), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(tiempo(duracion), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!compacto) Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                // La velocidad: una pastilla que va 1× → 0.75× → 0.5×.
                Box(
                    Modifier.size(width = 64.dp, height = Tema.TOQUE).clip(RoundedCornerShape(24.dp))
                        .background(if (velocidad != 1f) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable(onClick = alVelocidad),
                    contentAlignment = Alignment.Center
                ) { Text(if (velocidad == 1f) "1×" else "$velocidad×", fontWeight = FontWeight.Bold, color = if (velocidad != 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                IconButton(onClick = { alIr(posicion - 5000) }, modifier = Modifier.size(Tema.BOTON)) { Icon(Icons.Filled.Replay5, "5 s atrás", Modifier.size(30.dp)) }
                Box(
                    Modifier.size(68.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable(onClick = alTocar),
                    contentAlignment = Alignment.Center
                ) { Icon(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(38.dp)) }
                IconButton(onClick = { alIr(posicion + 5000) }, modifier = Modifier.size(Tema.BOTON)) { Icon(Icons.Filled.Forward5, "5 s adelante", Modifier.size(30.dp)) }
                Box(Modifier.size(width = 64.dp, height = Tema.TOQUE), contentAlignment = Alignment.Center) {
                    if (alVistaPrevia != null) IconButton(onClick = alVistaPrevia, modifier = Modifier.size(Tema.TOQUE)) { Icon(Icons.Filled.Fullscreen, "Vista previa", Modifier.size(28.dp)) }
                }
            } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = alTocar, modifier = Modifier.size(Tema.TOQUE)) { Icon(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir") }
            }
        }
    }

    @Composable
    private fun OpcionDeHoja(icono: ImageVector, titulo: String, detalle: String?, activo: Boolean, alTocar: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(enabled = activo, onClick = alTocar).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = if (activo) 0.15f else 0.06f)), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (activo) 1f else 0.4f), modifier = Modifier.size(22.dp))
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (activo) 1f else 0.4f))
                detalle?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (activo) 1f else 0.5f)) }
            }
        }
    }

    @Composable
    private fun Vacio(icono: ImageVector, titulo: String, texto: String, boton: String, alTocar: () -> Unit) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(listOf(Tema.VIOLETA, Tema.VIOLETA_FUERTE))), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Text(texto, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            Button(onClick = alTocar, modifier = Modifier.fillMaxWidth().height(Tema.BOTON), shape = RoundedCornerShape(28.dp)) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text(boton, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
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
                        fontSize = 40.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Tema.CORAL, modifier = Modifier.padding(vertical = 14.dp)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotonRepetible("Antes", true, Modifier.weight(1f)) { d -= 100 }
                        BotonRepetible("Después", true, Modifier.weight(1f)) { d += 100 }
                    }
                    Text("Mantén pulsado para ir más deprisa.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                }
            },
            confirmButton = { Button(onClick = { alAplicar(d) }, enabled = d != 0) { Text("Aplicar") } },
            dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
        )
    }

    /** Ajustes: el reconocedor, el retardo al tocar y el imán de la onda. */
    @Composable
    private fun DialogoDeAjustes(alCerrar: () -> Unit) {
        val contexto = LocalContext.current
        var modelo by remember { mutableStateOf(MotorWhisper.modelo(contexto)) }
        var retardo by remember { mutableFloatStateOf(Preferencias.retardo(contexto).toFloat()) }
        var iman by remember { mutableStateOf(Preferencias.iman(contexto)) }
        AlertDialog(
            onDismissRequest = alCerrar,
            title = { Text("Ajustes") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Sincronización automática", fontWeight = FontWeight.SemiBold)
                    MotorWhisper.MODELOS.forEach { (m, mb) ->
                        Row(Modifier.fillMaxWidth().heightIn(min = Tema.TOQUE).clickable { modelo = m }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(modelo == m, { modelo = m })
                            Column {
                                Text(when (m) { "tiny" -> "Rápida"; "base" -> "Equilibrada"; else -> "Precisa" } + " · $mb MB")
                                Text(if (MotorWhisper.modeloListo(contexto, m)) "Bajada" else "Se baja la primera vez", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Text("Retardo al tocar: ${retardo.toInt()} ms", fontWeight = FontWeight.SemiBold)
                    Text("Lo que tarda tu dedo. Si las líneas salen tarde, súbelo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(retardo, { retardo = (it / 10).toInt() * 10f }, valueRange = 0f..400f)
                    Row(Modifier.fillMaxWidth().heightIn(min = Tema.TOQUE).clickable { iman = !iman }, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Imán en la onda", fontWeight = FontWeight.SemiBold)
                            Text("Al soltar una línea, se pega a donde empieza el sonido.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(iman, { iman = it })
                    }
                    Text("LetraSync Pro ${com.letrasyncpro.BuildConfig.VERSION_NAME}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    Preferencias.poner(contexto, modelo = modelo); Preferencias.ponerRetardo(contexto, retardo.toInt()); Preferencias.ponerIman(contexto, iman); alCerrar()
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
        )
    }

    /** La biblioteca: tarjetas grandes con la canción, cuántas líneas y si ya está sincronizada. */
    @Composable
    private fun ListaDeLaBiblioteca(alAbrir: (Biblioteca.Cancion) -> Unit, alNueva: () -> Unit) {
        var canciones by remember { mutableStateOf<List<Biblioteca.Cancion>?>(null) }
        var borrar by remember { mutableStateOf<Biblioteca.Cancion?>(null) }
        LaunchedEffect(Unit) { canciones = withContext(Dispatchers.IO) { biblioteca.todas() } }
        val l = canciones
        if (l != null && l.isEmpty()) {
            Vacio(Icons.Filled.LibraryMusic, "Tu biblioteca", "Aquí aparecerán las canciones que sincronices, tal como las dejes.", "Abrir canción", alNueva)
            return
        }
        Column(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(l.orEmpty(), key = { it.id }) { c ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .combinedClickable(onClick = { alAbrir(c) }, onLongClick = { borrar = c }).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                                .background(if (c.conTiempos) Brush.linearGradient(listOf(Tema.VIOLETA, Tema.VIOLETA_FUERTE)) else Brush.linearGradient(listOf(Color(0xFF5A556E), Color(0xFF3A3550)))),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.LibraryMusic, null, tint = Color.White) }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(c.nombre.substringBeforeLast('.').ifBlank { "Canción" }, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                (if (c.conTiempos) "Sincronizada" else "Sin tiempos") + " · ${c.lineas} líneas · " + android.text.format.DateUtils.getRelativeTimeSpanString(c.cuando),
                                fontSize = 13.sp, color = if (c.conTiempos) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Button(onClick = alNueva, modifier = Modifier.fillMaxWidth().padding(16.dp).height(Tema.BOTON), shape = RoundedCornerShape(28.dp)) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Abrir canción", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        borrar?.let { c ->
            AlertDialog(
                onDismissRequest = { borrar = null },
                title = { Text("Quitar de la biblioteca") },
                text = { Text("«${c.nombre.substringBeforeLast('.')}» se quita de la lista. La canción no se borra del teléfono.") },
                confirmButton = { Button(onClick = { biblioteca.borrar(c.id); canciones = canciones?.filter { it.id != c.id }; borrar = null }) { Text("Quitar") } },
                dismissButton = { TextButton(onClick = { borrar = null }) { Text("Cancelar") } }
            )
        }
    }

    companion object {
        fun tiempo(ms: Int): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }
    }
}
