package cf.playhi.freezeyou.ui.compose

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable

@Composable
fun DrawableImage(drawable: Drawable?, contentDescription: String?, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) { drawable?.let(::getBitmapFromDrawable) }
    if (bitmap != null) {
        Image(bitmap.asImageBitmap(), contentDescription, modifier)
    }
}

@Composable
fun ResourceDrawableImage(
    @DrawableRes resourceId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = remember(context, resourceId) {
        ContextCompat.getDrawable(context, resourceId)
    }
    DrawableImage(drawable, contentDescription, modifier)
}

@Composable
fun AppListItem(
    icon: Drawable?,
    title: String,
    subtitle: String,
    @DrawableRes statusIcon: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawableImage(icon, title, Modifier.size(48.dp))
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (statusIcon != null) {
            ResourceDrawableImage(statusIcon, null, Modifier.size(24.dp))
        }
    }
}
