package com.letrasyncpro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letrasyncpro.motor.EdicionDeLetra
import com.letrasyncpro.motor.LetraDeCancion
import com.letrasyncpro.motor.Onda
import kotlin.math.abs

/**
 * **Marcar en la onda** (rehecho el 7-nov-2026 con lo que pidió el usuario: «al moverla con el dedo debería sonar;
 * al parar pongo la marca», «tiembla», «demasiadas cosas en la pantalla»).
 *
 * Como Notas de voz: **la raya de lo que suena está siempre en el centro** y la onda corre por debajo.
 * - **Deslizar** la onda mueve la canción y **suena lo que pasa bajo la raya** (el raspado).
 * - **Tocar** salta a ese punto. **Pellizcar** estira o encoge la onda.
 * - **▶︎** suena (a la velocidad de la pastilla) y la onda avanza, lisa.
 * - **Marcar aquí** pone la frase que toca justo en la raya, sin imán ni propuesta que la muevan, y pasa a la
 *   siguiente. Se guarda con su **anticipo** (la franja antes de cada marca): la letra aparece un poco antes.
 */
@Composable
fun MarcarEnLaOnda(
    letra: LetraDeCancion.Letra, onda: FloatArray?, anticipo: Int,
    posicion: Int, sonando: Boolean, duracion: Int, velocidad: Float, soloVoz: Boolean,
    alVelocidad: (Float) -> Unit, alRaspar: (Int) -> Unit, alSoltar: () -> Unit, alTocar: () -> Unit,
    alMarcar: (indice: Int) -> Unit, alTerminar: () -> Unit
) {
    val n = letra.lineas.size
    var indice by remember { mutableIntStateOf(letra.lineas.indexOfFirst { it.ms == null }.takeIf { it >= 0 } ?: 0) }
    var ventana by remember { mutableFloatStateOf(6_000f) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Frase ${minOf(indice + 1, n)} de $n", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            LinearProgressIndicator(
                progress = { if (n == 0) 0f else indice.toFloat() / n }, modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary, trackColor = relleno()
            )
        }
        AnimatedContent(indice, transitionSpec = { (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { -it / 3 } + fadeOut()) }, label = "frase") { i ->
            Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp)) {
                Text(letra.lineas.getOrNull(i)?.texto ?: "¡Listo!", fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp, maxLines = 2)
                Text(
                    letra.lineas.getOrNull(i + 1)?.texto.orEmpty(), fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(top = 4.dp), maxLines = 1
                )
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainer)) {
            OndaGrande(
                onda, letra, anticipo, indice, posicion, ventana,
                alRaspar = alRaspar, alSoltar = alSoltar,
                alAcercar = { f -> ventana = (ventana / f).coerceIn(VENTANA_MINIMA, VENTANA_MAXIMA) }
            )
            if (onda == null) Text("Preparando la onda…", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
            // Arriba, pequeño: la velocidad (toca para cambiarla) y el zum.
            Row(Modifier.align(Alignment.TopStart).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Pastilla(if (velocidad == 1f) "1×" else if (velocidad == 0.75f) "¾×" else "½×", velocidad != 1f) {
                    alVelocidad(when (velocidad) { 1f -> 0.75f; 0.75f -> 0.5f; else -> 1f })
                }
                if (soloVoz) Text("  solo la voz", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.align(Alignment.TopEnd).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Lupa(Icons.Filled.Remove, ventana < VENTANA_MAXIMA) { ventana = (ventana * 2).coerceAtMost(VENTANA_MAXIMA) }
                Lupa(Icons.Filled.Add, ventana > VENTANA_MINIMA) { ventana = (ventana / 2).coerceAtLeast(VENTANA_MINIMA) }
            }
        }
        Text(
            "Desliza para oír · toca para ir · pellizca para acercar", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotonIos("", Modifier.width(60.dp), Estilo.GRIS, Icons.AutoMirrored.Filled.Undo, activo = indice > 0) { indice-- }
            Box(
                Modifier.size(Tema.BOTON + 4.dp).clip(CircleShape).background(relleno()).clickable(onClick = alTocar),
                contentAlignment = Alignment.Center
            ) { Icon(if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (sonando) "Pausa" else "Reproducir", modifier = Modifier.size(30.dp)) }
            BotonIos(if (indice >= n) "Terminar" else "Marcar aquí", Modifier.weight(1f), Estilo.LLENO, alto = Tema.BOTON + 4.dp) {
                if (indice >= n) alTerminar()
                else { alMarcar(indice); if (indice == n - 1 && !sonando) alTerminar() else indice++ }
            }
        }
    }
}

private const val VENTANA_MINIMA = 1_000f
private const val VENTANA_MAXIMA = 30_000f
val VELOCIDADES = listOf(0.5f, 0.75f, 1f)

@Composable
private fun Lupa(icono: ImageVector, activo: Boolean, alTocar: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = activo, onClick = alTocar),
        contentAlignment = Alignment.Center
    ) { Icon(icono, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (activo) 1f else 0.35f), modifier = Modifier.size(20.dp)) }
}

@Composable
private fun Pastilla(texto: String, resaltada: Boolean, alTocar: () -> Unit) {
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp))
            .background(if (resaltada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = alTocar).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (resaltada) Color.White else MaterialTheme.colorScheme.onSurface) }
}

/**
 * La onda grande: barras fijas al tiempo ([Onda.barras], no tiemblan), la raya de lo que suena en el centro, la
 * regla de segundos y las frases ya marcadas donde se cantan (con la franja de su anticipo).
 */
@Composable
private fun OndaGrande(
    onda: FloatArray?, letra: LetraDeCancion.Letra, anticipo: Int, indice: Int, posicion: Int, ventana: Float,
    alRaspar: (Int) -> Unit, alSoltar: () -> Unit, alAcercar: (Float) -> Unit
) {
    val acento = MaterialTheme.colorScheme.primary
    val barra = MaterialTheme.colorScheme.onSurface
    val gris = MaterialTheme.colorScheme.onSurfaceVariant
    val densidad = LocalDensity.current
    var ancho by remember { mutableIntStateOf(1) }
    val pos by rememberUpdatedState(posicion)
    val v by rememberUpdatedState(ventana)
    val raspar by rememberUpdatedState(alRaspar)
    val soltar by rememberUpdatedState(alSoltar)
    val acercar by rememberUpdatedState(alAcercar)
    Canvas(
        Modifier.fillMaxSize().onSizeChanged { ancho = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                val umbral = viewConfiguration.touchSlop
                awaitEachGesture {
                    val abajo = awaitFirstDown()
                    val desde = pos
                    var dx = 0f
                    var movio = false
                    while (true) {
                        val ev = awaitPointerEvent()
                        if (ev.changes.none { it.pressed }) break
                        val z = ev.calculateZoom()
                        if (z != 1f) { acercar(z); movio = true }
                        dx += ev.calculatePan().x
                        if (abs(dx) > umbral) movio = true
                        // Arrastrar a la izquierda va hacia delante (la onda corre bajo la raya).
                        if (movio && ev.changes.size == 1) raspar((desde - dx / ancho * v).toInt())
                        ev.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                    // Un toque sin arrastrar: salta a ese punto.
                    if (!movio) raspar((desde + (abajo.position.x - ancho / 2f) / ancho * v).toInt())
                    soltar()
                }
            }
    ) {
        val w = size.width; val h = size.height
        val ini = pos - v / 2
        fun x(ms: Number) = ((ms.toFloat() - ini) / v) * w
        val arriba = 24f * densidad.density; val abajoPx = 20f * densidad.density
        val alto = h - arriba - abajoPx
        val medio = arriba + alto / 2
        // La regla.
        val paso = when { v <= 1_500 -> 100; v <= 4_000 -> 500; v <= 10_000 -> 1_000; v <= 20_000 -> 2_000; else -> 5_000 }
        val pintura = android.graphics.Paint().apply { color = android.graphics.Color.argb(150, 142, 142, 147); textSize = with(densidad) { 10.sp.toPx() }; isAntiAlias = true }
        var t = (ini / paso).toInt() * paso
        while (t <= ini + v) {
            if (t >= 0) {
                val xx = x(t)
                drawLine(gris.copy(alpha = 0.35f), Offset(xx, h - 14f), Offset(xx, h - 4f), strokeWidth = 1.5f)
                if (t % (paso * 2) == 0) drawContext.canvas.nativeCanvas.drawText(if (paso < 1_000) EdicionDeLetra.conDecimas(t) else LetraSyncActivity.tiempo(t), xx + 4f, h - 4f, pintura)
            }
            t += paso
        }
        // La onda: cada barra, siempre el mismo trozo de canción.
        if (onda != null) {
            val (msBarra, barras) = Onda.barras(onda, ini, v, w, 10f)
            val anchoBarra = (msBarra / v * w * 0.62f).coerceAtLeast(1.5f)
            for ((ms, amp) in barras) {
                val xx = x(ms)
                val hh = (amp * alto * 0.92f).coerceAtLeast(3f)
                drawRoundRect(if (ms < pos) barra.copy(alpha = 0.9f) else gris.copy(alpha = 0.55f), Offset(xx, medio - hh / 2), Size(anchoBarra, hh), CornerRadius(anchoBarra / 2))
            }
        }
        // Las frases marcadas: donde se cantan, con la franja de su anticipo.
        letra.lineas.forEachIndexed { i, l ->
            val ms = l.ms ?: return@forEachIndexed
            val voz = ms + anticipo
            if (x(voz) < -30 || x(ms) > w + 30) return@forEachIndexed
            val c = if (i == indice) acento else gris
            drawRect(c.copy(alpha = 0.14f), Offset(x(ms), arriba), Size(x(voz) - x(ms), alto))
            drawLine(c.copy(alpha = 0.85f), Offset(x(voz), arriba), Offset(x(voz), arriba + alto), strokeWidth = 2f)
            numero(i + 1, x(voz), c, densidad.density)
        }
        // La raya de lo que suena, en el centro.
        drawLine(acento, Offset(w / 2, arriba - 6f), Offset(w / 2, arriba + alto + 6f), strokeWidth = 3f)
        drawCircle(acento, 6f * densidad.density / 2.5f * 2f, Offset(w / 2, arriba - 6f))
    }
}

/** El número de la frase en una pastilla, encima de su raya. */
private fun DrawScope.numero(n: Int, x: Float, color: Color, densidad: Float) {
    val r = 10f * densidad
    drawRoundRect(color, Offset(x - r, 2f), Size(r * 2, 18f * densidad), CornerRadius(9f * densidad))
    val p = android.graphics.Paint().apply { this.color = android.graphics.Color.WHITE; textSize = 11f * densidad; isFakeBoldText = true; textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true }
    drawContext.canvas.nativeCanvas.drawText(n.toString(), x, 2f + 13f * densidad, p)
}
