package com.sancheeese.cleanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1677FF),
    secondary = Color(0xFF12A150),
    tertiary = Color(0xFFE87519),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    error = Color(0xFFC62828)
)

@Composable
fun CleannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
