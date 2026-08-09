package cf.playhi.freezeyou.ui.compose

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

private fun Context.themeColor(attribute: Int, fallback: Color): Color {
    val value = TypedValue()
    return if (theme.resolveAttribute(attribute, value, true)) {
        Color(value.data)
    } else {
        fallback
    }
}

@Composable
fun FreezeYouTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val dark = configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val colorScheme = when {
        dark -> darkColorScheme(
            primary = context.themeColor(android.R.attr.colorAccent, Color(0xFF90CAF9)),
            secondary = context.themeColor(android.R.attr.colorAccent, Color(0xFF90CAF9))
        )
        else -> lightColorScheme(
            primary = context.themeColor(android.R.attr.colorAccent, Color(0xFF1976D2)),
            secondary = context.themeColor(android.R.attr.colorAccent, Color(0xFF1976D2))
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
