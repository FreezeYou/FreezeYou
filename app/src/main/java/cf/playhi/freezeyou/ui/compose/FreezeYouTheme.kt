package cf.playhi.freezeyou.ui.compose

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.R as AppCompatR
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private fun Context.themeColor(@AttrRes attribute: Int, fallback: Color): Color {
    val value = TypedValue()
    if (!theme.resolveAttribute(attribute, value, true)) return fallback
    if (value.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
        return Color(value.data)
    }
    if (value.resourceId != 0) {
        ContextCompat.getColorStateList(this, value.resourceId)?.let {
            return Color(it.defaultColor)
        }
    }
    return fallback
}

@Composable
fun FreezeYouTheme(readableControls: Boolean = false, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val dark = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
        Configuration.UI_MODE_NIGHT_YES
    val accent = context.themeColor(android.R.attr.colorAccent, Color(0xFF1976D2))
    val primary = context.themeColor(AppCompatR.attr.colorPrimary, accent)
    val background = context.themeColor(
        android.R.attr.colorBackground,
        if (dark) Color(0xFF121212) else Color.White
    )
    val surface = context.themeColor(android.R.attr.windowBackground, background)
    val onSurface = context.themeColor(
        android.R.attr.textColorPrimary,
        if (dark) Color.White else Color.Black
    )
    // Controls used as text and outlines need contrast against the user's chosen background.
    val controlColor = if (readableControls) {
        listOf(primary, accent, onSurface).firstOrNull {
            val a = it.luminance()
            val b = background.luminance()
            (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f) >= 4.5f
        } ?: onSurface
    } else primary
    val onPrimary = if (controlColor.luminance() > (if (readableControls) 0.179f else 0.5f)) Color.Black else Color.White
    val useDarkScheme = if (readableControls) background.luminance() < 0.179f else dark
    val colorScheme = when {
        useDarkScheme -> darkColorScheme(
            primary = controlColor,
            onPrimary = onPrimary,
            secondary = accent,
            background = background,
            surface = surface,
            onBackground = onSurface,
            onSurface = onSurface
        )
        else -> lightColorScheme(
            primary = controlColor,
            onPrimary = onPrimary,
            secondary = accent,
            background = background,
            surface = surface,
            onBackground = onSurface,
            onSurface = onSurface
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(color = colorScheme.background, content = content)
    }
}
