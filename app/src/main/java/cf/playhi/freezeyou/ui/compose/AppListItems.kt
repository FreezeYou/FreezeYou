package cf.playhi.freezeyou.ui.compose

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.R

private class DrawablePainter(drawable: Drawable) : Painter() {
    private val drawable = drawable.mutate()

    override val intrinsicSize: Size = if (
        drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0
    ) {
        Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
    } else {
        Size.Unspecified
    }

    override fun DrawScope.onDraw() {
        val oldBounds = drawable.copyBounds()
        drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
        drawable.bounds = oldBounds
    }
}

@Composable
fun DrawableImage(drawable: Drawable?, contentDescription: String?, modifier: Modifier = Modifier) {
    val painter = remember(drawable) { drawable?.let(::DrawablePainter) }
    if (painter != null) {
        Image(painter, contentDescription, modifier)
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
@OptIn(ExperimentalFoundationApi::class)
fun AppListItem(
    icon: Drawable?,
    title: String,
    subtitle: String,
    @DrawableRes statusIcon: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawableImage(icon, title, Modifier.size(40.dp))
        Column(Modifier.weight(1f).padding(horizontal = 5.dp)) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Text(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 12.sp,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
        }
        if (statusIcon != null) {
            ResourceDrawableImage(
                statusIcon,
                stringResource(R.string.status),
                Modifier.size(10.dp)
            )
        }
    }
}
