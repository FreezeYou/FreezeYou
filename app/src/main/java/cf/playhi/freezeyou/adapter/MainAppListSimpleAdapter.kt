package cf.playhi.freezeyou.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.ui.compose.DrawableImage
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.ui.compose.ResourceDrawableImage

class MainAppListSimpleAdapter(
    private val context: Context,
    private val appList: MutableList<MutableMap<String, Any?>>,
    private val checkedPackages: List<String>,
    private val gridMode: Boolean
) : BaseAdapter() {
    override fun getCount(): Int = appList.size
    override fun getItem(position: Int): MutableMap<String, Any?> = appList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun replaceAllInFormerArrayList(list: List<MutableMap<String, Any?>>): Boolean {
        appList.clear()
        val changed = appList.addAll(list)
        notifyDataSetChanged()
        return changed
    }

    fun getStoredArrayList(): MutableList<MutableMap<String, Any?>> = appList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val composeView = convertView as? ComposeView ?: ComposeView(context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
        val item = appList[position]
        composeView.setContent {
            FreezeYouTheme {
                val selected = item["PackageName"] in checkedPackages
                val background = if (selected) Color.Gray.copy(alpha = 0.25f) else Color.Transparent
                if (gridMode) GridItem(item, background) else ListItem(item, background)
            }
        }
        return composeView
    }

    @Composable
    private fun ListItem(item: Map<String, Any?>, background: Color) {
        Row(
            Modifier.fillMaxWidth().background(background).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemImage(item["Img"], Modifier.size(48.dp))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(item["Name"].toString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item["PackageName"].toString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            (item["isFrozen"] as? Int)?.let {
                ResourceDrawableImage(it, null, Modifier.size(24.dp))
            }
        }
    }

    @Composable
    private fun GridItem(item: Map<String, Any?>, background: Color) {
        Column(
            Modifier.fillMaxWidth().background(background).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ItemImage(item["Img"], Modifier.size(52.dp))
            Text(
                item["Name"].toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun ItemImage(value: Any?, modifier: Modifier) {
        when (value) {
            is Bitmap -> Image(value.asImageBitmap(), null, modifier)
            is Drawable -> DrawableImage(value, null, modifier)
        }
    }
}
