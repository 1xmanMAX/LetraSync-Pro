package com.letrasyncpro.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Las piezas con el aire de iOS: barra de navegación, control segmentado, botones, listas agrupadas. */

/** El gris de los segmentos y los rellenos (tertiarySystemFill). */
@Composable
fun relleno(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)

/** La barra de navegación: a la izquierda y a la derecha botones de texto, en el centro el título. */
@Composable
fun BarraDeNavegacion(titulo: String, subtitulo: String?, grande: Boolean = false, izquierda: @Composable () -> Unit, derecha: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 6.dp)) {
            if (!grande) Column(Modifier.align(Alignment.Center).padding(horizontal = 96.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.4).sp)
                subtitulo?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
            }
            Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) { izquierda() }
            Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) { derecha() }
        }
        // El título grande de iOS, debajo de los botones: cabe entero y no pisa nada.
        if (grande && titulo.isNotEmpty()) Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 4.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.7).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitulo?.let { Text(it, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
        }
    }
}

/** Un botón de texto de la barra (acento; en negrita el principal). */
@Composable
fun BotonDeBarra(texto: String, principal: Boolean = false, activo: Boolean = true, icono: ImageVector? = null, alTocar: () -> Unit) {
    val color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Row(
        Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp)).clickable(enabled = activo, onClick = alTocar).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icono?.let { Icon(it, null, tint = color, modifier = Modifier.size(24.dp)) }
        if (texto.isNotEmpty()) Text(texto, color = color, fontSize = 17.sp, fontWeight = if (principal) FontWeight.SemiBold else FontWeight.Normal, letterSpacing = (-0.4).sp)
    }
}

/** El control segmentado de iOS: una pista gris y una pastilla que se desliza a la opción elegida. */
@Composable
fun Segmentado(opciones: List<String>, elegida: Int, activas: List<Boolean> = opciones.map { true }, modifier: Modifier = Modifier, alto: Dp = 36.dp, alElegir: (Int) -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth().height(alto).clip(RoundedCornerShape(9.dp)).background(relleno()).padding(2.dp)) {
        val ancho = maxWidth / opciones.size
        val x by animateDpAsState(ancho * elegida, spring(dampingRatio = 0.85f, stiffness = 600f), label = "seg")
        Box(
            Modifier.offset(x = x).width(ancho).fillMaxHeight().shadow(2.dp, RoundedCornerShape(7.dp))
                .clip(RoundedCornerShape(7.dp)).background(if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF636366) else Color.White)
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            opciones.forEachIndexed { i, t ->
                Box(
                    Modifier.weight(1f).fillMaxHeight().clickable(enabled = activas[i], interactionSource = remember { MutableInteractionSource() }, indication = null) { alElegir(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        t, fontSize = 14.sp, fontWeight = if (i == elegida) FontWeight.SemiBold else FontWeight.Medium, letterSpacing = (-0.2).sp,
                        color = if (activas[i]) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

enum class Estilo { LLENO, TINTADO, GRIS }

/** Un botón grande de iOS: esquinas continuas, se encoge un poco al pulsarlo. */
@Composable
fun BotonIos(
    texto: String, modifier: Modifier = Modifier, estilo: Estilo = Estilo.LLENO, icono: ImageVector? = null, activo: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary, alto: Dp = Tema.BOTON, alTocar: () -> Unit
) {
    val fuente = remember { MutableInteractionSource() }
    val pulsado by fuente.collectIsPressedAsState()
    val escala by animateFloatAsState(if (pulsado) 0.97f else 1f, label = "boton")
    val (fondo, tinta) = when (estilo) {
        Estilo.LLENO -> color to Color.White
        Estilo.TINTADO -> color.copy(alpha = 0.16f) to color
        Estilo.GRIS -> relleno() to MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier.height(alto).scale(escala).clip(RoundedCornerShape(14.dp)).background(if (activo) fondo else fondo.copy(alpha = fondo.alpha * 0.4f))
            .clickable(enabled = activo, interactionSource = fuente, indication = null, onClick = alTocar).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        val t = if (activo) tinta else tinta.copy(alpha = 0.4f)
        icono?.let { Icon(it, null, tint = t, modifier = Modifier.size(20.dp)); if (texto.isNotEmpty()) Spacer(Modifier.width(7.dp)) }
        if (texto.isNotEmpty()) Text(texto, color = t, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp, maxLines = 1)
    }
}

/** Una sección agrupada: el título en versalitas gris, y las filas en una tarjeta redondeada. */
@Composable
fun Grupo(titulo: String? = null, pie: String? = null, modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        titulo?.let { Text(it.uppercase(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 8.dp)) }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainer), content = contenido)
        pie?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)) }
    }
}

/** Una fila de lista de iOS: icono en su cuadradito de color, título, detalle y, si lleva a algo, la flecha. */
@Composable
fun Fila(
    titulo: String, detalle: String? = null, icono: ImageVector? = null, colorIcono: Color = MaterialTheme.colorScheme.primary,
    activo: Boolean = true, flecha: Boolean = true, separador: Boolean = true, alto: Dp = 52.dp, valor: String? = null,
    insignia: String? = null, alTocar: (() -> Unit)? = null, final: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = alto).then(if (alTocar != null) Modifier.clickable(enabled = activo, onClick = alTocar) else Modifier)
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icono?.let {
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)).background(if (activo) colorIcono else colorIcono.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                    Icon(it, null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(titulo, fontSize = 17.sp, letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (activo) 1f else 0.4f))
                    insignia?.let {
                        Text(
                            it, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                detalle?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (activo) 1f else 0.5f), lineHeight = 17.sp) }
            }
            valor?.let { Text(it, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp)) }
            final?.invoke()
            if (flecha && alTocar != null) Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp).size(22.dp))
        }
        if (separador) HorizontalDivider(Modifier.padding(start = if (icono != null) 60.dp else 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
    }
}

/** Arrastrar como en TikTok: desde donde iba la canción, lo que se mueve el dedo (todo el ancho, toda la canción). */
fun arrastreRelativo(desde: Int, dx: Float, ancho: Float, duracion: Int): Int =
    if (ancho <= 0f) desde else (desde + dx / ancho * duracion).toInt().coerceIn(0, duracion.coerceAtLeast(0))

/**
 * **La barra del tiempo** (7-nov-2026, «que se maneje con una mano, como TikTok»): se pone el dedo **en
 * cualquier punto** de la barra y se arrastra; la canción se mueve **desde donde iba**, adelante o atrás, lo que
 * se mueva el dedo. No hace falta llegar con el dedo al final: basta empujar. Un toque suelto no salta (así no
 * se pierde el sitio sin querer). Mientras se arrastra, la barra engorda y sale el tiempo grande.
 */
@Composable
fun BarraDeTiempo(posicion: Int, duracion: Int, alIr: (Int) -> Unit, modifier: Modifier = Modifier, alSoltar: () -> Unit = {}) {
    var cogida by remember { mutableStateOf(false) }
    val grosor by animateDpAsState(if (cogida) 12.dp else 5.dp, label = "grosor")
    val dur by rememberUpdatedState(duracion)
    val pos by rememberUpdatedState(posicion)
    val ir by rememberUpdatedState(alIr)
    val soltar by rememberUpdatedState(alSoltar)
    val lleno = MaterialTheme.colorScheme.onSurface.copy(alpha = if (cogida) 0.95f else 0.75f)
    val pista = relleno()
    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier.fillMaxWidth().height(44.dp)
                .pointerInput(Unit) {
                    var desde = 0
                    var dx = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { cogida = true; desde = pos; dx = 0f },
                        onDragEnd = { cogida = false; soltar() }, onDragCancel = { cogida = false; soltar() },
                        onHorizontalDrag = { c, d -> c.consume(); dx += d; ir(arrastreRelativo(desde, dx, size.width.toFloat(), dur)) }
                    )
                }
        ) {
            val g = grosor.toPx(); val y = size.height / 2 - g / 2
            val f = if (duracion > 0) (posicion.toFloat() / duracion).coerceIn(0f, 1f) else 0f
            drawRoundRect(pista, Offset(0f, y), Size(size.width, g), CornerRadius(g / 2))
            drawRoundRect(lleno, Offset(0f, y), Size(size.width * f, g), CornerRadius(g / 2))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(LetraSyncActivity.tiempo(posicion), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            if (cogida) Text(LetraSyncActivity.tiempo(posicion) + " / " + LetraSyncActivity.tiempo(duracion), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.weight(1f))
            Text("−" + LetraSyncActivity.tiempo((duracion - posicion).coerceAtLeast(0)), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
        }
    }
}
