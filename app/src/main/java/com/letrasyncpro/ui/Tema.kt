package com.letrasyncpro.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * **El aspecto de LetraSync Pro** (6-nov-2026): oscuro de estudio por defecto, violeta para lo que suena y coral
 * para lo que se está tocando. Esquinas generosas y todo lo que se toca de 48 dp o más.
 */
object Tema {
    val VIOLETA = Color(0xFF8B7BFF)
    val VIOLETA_FUERTE = Color(0xFF5B45F0)
    val CORAL = Color(0xFFFF8A3D)
    val ROJO = Color(0xFFFF5A5F)
    val AMBAR = Color(0xFFFFB020)

    /** Lo mínimo que mide algo que se toca. */
    val TOQUE = 48.dp
    /** Los botones principales. */
    val BOTON = 56.dp
}

private val Oscuro = darkColorScheme(
    primary = Tema.VIOLETA, onPrimary = Color.White,
    primaryContainer = Color(0xFF2E2754), onPrimaryContainer = Color(0xFFE4DFFF),
    secondary = Tema.CORAL, onSecondary = Color.White,
    background = Color(0xFF0F0D17), onBackground = Color(0xFFF3F1FA),
    surface = Color(0xFF15121F), onSurface = Color(0xFFF3F1FA),
    surfaceVariant = Color(0xFF221E33), onSurfaceVariant = Color(0xFFA49FB8),
    surfaceContainer = Color(0xFF1B1828), surfaceContainerHigh = Color(0xFF242036),
    surfaceContainerHighest = Color(0xFF2C2840), surfaceContainerLow = Color(0xFF17141F),
    outline = Color(0xFF3A3550), outlineVariant = Color(0xFF2A2640), error = Tema.ROJO
)

private val Claro = lightColorScheme(
    primary = Tema.VIOLETA_FUERTE, onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E0FF), onPrimaryContainer = Color(0xFF1E1166),
    secondary = Color(0xFFE8681A), onSecondary = Color.White,
    background = Color(0xFFF7F6FB), onBackground = Color(0xFF16131F),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF16131F),
    surfaceVariant = Color(0xFFEEEBF7), onSurfaceVariant = Color(0xFF6B6680),
    surfaceContainer = Color(0xFFF2F0F9), surfaceContainerHigh = Color(0xFFECE9F5),
    surfaceContainerHighest = Color(0xFFE5E1F0), surfaceContainerLow = Color(0xFFF7F6FB),
    outline = Color(0xFFD5D0E5), outlineVariant = Color(0xFFE4E0EF), error = Color(0xFFD93A3F)
)

@Composable
fun TemaLetraSync(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(32.dp)
        ),
        content = contenido
    )
}
