package com.letrasyncpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letrasyncpro.motor.EdicionDeLetra
import com.letrasyncpro.motor.LetraDeCancion
import com.letrasyncpro.motor.LrcLib
import com.letrasyncpro.motor.Onda
import com.letrasyncpro.motor.SincronizadorPorPalabras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/** Lo que se ve de la onda: 10 s, con lo que suena en el centro. */
private const val VENTANA_MS = 10_000

/**
 * **La onda de la canción con las líneas encima** (como la línea de tiempo de Composer): lo que suena va en el
 * centro; arrastrar el fondo corre la canción; arrastrar una marca mueve su línea (y con el imán se pega al
 * arranque del sonido más cercano); tocar una marca la elige, tocar el fondo salta ahí.
 */
@Composable
fun VistaDeOnda(
    onda: FloatArray?, lineas: List<LetraDeCancion.Linea>, posicion: Int, duracion: Int, elegida: Int?,
    iman: Boolean, inicios: List<Int>, limites: (Int) -> IntRange, anticipo: Int = 0,
    alIr: (Int) -> Unit, alMover: (Int, Int) -> Unit, alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primario = MaterialTheme.colorScheme.primary
    val tenue = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val fondo = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val densidad = LocalDensity.current
    val umbralPx = with(densidad) { 28.dp.toPx() }
    val asaPx = with(densidad) { 22.dp.toPx() }
    val lim by rememberUpdatedState(limites)
    val a by rememberUpdatedState(anticipo)
    // Mientras se arrastra una marca, la ventana se queda quieta y la marca va donde el dedo.
    var arrastrada by remember { mutableIntStateOf(-1) }
    var msArrastre by remember { mutableIntStateOf(0) }
    var centroFijo by remember { mutableStateOf<Int?>(null) }
    val pos by rememberUpdatedState(posicion)
    val ls by rememberUpdatedState(lineas)
    val dur by rememberUpdatedState(duracion)
    val imanAhora by rememberUpdatedState(iman)
    val ini by rememberUpdatedState(inicios)
    var anchoPx by remember { mutableIntStateOf(1) }
    /** Donde se ve la línea [i] en la onda: donde se canta (su tiempo más el anticipo). */
    fun vozDe(i: Int): Int? = ls.getOrNull(i)?.ms?.let { it + a }
    /** Los límites de la línea [i] en la onda (en voz). */
    fun limVoz(i: Int): IntRange = lim(i).let { r -> (r.first + a)..(if (r.last == Int.MAX_VALUE) Int.MAX_VALUE else r.last + a) }

    // Al principio de la canción no se enseña «antes de 0:00»: la ventana empieza en 0 y la raya de lo que suena va a la izquierda.
    fun centro() = (centroFijo ?: pos).coerceAtLeast(VENTANA_MS / 2)
    fun msEn(x: Float): Int = (centro() - VENTANA_MS / 2 + x / anchoPx * VENTANA_MS).toInt()
    fun xDe(ms: Int): Float = ((ms - (centro() - VENTANA_MS / 2)).toFloat() / VENTANA_MS) * anchoPx
    fun marcaCerca(x: Float): Int? =
        ls.indices.filter { ls[it].ms != null }.minByOrNull { abs(xDe(vozDe(it)!!) - x) }?.takeIf { abs(xDe(vozDe(it)!!) - x) <= umbralPx }

    Canvas(
        modifier.fillMaxWidth().height(112.dp).background(fondo).onSizeChanged { anchoPx = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectTapGestures { o -> marcaCerca(o.x)?.let(alElegir) ?: alIr(msEn(o.x).coerceIn(0, dur)) }
            }
            .pointerInput(Unit) {
                var desde = 0
                var dx = 0f
                detectDragGestures(
                    onDragStart = { o ->
                        dx = 0f
                        val m = marcaCerca(o.x)
                        // Solo se coge una línea por su asa (arriba) o muy cerca de su raya: así arrastrar la onda
                        // para moverse por la canción no se lleva una línea sin querer.
                        val porArriba = o.y <= asaPx * 1.6f
                        if (m != null && (porArriba || abs(xDe(vozDe(m)!!) - o.x) <= umbralPx / 2)) { arrastrada = m; msArrastre = vozDe(m)!!; centroFijo = pos; desde = msArrastre; alElegir(m) }
                        else { arrastrada = -1; desde = pos }
                    },
                    onDragEnd = {
                        if (arrastrada >= 0) {
                            val fin = (if (imanAhora) Onda.pegar(msArrastre, ini) else msArrastre).coerceIn(limVoz(arrastrada))
                            alMover(arrastrada, fin)
                        }
                        arrastrada = -1; centroFijo = null
                    },
                    onDragCancel = { arrastrada = -1; centroFijo = null },
                    onDrag = { cambio, d ->
                        cambio.consume()
                        dx += d.x
                        val msPorPx = VENTANA_MS.toFloat() / anchoPx
                        if (arrastrada >= 0) msArrastre = (desde + dx * msPorPx).toInt().coerceIn(limVoz(arrastrada))
                        else alIr((desde - dx * msPorPx).toInt().coerceIn(0, dur))
                    }
                )
            }
    ) {
        val w = size.width; val h = size.height
        val inicio = centro() - VENTANA_MS / 2
        val msPorPx = VENTANA_MS / w
        // La onda.
        if (onda != null) {
            val paso = 3f
            var x = 0f
            while (x < w) {
                val t0 = inicio + x * msPorPx; val t1 = t0 + paso * msPorPx
                val a = (t0 / Onda.MS_POR_PUNTO).toInt().coerceAtLeast(0); val b = (t1 / Onda.MS_POR_PUNTO).toInt().coerceAtMost(onda.size - 1)
                var amp = 0f
                for (k in a..b) if (k in onda.indices && onda[k] > amp) amp = onda[k]
                if (t0 >= 0 && a < onda.size) {
                    val alto = (amp * (h * 0.8f)).coerceAtLeast(1f)
                    drawLine(if (t0 < pos) primario.copy(alpha = 0.75f) else tenue, Offset(x, h / 2 - alto / 2), Offset(x, h / 2 + alto / 2), strokeWidth = 2f)
                }
                x += paso
            }
        }
        // Las líneas.
        ls.forEachIndexed { i, l ->
            val ms = if (i == arrastrada) msArrastre else vozDe(i) ?: return@forEachIndexed
            val x = xDe(ms)
            // La franja del anticipo: desde que aparece la letra hasta que se canta.
            if (a > 0) drawRect((if (i == elegida || i == arrastrada) Tema.CORAL else primario).copy(alpha = 0.14f), Offset(xDe(ms - a), 0f), androidx.compose.ui.geometry.Size(x - xDe(ms - a), h))
            if (x < -4 || x > w + 4) return@forEachIndexed
            val c = if (i == elegida || i == arrastrada) Tema.CORAL else primario
            drawLine(c, Offset(x, asaPx * 0.9f), Offset(x, h), strokeWidth = if (i == arrastrada) 5f else 3f)
            // El asa: una pastilla arriba, de donde se coge.
            drawRoundRect(
                c, topLeft = Offset(x - asaPx * 0.55f, 2f), size = androidx.compose.ui.geometry.Size(asaPx * 1.1f, asaPx * 0.9f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(asaPx * 0.45f)
            )
            // Las palabras de la línea, como rayitas.
            l.palabras?.forEachIndexed { k, p -> if (k > 0 && p != null) { val px = xDe(p + a + (ms - a - (l.ms ?: (ms - a)))); drawLine(c.copy(alpha = 0.6f), Offset(px, h - 14f), Offset(px, h), strokeWidth = 2f) } }
        }
        // Lo que suena.
        val xp = xDe(pos)
        drawLine(Color.White.copy(alpha = 0.9f), Offset(xp, 0f), Offset(xp, h), strokeWidth = 3f)
    }
}

/** El texto de una línea con la palabra que suena (y las ya cantadas) resaltadas. */
fun conPalabras(l: LetraDeCancion.Linea, ms: Int, resaltado: Color, cantado: Color): AnnotatedString {
    val actual = EdicionDeLetra.palabraActual(l, ms)
    if (actual < 0) return AnnotatedString(l.texto)
    return buildAnnotatedString {
        l.trozos.forEachIndexed { k, w ->
            if (k > 0) append(' ')
            when {
                k == actual -> withStyle(SpanStyle(color = resaltado, fontWeight = FontWeight.Bold)) { append(w) }
                k < actual -> withStyle(SpanStyle(color = cantado)) { append(w) }
                else -> append(w)
            }
        }
    }
}

/** **Vista previa** (como la de Voxen o Composer): la letra como la verá quien cante, a pantalla completa. */
@Composable
fun VistaPrevia(letra: LetraDeCancion.Letra, posicion: Int, alCerrar: () -> Unit) {
    val i = LetraDeCancion.lineaActual(letra.lineas, posicion)
    Box(Modifier.fillMaxSize().background(Color(0xFF0E0B1F)).clickable(onClick = alCerrar).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letra.lineas.getOrNull(i - 1)?.texto.orEmpty(), color = Color.White.copy(alpha = 0.35f), fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            val l = letra.lineas.getOrNull(i)
            Text(
                if (l == null) AnnotatedString("♪") else conPalabras(l, posicion, Tema.VIOLETA, Color.White.copy(alpha = 0.85f)),
                color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 40.sp
            )
            Spacer(Modifier.height(18.dp))
            Text(letra.lineas.getOrNull(i + 1)?.texto.orEmpty(), color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            Text("Toca para salir", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
        }
    }
}

/** **Buscar la letra en LRCLib** por título y artista; tocar una la trae (con tiempos si los tiene). */
@Composable
fun BuscarEnLrcLib(nombre: String, duracionMs: Int, version: String, alElegir: (String) -> Unit, alCerrar: () -> Unit) {
    val (t0, a0) = remember(nombre) { LrcLib.deNombre(nombre) }
    var titulo by remember { mutableStateOf(t0) }
    var artista by remember { mutableStateOf(a0) }
    var buscando by remember { mutableStateOf(false) }
    var resultados by remember { mutableStateOf<List<LrcLib.Resultado>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    fun buscar() {
        buscando = true; error = null
        alcance.launch {
            val r = withContext(Dispatchers.IO) { runCatching { LrcLib.ordenar(LrcLib.buscar(titulo, artista, version), duracionMs) } }
            buscando = false
            r.onSuccess { resultados = it }.onFailure { error = "No se pudo buscar: mira la conexión" }
        }
    }
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Buscar la letra en LRCLib") },
        text = {
            Column {
                OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(artista, { artista = it }, label = { Text("Artista") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { buscar() }, enabled = !buscando && (titulo.isNotBlank() || artista.isNotBlank())) { Text("Buscar") }
                    if (buscando) CircularProgressIndicator(Modifier.padding(start = 12.dp).width(24.dp).height(24.dp), strokeWidth = 2.dp)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
                val r = resultados
                if (r != null && r.isEmpty()) Text("No hay nada con ese nombre. Prueba solo con el título.", fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                if (!r.isNullOrEmpty()) LazyColumn(Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(top = 8.dp)) {
                    items(r, key = { it.id }) { x ->
                        val texto = x.sincronizada ?: x.llana
                        Column(Modifier.fillMaxWidth().clickable(enabled = texto != null) { texto?.let(alElegir) }.padding(vertical = 8.dp)) {
                            Text(x.titulo, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                listOfNotNull(x.artista.ifBlank { null }, x.album, x.duracionS?.let { LetraSyncActivity.tiempo((it * 1000).toInt()) }).joinToString(" · ") +
                                    " · " + when { x.instrumental -> "instrumental"; x.sincronizada != null -> "sincronizada"; else -> "solo texto" },
                                fontSize = 12.sp, color = if (x.sincronizada != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = alCerrar) { Text("Cerrar") } }
    )
}

/** Los datos de la canción que van en el LRC: `[ti:]`, `[ar:]`, `[al:]`, `[by:]`. */
@Composable
fun DatosDeLaCancion(letra: LetraDeCancion.Letra, nombre: String, alGuardar: (LetraDeCancion.Letra) -> Unit, alCerrar: () -> Unit) {
    val (t0, a0) = remember(nombre) { LrcLib.deNombre(nombre) }
    var titulo by remember { mutableStateOf(letra.titulo ?: t0) }
    var artista by remember { mutableStateOf(letra.artista ?: a0) }
    var album by remember { mutableStateOf(letra.album.orEmpty()) }
    var autor by remember { mutableStateOf(letra.autor.orEmpty()) }
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Datos de la canción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") }, singleLine = true)
                OutlinedTextField(artista, { artista = it }, label = { Text("Artista") }, singleLine = true)
                OutlinedTextField(album, { album = it }, label = { Text("Álbum") }, singleLine = true)
                OutlinedTextField(autor, { autor = it }, label = { Text("Quién hizo el LRC") }, singleLine = true)
                Text("Van en el LRC que guardes o compartas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                alGuardar(letra.copy(titulo = titulo.ifBlank { null }, artista = artista.ifBlank { null }, album = album.ifBlank { null }, autor = autor.ifBlank { null }))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}
