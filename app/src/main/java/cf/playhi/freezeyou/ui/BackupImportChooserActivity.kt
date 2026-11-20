package cf.playhi.freezeyou.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.BackupUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class BackupImportChooserActivity : FreezeYouBaseActivity() {
    private val backupFile: File?
        get() = intent.getStringExtra("backupFile")?.takeIf {
            it.startsWith("restore-") && it.endsWith(".json") && File(it).name == it
        }?.let { File(cacheDir, it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        setTitle(R.string.restore_from_backup)
        val plan = try {
            val content = backupFile?.readText() ?: intent.getStringExtra("jsonObjectString").orEmpty()
            BackupUtils.readRestorePlan(this, JSONObject(content))
        } catch (_: JSONException) {
            null
        } catch (_: IOException) {
            null
        }
        setContent { FreezeYouTheme(readableControls = true) { RestoreChooser(plan) } }
    }

    @Composable
    private fun RestoreChooser(plan: BackupUtils.RestorePlan?) {
        val items = plan?.items.orEmpty()
        var excluded by rememberSaveable { mutableStateOf(intArrayOf()) }
        var applied by rememberSaveable { mutableIntStateOf(-1) }
        val excludedSet = remember(excluded) { excluded.toSet() }
        val count = items.size - excludedSet.size
        Column(Modifier.fillMaxSize()) {
            if (applied >= 0) {
                Text(stringResource(R.string.restore_result, applied, count - applied + (plan?.invalidCount ?: 0)),
                    Modifier.weight(1f).padding(16.dp))
            } else if (items.isEmpty()) {
                Text(stringResource(if (plan == null || plan.invalidCount > 0) R.string.backup_invalid else R.string.backup_no_items),
                    Modifier.weight(1f).padding(16.dp))
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    itemsIndexed(items) { index, item ->
                        val checked = index !in excludedSet
                        Row(
                            Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox) {
                                excluded = (if (it) excludedSet - index else excludedSet + index).toIntArray()
                            }.heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked, onCheckedChange = null)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(item.title)
                                Text(stringResource(item.sectionLabel), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (applied >= 0) {
                    Button(onClick = { finish() }) { Text(stringResource(android.R.string.ok)) }
                } else {
                    TextButton(onClick = { finish() }) { Text(stringResource(android.R.string.cancel)) }
                    Button(enabled = count > 0, onClick = {
                        if (applied < 0) applied = BackupUtils.restoreSelected(
                            this@BackupImportChooserActivity, items.filterIndexed { i, _ -> i !in excludedSet }
                        )
                    }) { Text(pluralStringResource(R.plurals.restore_items, count, count)) }
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) backupFile?.delete()
        super.onDestroy()
    }
}
