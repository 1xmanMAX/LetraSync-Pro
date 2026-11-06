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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.letrasyncpro.motor.LetraDeCancion
import com.letrasyncpro.motor.MotorWhisper
import com.letrasyncpro.motor.OirCancion
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
 *   ([AlinearLetra], el mismo cálculo que PinDesk y el PC: con lo mismo oído, los mismos minutos). El texto que se
 *   guarda es siempre el pegado.
 * - **A toques**: con la canción sonando, un toque al empezar cada línea ([SincronizadorDeLetra]).
 * - **El karaoke**: la línea que suena resaltada; tocar una salta a ella; mantenerla le pone el tiempo de ahora
 *   (para corregir la que cayó mal). Las que se pusieron «a ojo» van en ámbar.
 * - **Desde PinDesk** ([Integracion]): llega la canción y su letra, y «Guardar en PinDesk» la devuelve sincronizada.
 */
class LetraSyncActivity : ComponentActivity() {

    private var reproductor: MediaPlayer? = null

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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Pantalla(audioInicial: Uri?, letraInicial: String, tituloInicial: String?, quienLlama: String?) {
        val contexto = LocalContext.current
        val alcance = rememberCoroutineScope()
        var audio by rememberSaveable { mutableStateOf(audioInicial) }
        var nombre by rememberSaveable { mutableStateOf(tituloInicial ?: audioInicial?.let { nombreDe(it) }.orEmpty()) }
        var texto by rememberSaveable { mutableStateOf(letraInicial) }
        var editando by rememberSaveable { mutableStateOf(LetraDeCancion.leer(letraInicial).lineas.isEmpty()) }
        var sonando by remember { mutableStateOf(false) }
        var posicion by remember { mutableIntStateOf(0) }
        var duracion by remember { mutableIntStateOf(0) }
        var avance by remember { mutableStateOf<Float?>(null) }
        var bajando by remember { mutableStateOf(false) }
        var confianzas by remember { mutableStateOf<List<Float>?>(null) }
        var preguntarBajar by remember { mutableStateOf(false) }
        var aToques by remember { mutableStateOf<SincronizadorDeLetra?>(null) }
        var marcadas by remember { mutableIntStateOf(0) }

        val letra = remember(texto) { LetraDeCancion.leer(texto) }

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
            while (sonando) { posicion = reproductor?.currentPosition ?: 0; delay(80) }
            posicion = reproductor?.currentPosition ?: posicion
        }
        fun tocar() {
            val r = reproductor ?: return
            if (r.isPlaying) { r.pause(); sonando = false } else { r.start(); sonando = true }
        }
        fun ir(ms: Int) { reproductor?.seekTo(ms); posicion = ms }

        val elegirAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) {
                runCatching { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                audio = u; nombre = nombreDe(u)
            }
        }
        val elegirLetra = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u != null) runCatching { contentResolver.openInputStream(u)?.use { it.readBytes().decodeToString() } }.getOrNull()?.let { texto = it; editando = false }
        }
        val guardarLrc = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { u ->
            if (u != null) runCatching {
                contentResolver.openOutputStream(u)?.use { it.write(LetraDeCancion.aLrc(letra, nombre.substringBeforeLast('.')).toByteArray()) }
                Toast.makeText(contexto, "Guardada", Toast.LENGTH_SHORT).show()
            }
        }

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
                                texto = r.texto; editando = false
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

        fun empezarAToques() {
            val s = SincronizadorDeLetra.de(letra)
            if (s.textos.isEmpty()) { Toast.makeText(contexto, "Pega antes la letra", Toast.LENGTH_SHORT).show(); return }
            aToques = s; marcadas = 0; confianzas = null; editando = false
            ir(0); if (reproductor?.isPlaying != true) tocar()
        }
        fun acabarAToques() {
            val s = aToques ?: return
            texto = LetraDeCancion.aGuardar(s.letra()); aToques = null
            reproductor?.pause(); sonando = false
        }
        fun ponerTiempo(i: Int, ms: Int) {
            val nuevas = letra.lineas.mapIndexed { k, l -> if (k == i) l.copy(ms = ms) else l }
            texto = LetraDeCancion.aGuardar(letra.copy(lineas = nuevas))
            confianzas = confianzas?.toMutableList()?.also { if (i in it.indices) it[i] = 1f }
        }
        fun compartir() {
            val f = File(File(cacheDir, "share").apply { mkdirs() }, (nombre.substringBeforeLast('.').ifBlank { "letra" }) + ".lrc")
            f.writeText(LetraDeCancion.aLrc(letra, nombre.substringBeforeLast('.')))
            val u = FileProvider.getUriForFile(contexto, "$packageName.fileprovider", f)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_STREAM, u)
                .putExtra(Intent.EXTRA_TEXT, LetraDeCancion.aLrc(letra)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .apply { clipData = ClipData.newRawUri(f.name, u) }, "Compartir la letra"))
        }
        fun devolver() {
            setResult(Activity.RESULT_OK, Intent()
                .putExtra(Integracion.EXTRA_LETRA, LetraDeCancion.aGuardar(letra))
                .putExtra(Integracion.EXTRA_LRC, LetraDeCancion.aLrc(letra)))
            finish()
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // Cabecera: la canción y lo que se hace con ella.
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MORADO), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LibraryMusic, null, tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(if (nombre.isBlank()) "LetraSync Pro" else nombre.substringBeforeLast('.'), fontWeight = FontWeight.SemiBold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (quienLlama != null) "Desde $quienLlama" else if (audio == null) "Elige una canción" else tiempo(duracion),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { elegirAudio.launch(arrayOf("audio/*")) }) { Text(if (audio == null) "Elegir canción" else "Cambiar") }
            }

            // El reproductor.
            if (audio != null) Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { tocar() }) { Icon(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir") }
                Slider(
                    value = if (duracion > 0) posicion.toFloat() / duracion else 0f,
                    onValueChange = { v -> ir((v * duracion).toInt()) },
                    modifier = Modifier.weight(1f)
                )
                Text(tiempo(posicion), fontSize = 12.sp, modifier = Modifier.width(48.dp).padding(start = 6.dp))
            }

            // El avance de la sincronización.
            avance?.let { a ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(if (bajando) "Bajando el reconocedor de voz… ${(a * 100).toInt()} %" else "Escuchando la canción… ${(a * 100).toInt()} %", fontSize = 13.sp)
                    LinearProgressIndicator(progress = { a }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    TextButton(onClick = { cancelar.set(true) }) { Text("Parar") }
                }
            }

            // La letra: escribiéndola o en karaoke.
            Box(Modifier.weight(1f).fillMaxWidth()) {
                val s = aToques
                if (editando) {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            texto, { texto = it }, modifier = Modifier.fillMaxWidth().weight(1f),
                            placeholder = { Text("Pega aquí la letra de la canción, una línea por renglón (o un .lrc)") }
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                val cm = getSystemService(android.content.ClipboardManager::class.java)
                                cm?.primaryClip?.getItemAt(0)?.coerceToText(contexto)?.toString()?.let { texto = it }
                            }) { Icon(Icons.Filled.ContentPaste, null); Spacer(Modifier.width(6.dp)); Text("Pegar") }
                            FilledTonalButton(onClick = { elegirLetra.launch(arrayOf("text/*", "application/octet-stream")) }) { Text("Abrir .lrc / .txt") }
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
                        Spacer(Modifier.height(18.dp))
                        Text(s.textos.getOrNull(s.siguiente) ?: "", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(10.dp))
                        Text(s.textos.getOrNull(s.siguiente + 1) ?: "", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Text("${s.siguiente} de ${s.textos.size}", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            TextButton(onClick = { s.deshacer()?.let { ir(it) }; marcadas++ }) { Icon(Icons.AutoMirrored.Filled.Undo, null); Text(" Atrás") }
                            TextButton(onClick = { acabarAToques() }) { Text("Terminar") }
                        }
                    }
                } else {
                    Karaoke(letra, posicion, confianzas, alTocar = { ms -> ir(ms) }, alMantener = { i -> ponerTiempo(i, posicion) })
                }
            }

            // Lo que se hace con la letra.
            if (aToques == null && !editando) Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { if (!MotorWhisper.modeloListo(contexto)) preguntarBajar = true else sincronizarSola() }, enabled = audio != null && avance == null) {
                    Icon(Icons.Filled.AutoAwesome, null); Spacer(Modifier.width(6.dp)); Text("Sola")
                }
                FilledTonalButton(onClick = { empezarAToques() }, enabled = audio != null && avance == null) {
                    Icon(Icons.Filled.TouchApp, null); Spacer(Modifier.width(6.dp)); Text("A toques")
                }
                IconButton(onClick = { editando = true }) { Icon(Icons.Filled.Edit, "Editar la letra") }
                IconButton(onClick = { compartir() }, enabled = letra.conTiempos) { Icon(Icons.Filled.Share, "Compartir el LRC") }
                IconButton(onClick = { guardarLrc.launch((nombre.substringBeforeLast('.').ifBlank { "letra" }) + ".lrc") }, enabled = letra.conTiempos) { Icon(Icons.Filled.Save, "Guardar el LRC") }
            }
            if (quienLlama != null && aToques == null) Button(
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
    }

    /** El karaoke: la que suena resaltada y en el centro; tocar salta, mantener le pone el tiempo de ahora. */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Karaoke(letra: LetraDeCancion.Letra, posicion: Int, confianzas: List<Float>?, alTocar: (Int) -> Unit, alMantener: (Int) -> Unit) {
        val actual = LetraDeCancion.lineaActual(letra.lineas, posicion)
        val lista = rememberLazyListState()
        LaunchedEffect(actual) { if (actual >= 0) lista.animateScrollToItem((actual - 2).coerceAtLeast(0)) }
        if (letra.lineas.isEmpty()) {
            Text("Sin letra. Tócala con el lápiz para pegarla.", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        LazyColumn(Modifier.fillMaxSize(), state = lista) {
            itemsIndexed(letra.lineas) { i, l ->
                val suena = i == actual
                val dudosa = confianzas?.getOrNull(i)?.let { it < 0.4f } == true
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 44.dp)
                        .combinedClickable(onClick = { l.ms?.let(alTocar) }, onLongClick = { alMantener(i) })
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        l.ms?.let { tiempo(it) } ?: "—", fontSize = 12.sp, modifier = Modifier.width(52.dp),
                        color = if (dudosa) Color(0xFFFFA000) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        l.texto, fontSize = if (suena) 21.sp else 17.sp,
                        fontWeight = if (suena) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            suena -> MaterialTheme.colorScheme.primary
                            dudosa -> Color(0xFFFFA000)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            item {
                Text(
                    "Toca una línea para ir a ella; mantenla para ponerle el tiempo de ahora. En ámbar, las que se pusieron a ojo.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(18.dp)
                )
            }
        }
    }

    companion object {
        val MORADO = Color(0xFF6C4DFF)
        val MORADO_CLARO = Color(0xFFB4A3FF)
        fun tiempo(ms: Int): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }
    }
}

