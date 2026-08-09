package cf.playhi.freezeyou.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

data class PackageListEntry(val label: String, val packageName: String)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PackageListScreen(entries: List<PackageListEntry>, onClick: ((PackageListEntry) -> Unit)? = null) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(entries, key = { it.packageName }) { entry ->
            Column(
                Modifier.fillMaxWidth()
                    .then(if (onClick == null) Modifier else Modifier.clickable { onClick(entry) })
                    .padding(10.dp)
            ) {
                Text(
                    entry.label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
                Text(
                    entry.packageName,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }
}
