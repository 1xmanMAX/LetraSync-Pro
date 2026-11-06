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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.letrasyncpro.motor.Preferencias
import com.letrasyncpro.motor.SincronizadorDeLetra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **LetraSync Pro**: la canción, su letra y sus tiempos.
 *
 * - **Sincronizar sola**: Whisper oye la canción en el teléfono y la letra pegada se alinea con lo oído
 *   ([AlinearLetra]). El texto que se guarda es siempre el pegado.
 * - **A toques**: con la canción sonando (a la velocidad que se elija), un toque al empezar cada línea.
 * - **Afinar** ([EdicionDeLetra]): cada línea por décimas, «ahora», oírla desde 2 s antes; mover toda la letra;
 *   rellenar las que quedaron sin tiempo; las desordenadas en rojo y las dudosas en ámbar; deshacer y rehacer.
 * - **Sacar**: LRC (con centésimas), SRT y la letra para PinDesk.
 * - **Biblioteca** ([Biblioteca]): las canciones recientes, con la letra como se dejó.
 * - **Desde PinDesk** ([Integracion]): llega la canción y su letra, y «Guardar en PinDesk» la devuelve sincronizada.
 */
class LetraSyncActivity : ComponentActivity() {

    private var reproductor: MediaPlayer? = null
    private val biblioteca by lazy { Biblioteca(File(filesDir, "biblioteca")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val desdePinDesk = intent?.action == Integracion.ACCION
        val audioInicial = intent?.data ?: (intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        val letraInicial = intent?.getStringExtra(Integracion.EXTRA_LETRA).orEmpty()
        val titulo = intent?.getStringExtra(Integracion.EXTRA_TITULO)
        val quien = intent?.getStringExtra(Integracion.EXTRA_QUIEN_LLAMA) ?: "PinDesk"
        setContent {
            val oscuro = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (oscuro) darkColorScheme(primary = MORADO_CLARO) else lightColorScheme(primary = MORADO)) {
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

    private val cancelar = AtomicBoolean(false)

    private fun nombreDe(uri: Uri): String = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Canción"

    /** La canción, copiada a la caché: el reconocedor lee un archivo. */
    private fun copiaDe(uri: Uri): File? = runCatching {
        val f = File(cacheDir, "cancion")
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Pantalla(audioInicial: Uri?, letraInicial: String, tituloInicial: String?, quienLlama: String?) {
        val contexto = LocalContext.current
        val alcance = rememberCoroutineScope()
        var audio by rememberSaveable { mutableStateOf(audioInicial) }
        var nombre by rememberSaveable { mutableStateOf(tituloInicial ?: audioInicial?.let { nombreDe(it) }.orEmpty()) }
        var texto by rememberSaveable { mutableStateOf(letraInicial) }
        var editando by rememberSaveable { mutableStateOf(LetraDeCancion.leer(letraInicial).lineas.isEmpty() && audioInicial != null) }
        var verBiblioteca by rememberSaveable { mutableStateOf(audioInicial == null && quienLlama == null) }
        var sonando by remember { mutableStateOf(false) }
        var posicion by remember { mutableIntStateOf(0) }
        var duracion by remember { mutableIntStateOf(0) }
        var velocidad by rememberSaveable { mutableFloatStateOf(1f) }
        var avance by remember { mutableStateOf<Float?>(null) }
        var bajando by remember { mutableStateOf(false) }
        var confianzas by remember { mutableStateOf<List<Float>?>(null) }
        var preguntarBajar by remember { mutableStateOf(false) }
        var aToques by remember { mutableStateOf<SincronizadorDeLetra?>(null) }
        var marcadas by remember { mutableIntStateOf(0) }
        var elegida by remember { mutableStateOf<Int?>(null) }
        var menu by remember { mutableStateOf(false) }
        var moverTodo by remember { mutableStateOf(false) }
        var ajustes by remember { mutableStateOf(false) }
        val historial = remember { Historial() }
        var pasos by remember { mutableIntStateOf(0) } // para que deshacer/rehacer se repinten

        // Lo escrito a mano cuenta como un solo paso al terminar de editar.
        var antesDeEditar by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(editando) {
            if (editando) antesDeEditar = texto
            else antesDeEditar?.let { if (it != texto) { historial.apuntar(it); pasos++ }; antesDeEditar = null }
        }

        val letra = remember(texto) { LetraDeCancion.leer(texto) }
        val desordenadas = remember(letra) { EdicionDeLetra.desordenadas(letra) }

        /** Todo cambio de la letra pasa por aquí: se apunta para deshacerlo. */
        fun cambiar(nueva: LetraDeCancion.Letra) {
            val t = EdicionDeLetra.texto(nueva)
            if (t == texto) return
            historial.apuntar(texto); texto = t; pasos++
        }
        fun deshacer() { historial.deshacer(texto)?.let { texto = it; pasos++ } }
        fun rehacer() { historial.rehacer(texto)?.let { texto = it; pasos++ } }

        // El reproductor, al tener canción.
        LaunchedEffect(audio) {
            reproductor?.release(); reproductor = null; sonando = false; posicion = 0; duracion = 0
            val u = audio ?: return@LaunchedEffect
            reproductor = runCatching {
                MediaPlayer().apply { setDataSource(contexto, u); prepare(); setOnCompletionListener { sonando = false } }
            }.getOrNull()
            duracion = reproductor?.duration ?: 0
            if (reproductor == null) Toast.makeText(contexto, "No se puede reproducir esta canción", Toast.LENGTH_SHORT).show()
        }
        LaunchedEffect(sonando) {
            while (sonando) { posicion = reproductor?.currentPosition ?: 0; delay(50) }
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
        fun ir(ms: Int) { reproductor?.seekTo(ms.coerceIn(0, duracion.coerceAtLeast(0))); posicion = ms.coerceIn(0, duracion.coerceAtLeast(0)) }
        fun oirDesde(ms: Int) { ir((ms - 2000).coerceAtLeast(0)); if (reproductor?.isPlaying != true) tocar() }
        fun ponerVelocidad(v: Float) {
            velocidad = v
            reproductor?.takeIf { it.isPlaying }?.let { r -> runCatching { r.playbackParams = r.playbackParams.setSpeed(v) } }
        }

        val elegirAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) {
                runCatching { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                audio = u; nombre = nombreDe(u); verBiblioteca = false
                val guardada = biblioteca.de(u.toString())
                if (guardada != null) { texto = guardada.letra; editando = false }
                else { texto = ""; editando = true }
                confianzas = null; elegida = null
            }
        }
        val elegirLetra = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) runCatching { contentResolver.openInputStream(u)?.use { it.readBytes().decodeToString() } }.getOrNull()?.let {
                historial.apuntar(texto); texto = it; pasos++; editando = false
            }
        }
        fun guardarComo(ext: String, contenido: () -> String) = Pair(ext, contenido)
        var aGuardar by remember { mutableStateOf<Pair<String, () -> String>?>(null) }
        val guardarArchivo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { u ->
            val g = aGuardar; aGuardar = null
            if (u != null && g != null) runCatching {
                contentResolver.openOutputStream(u)?.use { it.write(g.second().toByteArray()) }
                Toast.makeText(contexto, "Guardada", Toast.LENGTH_SHORT).show()
            }
        }
        val base = nombre.substringBeforeLast('.').ifBlank { "letra" }
        fun guardar(ext: String, contenido: () -> String) { aGuardar = guardarComo(ext, contenido); guardarArchivo.launch("$base.$ext") }

        fun sincronizarSola() {
            val u = audio ?: return
            val lineas = AlinearLetra.lineasDeLaLetra(texto)
            if (lineas.isEmpty()) { Toast.makeText(contexto, "Pega antes la letra", Toast.LENGTH_SHORT).show(); return }
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
                                cambiar(LetraDeCancion.leer(r.texto)); editando = false
                                confianzas = r.alineado.lineas.map { if (it.anclada) it.confianza else 0f }
                            }
                            val n = r.alineado.ancladas; val t = r.alineado.lineas.size
                            if (r.alineado.aOjo) "Sincronizada: $n de $t líneas se oyeron; el resto va a ojo (revísalas en ámbar)"
                            else "Sincronizada: $n de $t líneas se oyeron"
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

        fun empezarAToques(desde: Int = 0) {
            val s = SincronizadorDeLetra.de(letra)
            if (s.textos.isEmpty()) { Toast.makeText(contexto, "Pega antes la letra", Toast.LENGTH_SHORT).show(); return }
            aToques = s; marcadas = 0; confianzas = null; editando = false; elegida = null
            ir(desde); if (reproductor?.isPlaying != true) tocar()
        }
        fun acabarAToques() {
            val s = aToques ?: return
            aToques = null
            cambiar(s.letra())
            reproductor?.pause(); sonando = false
        }
        fun ponerTiempo(i: Int, ms: Int) {
            cambiar(EdicionDeLetra.poner(letra, i, ms))
            confianzas = confianzas?.toMutableList()?.also { if (i in it.indices) it[i] = 1f }
        }
        fun devolver() {
            setResult(Activity.RESULT_OK, Intent()
                .putExtra(Integracion.EXTRA_LETRA, LetraDeCancion.aGuardar(letra))
                .putExtra(Integracion.EXTRA_LRC, LetraDeCancion.aLrc(letra)))
            finish()
        }

        BackHandler(enabled = elegida != null || aToques != null || (verBiblioteca && audio != null)) {
            when {
                elegida != null -> elegida = null
                aToques != null -> acabarAToques()
                else -> verBiblioteca = false
            }
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // Cabecera: la canción y lo que se hace con ella.
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MORADO), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LibraryMusic, null, tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp).clickable(enabled = quienLlama == null) { elegirAudio.launch(arrayOf("audio/*")) }) {
                    Text(if (nombre.isBlank()) "LetraSync Pro" else nombre.substringBeforeLast('.'), fontWeight = FontWeight.SemiBold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        when {
                            quienLlama != null -> "Desde $quienLlama"
                            audio == null -> "Elige una canción"
                            else -> "${tiempo(duracion)} · ${letra.lineas.size} líneas · ${letra.lineas.count { it.ms != null }} con tiempo"
                        },
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                    )
                }
                @Suppress("UNUSED_EXPRESSION") pasos
                IconButton(onClick = { deshacer() }, enabled = historial.puedeDeshacer && aToques == null) { Icon(Icons.AutoMirrored.Filled.Undo, "Deshacer") }
                IconButton(onClick = { rehacer() }, enabled = historial.puedeRehacer && aToques == null) { Icon(Icons.AutoMirrored.Filled.Redo, "Rehacer") }
                if (quienLlama == null) IconButton(onClick = { verBiblioteca = !verBiblioteca }) { Icon(Icons.AutoMirrored.Filled.QueueMusic, "Biblioteca") }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "Más") }
                    DropdownMenu(menu, { menu = false }) {
                        if (quienLlama == null) DropdownMenuItem({ Text("Abrir otra canción") }, { menu = false; elegirAudio.launch(arrayOf("audio/*")) })
                        DropdownMenuItem({ Text("Editar el texto") }, { menu = false; editando = true; verBiblioteca = false })
                        DropdownMenuItem({ Text("Abrir un .lrc / .txt") }, { menu = false; elegirLetra.launch(arrayOf("text/*", "application/octet-stream")) })
                        HorizontalDivider()
                        DropdownMenuItem({ Text("Mover toda la letra…") }, { menu = false; moverTodo = true }, enabled = letra.conTiempos)
                        DropdownMenuItem({ Text("Rellenar las que no tienen tiempo") }, {
                            menu = false
                            val (r, n) = EdicionDeLetra.rellenar(letra)
                            if (n > 0) cambiar(r)
                            Toast.makeText(contexto, if (n > 0) "$n líneas repartidas entre sus vecinas" else "No hay huecos entre líneas con tiempo", Toast.LENGTH_SHORT).show()
                        }, enabled = letra.conTiempos)
                        DropdownMenuItem({ Text("Quitar todos los tiempos") }, {
                            menu = false; cambiar(letra.copy(lineas = letra.lineas.map { it.copy(ms = null) })); confianzas = null
                        }, enabled = letra.conTiempos)
                        HorizontalDivider()
                        DropdownMenuItem({ Text("Guardar LRC") }, { menu = false; guardar("lrc") { LetraDeCancion.aLrc(letra, base) } }, enabled = letra.conTiempos)
                        DropdownMenuItem({ Text("Guardar subtítulos SRT") }, { menu = false; guardar("srt") { EdicionDeLetra.aSrt(letra, duracion) } }, enabled = letra.conTiempos)
                        DropdownMenuItem({ Text("Compartir LRC") }, { menu = false; compartirArchivo("$base.lrc", LetraDeCancion.aLrc(letra, base), "Compartir la letra") }, enabled = letra.conTiempos)
                        DropdownMenuItem({ Text("Compartir SRT") }, { menu = false; compartirArchivo("$base.srt", EdicionDeLetra.aSrt(letra, duracion), "Compartir los subtítulos") }, enabled = letra.conTiempos)
                        HorizontalDivider()
                        DropdownMenuItem({ Text("Ajustes") }, { menu = false; ajustes = true }, leadingIcon = { Icon(Icons.Filled.Settings, null) })
                    }
                }
            }

            // El reproductor.
            if (audio != null && !verBiblioteca) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { ir(posicion - 5000) }) { Icon(Icons.Filled.Replay5, "5 s atrás") }
                    IconButton(onClick = { tocar() }) { Icon(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir") }
                    IconButton(onClick = { ir(posicion + 5000) }) { Icon(Icons.Filled.Forward5, "5 s adelante") }
                    Slider(
                        value = if (duracion > 0) posicion.toFloat() / duracion else 0f,
                        onValueChange = { v -> ir((v * duracion).toInt()) },
                        modifier = Modifier.weight(1f)
                    )
                    Text(EdicionDeLetra.conDecimas(posicion), fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, end = 4.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Velocidad", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VELOCIDADES.forEach { v ->
                        val esta = v == velocidad
                        AssistChip(
                            onClick = { ponerVelocidad(v) },
                            label = { Text(if (v == 1f) "1×" else "$v×", fontWeight = if (esta) FontWeight.Bold else FontWeight.Normal, color = if (esta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                        )
                    }
                }
            }

            // El avance de la sincronización.
            avance?.let { a ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(if (bajando) "Bajando el reconocedor de voz… ${(a * 100).toInt()} %" else "Escuchando la canción… ${(a * 100).toInt()} %", fontSize = 13.sp)
                    LinearProgressIndicator(progress = { a }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    TextButton(onClick = { cancelar.set(true) }) { Text("Parar") }
                }
            }

            // La letra: escribiéndola, a toques, en karaoke o la biblioteca.
            Box(Modifier.weight(1f).fillMaxWidth()) {
                val s = aToques
                if (verBiblioteca) {
                    ListaDeLaBiblioteca(
                        alAbrir = { c ->
                            val u = Uri.parse(c.audio)
                            audio = u; nombre = c.nombre; texto = c.letra; editando = false; verBiblioteca = false
                            confianzas = null; elegida = null
                        },
                        alNueva = { elegirAudio.launch(arrayOf("audio/*")) }
                    )
                } else if (editando) {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            texto, { texto = it }, modifier = Modifier.fillMaxWidth().weight(1f),
                            placeholder = { Text("Pega aquí la letra de la canción, una línea por renglón (o un .lrc)") }
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                val cm = getSystemService(android.content.ClipboardManager::class.java)
                                cm?.primaryClip?.getItemAt(0)?.coerceToText(contexto)?.toString()?.let { historial.apuntar(texto); texto = it; pasos++ }
                            }) { Icon(Icons.Filled.ContentPaste, null); Spacer(Modifier.width(6.dp)); Text("Pegar") }
                            FilledTonalButton(onClick = { elegirLetra.launch(arrayOf("text/*", "application/octet-stream")) }) { Text("Abrir archivo") }
                            Spacer(Modifier.weight(1f))
                            Button(onClick = { editando = false }, enabled = texto.isNotBlank()) { Text("Listo") }
                        }
                    }
                } else if (s != null) {
                    // A toques: la que toca, grande; un toque en cualquier sitio la marca.
                    Column(
                        Modifier.fillMaxSize().combinedClickable(onClick = {
                            if (s.marcar(posicion)) marcadas++ else { marcadas++; acabarAToques() }
                        }).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        @Suppress("UNUSED_EXPRESSION") marcadas
                        Text("Toca cuando empiece cada línea", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        if (velocidad != 1f) Text("A $velocidad× — los tiempos se guardan los de la canción", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
                        Text(s.textos.getOrNull(s.siguiente - 1)?.let { "✓ $it" } ?: "", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(Modifier.height(10.dp))
                        Text(s.textos.getOrNull(s.siguiente) ?: "", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(10.dp))
                        Text(s.textos.getOrNull(s.siguiente + 1) ?: "", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        LinearProgressIndicator(progress = { if (s.textos.isEmpty()) 0f else s.siguiente.toFloat() / s.textos.size }, modifier = Modifier.width(200.dp))
                        Text("${s.siguiente} de ${s.textos.size}", fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            TextButton(onClick = { s.deshacer()?.let { ir(it) }; marcadas++ }) { Icon(Icons.AutoMirrored.Filled.Undo, null); Text(" Atrás") }
                            TextButton(onClick = { acabarAToques() }) { Text("Terminar") }
                        }
                    }
                } else {
                    Karaoke(
                        letra, posicion, confianzas, desordenadas, elegida,
                        alTocar = { i, ms -> if (ms != null) ir(ms); elegida = i },
                        alMantener = { i -> ponerTiempo(i, posicion); elegida = i }
                    )
                }
            }

            // Afinar la línea elegida.
            val e = elegida
            if (e != null && aToques == null && !editando && !verBiblioteca && e in letra.lineas.indices) {
                AfinarLinea(
                    linea = letra.lineas[e], numero = e + 1,
                    alMover = { d -> cambiar(EdicionDeLetra.ajustar(letra, e, d)) },
                    alAhora = { ponerTiempo(e, posicion) },
                    alOir = { letra.lineas[e].ms?.let { oirDesde(it) } },
                    alQuitar = { cambiar(EdicionDeLetra.quitar(letra, e)) },
                    alAnterior = { elegida = (e - 1).coerceAtLeast(0) },
                    alSiguiente = { elegida = (e + 1).coerceAtMost(letra.lineas.lastIndex) },
                    alCerrar = { elegida = null }
                )
            }

            // Lo que se hace con la letra.
            if (aToques == null && !editando && !verBiblioteca && elegida == null) Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { if (!MotorWhisper.modeloListo(contexto)) preguntarBajar = true else sincronizarSola() }, enabled = audio != null && avance == null) {
                    Icon(Icons.Filled.AutoAwesome, null); Spacer(Modifier.width(6.dp)); Text("Sola")
                }
                FilledTonalButton(onClick = { empezarAToques() }, enabled = audio != null && avance == null) {
                    Icon(Icons.Filled.TouchApp, null); Spacer(Modifier.width(6.dp)); Text("A toques")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { editando = true }) { Icon(Icons.Filled.Edit, "Editar la letra") }
            }
            if (quienLlama != null && aToques == null && !editando) Button(
                onClick = { devolver() }, enabled = letra.lineas.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            ) { Icon(Icons.Filled.Check, null); Spacer(Modifier.width(6.dp)); Text("Guardar en $quienLlama") }
        }

        if (preguntarBajar) AlertDialog(
            onDismissRequest = { preguntarBajar = false },
            title = { Text("Bajar el reconocedor") },
            text = { Text("Para sincronizar sola, LetraSync Pro baja una vez el reconocedor de voz (unos ${MotorWhisper.MODELOS[MotorWhisper.modelo(contexto)] ?: 160} MB). Después funciona sin conexión.") },
            confirmButton = { TextButton(onClick = { preguntarBajar = false; sincronizarSola() }) { Text("Bajar y sincronizar") } },
            dismissButton = { TextButton(onClick = { preguntarBajar = false }) { Text("Ahora no") } }
        )
        if (moverTodo) MoverTodaLaLetra(
            alAplicar = { d -> moverTodo = false; if (d != 0) cambiar(EdicionDeLetra.desplazar(letra, d)) },
            alCancelar = { moverTodo = false }
        )
        if (ajustes) DialogoDeAjustes { ajustes = false }
    }

    /** El karaoke: la que suena resaltada y en el centro; tocar salta y la elige para afinar, mantener le pone el tiempo de ahora. */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Karaoke(
        letra: LetraDeCancion.Letra, posicion: Int, confianzas: List<Float>?, desordenadas: Set<Int>, elegida: Int?,
        alTocar: (Int, Int?) -> Unit, alMantener: (Int) -> Unit
    ) {
        val actual = LetraDeCancion.lineaActual(letra.lineas, posicion)
        val lista = rememberLazyListState()
        LaunchedEffect(actual) { if (actual >= 0 && elegida == null) lista.animateScrollToItem((actual - 2).coerceAtLeast(0)) }
        if (letra.lineas.isEmpty()) {
            Text("Sin letra. Tócala con el lápiz para pegarla.", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        LazyColumn(Modifier.fillMaxSize(), state = lista) {
            itemsIndexed(letra.lineas) { i, l ->
                val suena = i == actual
                val dudosa = confianzas?.getOrNull(i)?.let { it < 0.4f } == true
                val mal = i in desordenadas
                val color = when { mal -> ROJO; dudosa -> AMBAR; else -> null }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 44.dp)
                        .background(if (i == elegida) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .combinedClickable(onClick = { alTocar(i, l.ms) }, onLongClick = { alMantener(i) })
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        l.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "—", fontSize = 12.sp, modifier = Modifier.width(56.dp),
                        color = color ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        l.texto, fontSize = if (suena) 21.sp else 17.sp,
                        fontWeight = if (suena) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            suena -> MaterialTheme.colorScheme.primary
                            color != null -> color
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            item {
                Text(
                    "Toca una línea para ir a ella y afinarla; mantenla para ponerle el tiempo de ahora. En ámbar, las que se pusieron a ojo; en rojo, las que van antes que la anterior.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(18.dp)
                )
            }
        }
    }

    /** La barra para afinar una línea: décimas, «ahora», oírla y pasar a la vecina. */
    @Composable
    private fun AfinarLinea(
        linea: LetraDeCancion.Linea, numero: Int,
        alMover: (Int) -> Unit, alAhora: () -> Unit, alOir: () -> Unit, alQuitar: () -> Unit,
        alAnterior: () -> Unit, alSiguiente: () -> Unit, alCerrar: () -> Unit
    ) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Línea $numero · ${linea.ms?.let { EdicionDeLetra.conDecimas(it) } ?: "sin tiempo"}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("  " + linea.texto, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = alCerrar) { Icon(Icons.Filled.Close, "Cerrar") }
                }
                val conTiempo = linea.ms != null
                val pequeno = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { alMover(-500) }, enabled = conTiempo, contentPadding = pequeno) { Text("−0.5") }
                    OutlinedButton(onClick = { alMover(-100) }, enabled = conTiempo, contentPadding = pequeno) { Text("−0.1") }
                    OutlinedButton(onClick = { alMover(100) }, enabled = conTiempo, contentPadding = pequeno) { Text("+0.1") }
                    OutlinedButton(onClick = { alMover(500) }, enabled = conTiempo, contentPadding = pequeno) { Text("+0.5") }
                    FilledTonalButton(onClick = alAhora, contentPadding = pequeno) { Text("Ahora") }
                    FilledTonalButton(onClick = alOir, enabled = conTiempo, contentPadding = pequeno) { Icon(Icons.Filled.Headphones, null, Modifier.size(18.dp)); Text(" Oír") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = alAnterior) { Text("‹ Anterior") }
                    TextButton(onClick = alSiguiente) { Text("Siguiente ›") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = alQuitar, enabled = conTiempo) { Icon(Icons.Filled.Delete, null, Modifier.size(18.dp)); Text(" Quitar tiempo") }
                }
            }
        }
    }

    /** Mover toda la letra: para la canción que va adelantada o atrasada respecto a la letra. */
    @Composable
    private fun MoverTodaLaLetra(alAplicar: (Int) -> Unit, alCancelar: () -> Unit) {
        var d by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = alCancelar,
            title = { Text("Mover toda la letra") },
            text = {
                Column {
                    Text("Si todas las líneas salen antes o después de lo que se canta, muévelas a la vez.", fontSize = 14.sp)
                    Text(
                        (if (d > 0) "+" else if (d < 0) "−" else "") + String.format(java.util.Locale.ROOT, "%.1f s", kotlin.math.abs(d) / 1000f),
                        fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp).align(Alignment.CenterHorizontally)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf(-1000, -100, 100, 1000).forEach { p ->
                            OutlinedButton(onClick = { d += p }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text((if (p > 0) "+" else "−") + (kotlin.math.abs(p) / 1000f).toString()) }
                        }
                    }
                    Text(if (d < 0) "Las líneas saldrán antes." else if (d > 0) "Las líneas saldrán después." else "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = { TextButton(onClick = { alAplicar(d) }, enabled = d != 0) { Text("Mover") } },
            dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
        )
    }

    /** Ajustes: el tamaño del reconocedor (más grande, más acierta y más tarda). */
    @Composable
    private fun DialogoDeAjustes(alCerrar: () -> Unit) {
        val contexto = LocalContext.current
        var modelo by remember { mutableStateOf(MotorWhisper.modelo(contexto)) }
        AlertDialog(
            onDismissRequest = alCerrar,
            title = { Text("Ajustes") },
            text = {
                Column {
                    Text("Reconocedor de voz para «Sola»", fontWeight = FontWeight.SemiBold)
                    MotorWhisper.MODELOS.forEach { (m, mb) ->
                        Row(Modifier.fillMaxWidth().clickable { modelo = m }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(modelo == m, { modelo = m })
                            Column {
                                Text(when (m) { "tiny" -> "Rápido"; "base" -> "Equilibrado"; else -> "Preciso" } + " ($m, $mb MB)")
                                Text(
                                    (if (MotorWhisper.modeloListo(contexto, m)) "Bajado" else "Se baja la primera vez") +
                                        when (m) { "tiny" -> " · se equivoca más"; "small" -> " · tarda más"; else -> "" },
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Text("LetraSync Pro ${com.letrasyncpro.BuildConfig.VERSION_NAME}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
            },
            confirmButton = { TextButton(onClick = { Preferencias.poner(contexto, modelo = modelo); alCerrar() }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
        )
    }

    /** La biblioteca: las canciones recientes con cuántas líneas y si ya tienen tiempos. */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ListaDeLaBiblioteca(alAbrir: (Biblioteca.Cancion) -> Unit, alNueva: () -> Unit) {
        var canciones by remember { mutableStateOf<List<Biblioteca.Cancion>?>(null) }
        var borrar by remember { mutableStateOf<Biblioteca.Cancion?>(null) }
        LaunchedEffect(Unit) { canciones = withContext(Dispatchers.IO) { biblioteca.todas() } }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Biblioteca", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                Button(onClick = alNueva) { Text("Abrir canción") }
            }
            val l = canciones
            if (l != null && l.isEmpty()) Text(
                "Aquí aparecen las canciones que sincronices, con la letra como la dejaste.\n\nAbre una canción, pega su letra y sincronízala sola o a toques.",
                modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(l.orEmpty(), key = { it.id }) { c ->
                    Row(
                        Modifier.fillMaxWidth().combinedClickable(onClick = { alAbrir(c) }, onLongClick = { borrar = c }).padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(MORADO.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LibraryMusic, null, tint = MORADO)
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(c.nombre.substringBeforeLast('.').ifBlank { "Canción" }, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${c.lineas} líneas · " + (if (c.conTiempos) "sincronizada" else "sin tiempos") + " · " +
                                    android.text.format.DateUtils.getRelativeTimeSpanString(c.cuando),
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        borrar?.let { c ->
            AlertDialog(
                onDismissRequest = { borrar = null },
                title = { Text("Quitar de la biblioteca") },
                text = { Text("«${c.nombre.substringBeforeLast('.')}» se quita de la lista (la canción no se borra del teléfono).") },
                confirmButton = { TextButton(onClick = { biblioteca.borrar(c.id); canciones = canciones?.filter { it.id != c.id }; borrar = null }) { Text("Quitar") } },
                dismissButton = { TextButton(onClick = { borrar = null }) { Text("Cancelar") } }
            )
        }
    }

    companion object {
        val MORADO = Color(0xFF6C4DFF)
        val MORADO_CLARO = Color(0xFFB4A3FF)
        val AMBAR = Color(0xFFFFA000)
        val ROJO = Color(0xFFE53935)
        val VELOCIDADES = listOf(0.5f, 0.75f, 1f)
        fun tiempo(ms: Int): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }
    }
}
