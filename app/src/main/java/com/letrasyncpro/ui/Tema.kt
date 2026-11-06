package com.letrasyncpro.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * **El aspecto de LetraSync Pro, al estilo de Apple** (6-nov-2026): los colores del sistema de iOS (negro puro
 * de noche, grises agrupados), el rojo rosado de Apple Music para lo que suena y lo que se toca, naranja para
 * lo elegido, esquinas continuas y listas agrupadas. Todo lo que se toca mide 44 pt / 48 dp o más.
 */
object Tema {
    /** El acento (Apple Music). */
    val ACENTO = Color(0xFFFA2D48)
    /** Lo que se está retocando (systemOrange). */
    val NARANJA = Color(0xFFFF9F0A)
    val VERDE = Color(0xFF30D158)
    val AZUL = Color(0xFF0A84FF)
    val MORADO = Color(0xFFBF5AF2)
    val ROJO = Color(0xFFFF453A)
    val AMBAR = Color(0xFFFFD60A)

    // Los nombres de antes, para las piezas que aún los usan.
    val VIOLETA = ACENTO
    val VIOLETA_FUERTE = Color(0xFFE0153A)
    val CORAL = NARANJA

    val TOQUE = 48.dp
    val BOTON = 52.dp
}

private val Oscuro = darkColorScheme(
    primary = Tema.ACENTO, onPrimary = Color.White,
    primaryContainer = Color(0xFF3A0F18), onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = Tema.NARANJA, onSecondary = Color.Black,
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1E), onSurfaceVariant = Color(0xFF8E8E93),
    surfaceContainerLowest = Color.Black, surfaceContainerLow = Color(0xFF0E0E10),
    surfaceContainer = Color(0xFF1C1C1E), surfaceContainerHigh = Color(0xFF2C2C2E), surfaceContainerHighest = Color(0xFF3A3A3C),
    outline = Color(0xFF38383A), outlineVariant = Color(0xFF2C2C2E), error = Tema.ROJO
)

private val Claro = lightColorScheme(
    primary = Color(0xFFFA233B), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE1E5), onPrimaryContainer = Color(0xFF5C0010),
    secondary = Color(0xFFFF9500), onSecondary = Color.White,
    background = Color(0xFFF2F2F7), onBackground = Color.Black,
    surface = Color(0xFFF2F2F7), onSurface = Color.Black,
    surfaceVariant = Color.White, onSurfaceVariant = Color(0xFF8A8A8E),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF7F7FA),
    surfaceContainer = Color.White, surfaceContainerHigh = Color(0xFFE5E5EA), surfaceContainerHighest = Color(0xFFD1D1D6),
    outline = Color(0xFFC6C6C8), outlineVariant = Color(0xFFE5E5EA), error = Color(0xFFFF3B30)
)

@Composable
fun TemaLetraSync(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(28.dp)
        ),
        content = contenido
    )
}
