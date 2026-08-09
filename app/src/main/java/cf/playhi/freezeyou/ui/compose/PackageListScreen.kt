package cf.playhi.freezeyou.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class PackageListEntry(val label: String, val packageName: String)

@Composable
fun PackageListScreen(entries: List<PackageListEntry>, onClick: ((PackageListEntry) -> Unit)? = null) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(entries, key = { it.packageName }) { entry ->
            Column(
                Modifier.fillMaxWidth()
                    .then(if (onClick == null) Modifier else Modifier.clickable { onClick(entry) })
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(entry.label)
                Text(entry.packageName)
            }
            HorizontalDivider()
        }
    }
}
