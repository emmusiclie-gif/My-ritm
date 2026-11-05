package com.moyritm.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MyRitmColors = lightColorScheme(
    primary = Color(0xFF657B5B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF9EAF91),
    onSecondary = Color(0xFF243021),
    background = Color(0xFFF6F3EA),
    onBackground = Color(0xFF30392D),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF30392D),
    surfaceVariant = Color(0xFFE8EDE2),
    onSurfaceVariant = Color(0xFF596653)
)

@Composable
fun MyRitmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyRitmColors,
        content = content
    )
}
