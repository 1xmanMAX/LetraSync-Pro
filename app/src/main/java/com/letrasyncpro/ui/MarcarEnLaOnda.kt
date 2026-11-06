package com.letrasyncpro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letrasyncpro.motor.LetraDeCancion
import com.letrasyncpro.motor.Onda

/**
 * **Marcar en la onda** (6-nov-2026, idea del usuario: «yo selecciono dónde empieza cada oración y tú lo
 * retrocedes un poco para que la letra aparezca antes, en una interfaz con las ondas»).
 *
 * Arriba, la frase que toca, grande. En medio, la onda de la canción (deslizar para moverse, pellizcar para
 * acercar). La app **propone** dónde empieza la frase (el siguiente arranque de sonido) y lo deja sonar; si es
 * ahí, «Aceptar»; si no, se toca donde empieza la voz y vuelve a sonar desde ahí. Lo que se guarda es ese punto
 * menos el **anticipo** (Ajustes): la letra aparece un poco antes de cantarse. La franja antes de cada marca es
 * ese anticipo.
 */
@Composable
fun MarcarEnLaOnda(
    letra: LetraDeCancion.Letra, onda: FloatArray?, inicios: List<Int>, anticipo: Int, iman: Boolean,
    posicion: Int, sonando: Boolean, duracion: Int, velocidad: Float, soloVoz: Boolean,
    alVelocidad: (Float) -> Unit,
    alIr: (Int) -> Unit, alOir: (desde: Int) -> Unit, alMarcar: (indice: Int, vozMs: Int) -> Unit, alTerminar: () -> Unit
) {
    val n = letra.lineas.size
    var indice by remember { mutableIntStateOf(letra.lineas.indexOfFirst { it.ms == null }.takeIf { it >= 0 } ?: 0) }
    /** Dónde se canta la línea [i] ya marcada (su tiempo más el anticipo), o null. */
    fun vozDe(i: Int): Int? = letra.lineas.getOrNull(i)?.ms?.let { it + anticipo }
    fun propuesta(i: Int): Int {
        vozDe(i)?.let { return it }
        val previa = (i - 1 downTo 0).firstNotNullOfOrNull { vozDe(it) }
        return Onda.sugerencia(inicios, previa ?: -600) ?: ((previa ?: 0) + 2_000).coerceAtMost(duracion.coerceAtLeast(0))
    }
    var candidato by remember { mutableIntStateOf(propuesta(indice)) }
    var ventana by remember { mutableFloatStateOf(4_000f) }
    var centro by remember { mutableIntStateOf(candidato) }
    // Al cambiar de frase (o al llegar la onda y sus arranques), la propuesta nueva, y suena.
    LaunchedEffect(indice, inicios.size) { candidato = propuesta(indice); centro = candidato }
    // Sonando, la onda sigue a la canción.
    LaunchedEffect(sonando, posicion) { if (sonando) centro = posicion }

    fun elegirEn(ms: Int) {
        candidato = (if (iman) Onda.pegar(ms, inicios, 250) else ms).coerceIn(0, duracion.coerceAtLeast(0))
        alOir(candidato)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // La frase que toca.
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Frase ${indice + 1} de $n", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            Text("aparece ${"%.1f".format(anticipo / 1000f)} s antes", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { if (n == 0) 0f else indice.toFloat() / n }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary, trackColor = relleno()
        )
        AnimatedContent(indice, transitionSpec = { (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { -it / 3 } + fadeOut()) }, label = "frase") { i ->
            Column(Modifier.fillMaxWidth().heightIn(min = 120.dp)) {
                Text(
                    letra.lineas.getOrNull(i)?.texto.orEmpty(), fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp, maxLines = 3
                )
                Text(
                    letra.lineas.getOrNull(i + 1)?.texto.orEmpty(), fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), modifier = Modifier.padding(top = 6.dp), maxLines = 2
                )
            }
        }
        // La onda.
        Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainer)) {
            OndaGrande(
                onda, letra, anticipo, indice, candidato, posicion, centro, ventana,
                alTocar = { elegirEn(it) },
                alMover = { d -> centro = (centro + d).coerceIn(0, duracion.coerceAtLeast(0)); alIr(centro) },
                alAcercar = { f -> ventana = (ventana / f).coerceIn(VENTANA_MINIMA, VENTANA_MAXIMA) }
            )
            if (onda == null) Text("Preparando la onda…", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
            // Estirar y encoger la onda (también con dos dedos), y si lo que se ve es solo la voz.
            Row(Modifier.align(Alignment.TopEnd).padding(top = 26.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Lupa(Icons.Filled.ZoomOut, ventana < VENTANA_MAXIMA) { ventana = (ventana * 2).coerceAtMost(VENTANA_MAXIMA) }
                Lupa(Icons.Filled.ZoomIn, ventana > VENTANA_MINIMA) { ventana = (ventana / 2).coerceAtLeast(VENTANA_MINIMA) }
            }
            Text(
                (if (soloVoz) "Solo la voz · " else "") + "${"%.1f".format(ventana / 1000f)} s a la vista",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 30.dp)
            )
        }
        Text(
            "Toca donde empieza la voz. Desliza para moverte; pellizca para acercar.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        // Más despacio para oír bien dónde empieza (el tono no cambia).
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Velocidad", fontSize = 15.sp, modifier = Modifier.padding(end = 12.dp))
            Segmentado(
                listOf("½×", "¾×", "1×"), VELOCIDADES.indexOf(velocidad).coerceAtLeast(0), alto = 34.dp, modifier = Modifier.weight(1f)
            ) { alVelocidad(VELOCIDADES[it]) }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonIos("", Modifier.weight(0.8f), Estilo.GRIS, Icons.AutoMirrored.Filled.Undo, activo = indice > 0) { indice-- }
            BotonIos("Oír", Modifier.weight(1.1f), Estilo.TINTADO, Icons.Filled.Headphones) { alOir(candidato) }
            BotonIos(if (indice == n - 1) "Terminar" else "Aceptar", Modifier.weight(1.6f), Estilo.LLENO, Icons.Filled.Check) {
                alMarcar(indice, candidato)
                if (indice >= n - 1) alTerminar() else indice++
            }
        }
    }
}

private const val VENTANA_MINIMA = 1_000f
private const val VENTANA_MAXIMA = 30_000f
val VELOCIDADES = listOf(0.5f, 0.75f, 1f)

@Composable
private fun Lupa(icono: androidx.compose.ui.graphics.vector.ImageVector, activo: Boolean, alTocar: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f))
            .clickable(enabled = activo, onClick = alTocar),
        contentAlignment = Alignment.Center
    ) { Icon(icono, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (activo) 1f else 0.35f), modifier = Modifier.size(22.dp)) }
}

/** La onda grande de «Marcar»: barras redondeadas como Notas de voz, la regla de segundos y las marcas. */
@Composable
private fun OndaGrande(
    onda: FloatArray?, letra: LetraDeCancion.Letra, anticipo: Int, indice: Int, candidato: Int, posicion: Int, centro: Int, ventana: Float,
    alTocar: (Int) -> Unit, alMover: (Int) -> Unit, alAcercar: (Float) -> Unit
) {
    val acento = MaterialTheme.colorScheme.primary
    val barra = MaterialTheme.colorScheme.onSurface
    val gris = MaterialTheme.colorScheme.onSurfaceVariant
    val densidad = LocalDensity.current
    var ancho by remember { mutableIntStateOf(1) }
    val c by rememberUpdatedState(centro)
    val v by rememberUpdatedState(ventana)
    val tocar by rememberUpdatedState(alTocar)
    val mover by rememberUpdatedState(alMover)
    val acercar by rememberUpdatedState(alAcercar)
    fun inicio() = c - v / 2
    Canvas(
        Modifier.fillMaxSize().onSizeChanged { ancho = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) { detectTapGestures { o -> tocar((inicio() + o.x / ancho * v).toInt()) } }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f) acercar(zoom)
                    if (pan.x != 0f) mover((-pan.x / ancho * v).toInt())
                }
            }
    ) {
        val w = size.width; val h = size.height
        val ini = inicio()
        fun x(ms: Number) = ((ms.toFloat() - ini) / v) * w
        val arriba = 22f * densidad.density; val abajo = 18f * densidad.density
        val alto = h - arriba - abajo
        val medio = arriba + alto / 2
        // La regla de segundos.
        val paso = when { v <= 1_500 -> 100; v <= 4_000 -> 500; v <= 10_000 -> 1_000; v <= 20_000 -> 2_000; else -> 5_000 }
        var t = (ini / paso).toInt() * paso
        val pintura = android.graphics.Paint().apply { color = android.graphics.Color.argb(150, 142, 142, 147); textSize = with(densidad) { 10.sp.toPx() }; isAntiAlias = true }
        while (t <= ini + v) {
            if (t >= 0) {
                val xx = x(t)
                drawLine(gris.copy(alpha = 0.35f), Offset(xx, h - 14f), Offset(xx, h - 4f), strokeWidth = 1.5f)
                if (t % (paso * 2) == 0) drawContext.canvas.nativeCanvas.drawText(if (paso < 1_000) com.letrasyncpro.motor.EdicionDeLetra.conDecimas(t) else LetraSyncActivity.tiempo(t), xx + 4f, h - 4f, pintura)
            }
            t += paso
        }
        // La onda: barras redondeadas, más claras lo ya sonado.
        if (onda != null) {
            val barraPx = 4.5f; val hueco = 2.5f
            var xx = 0f
            while (xx < w) {
                val t0 = ini + xx / w * v; val t1 = ini + (xx + barraPx + hueco) / w * v
                val a = (t0 / Onda.MS_POR_PUNTO).toInt(); val b = (t1 / Onda.MS_POR_PUNTO).toInt()
                if (t0 >= 0 && a < onda.size) {
                    var amp = 0f
                    for (k in a.coerceAtLeast(0)..b.coerceAtMost(onda.size - 1)) if (onda[k] > amp) amp = onda[k]
                    val hh = (amp * alto * 0.92f).coerceAtLeast(3f)
                    drawRoundRect(
                        if (t0 < posicion) barra.copy(alpha = 0.9f) else gris.copy(alpha = 0.55f),
                        Offset(xx, medio - hh / 2), Size(barraPx, hh), CornerRadius(barraPx / 2)
                    )
                }
                xx += barraPx + hueco
            }
        }
        // Las frases ya marcadas: donde se canta, y la franja de su anticipo.
        letra.lineas.forEachIndexed { i, l ->
            if (i == indice) return@forEachIndexed
            val ms = l.ms ?: return@forEachIndexed
            val voz = ms + anticipo
            if (x(voz) < -20 || x(ms) > w + 20) return@forEachIndexed
            drawRect(gris.copy(alpha = 0.12f), Offset(x(ms), arriba), Size(x(voz) - x(ms), alto))
            drawLine(gris.copy(alpha = 0.7f), Offset(x(voz), arriba), Offset(x(voz), arriba + alto), strokeWidth = 2f)
            numero(i + 1, x(voz), gris.copy(alpha = 0.8f), densidad.density)
        }
        // La propuesta: en el acento, con su franja.
        val xc = x(candidato); val xa = x(candidato - anticipo)
        drawRect(acento.copy(alpha = 0.16f), Offset(xa, arriba), Size(xc - xa, alto))
        drawLine(acento, Offset(xa, arriba), Offset(xa, arriba + alto), strokeWidth = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        drawLine(acento, Offset(xc, arriba - 4f), Offset(xc, arriba + alto), strokeWidth = 3.5f)
        numero(indice + 1, xc, acento, densidad.density)
        // Lo que suena.
        val xp = x(posicion)
        drawLine(barra, Offset(xp, arriba), Offset(xp, arriba + alto), strokeWidth = 2f)
        drawCircle(barra, 5f, Offset(xp, arriba))
    }
}

/** El número de la frase en una pastilla, encima de su raya. */
private fun DrawScope.numero(n: Int, x: Float, color: Color, densidad: Float) {
    val r = 10f * densidad
    drawRoundRect(color, Offset(x - r, 0f), Size(r * 2, 18f * densidad), CornerRadius(9f * densidad))
    val p = android.graphics.Paint().apply { this.color = android.graphics.Color.WHITE; textSize = 11f * densidad; isFakeBoldText = true; textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true }
    drawContext.canvas.nativeCanvas.drawText(n.toString(), x, 13f * densidad, p)
}
