package cf.playhi.freezeyou.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.BackupUtils.getExportContent
import cf.playhi.freezeyou.utils.ClipboardUtils
import cf.playhi.freezeyou.utils.GZipUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class BackupDraft : ViewModel() {
    var text by mutableStateOf("")
}

class BackupMainActivity : FreezeYouBaseActivity() {
    private val draft: BackupDraft by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        setContent {
            FreezeYouTheme(readableControls = true) {
                var error by rememberSaveable { mutableIntStateOf(0) }
                Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    OutlinedTextField(
                        value = draft.text,
                        onValueChange = { draft.text = it; error = 0 },
                        label = { Text(stringResource(R.string.backup_text)) },
                        isError = error != 0, minLines = 3, maxLines = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != 0) Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                    Row {
                        TextButton(onClick = {
                            draft.text = ClipboardUtils.getClipboardItemText(applicationContext)?.toString().orEmpty()
                            error = 0
                        }) { Text(stringResource(android.R.string.paste)) }
                        TextButton(enabled = draft.text.isNotEmpty(), onClick = {
                            val copied = ClipboardUtils.copyToClipboard(applicationContext, draft.text)
                            ToastUtils.showToast(this@BackupMainActivity, if (copied) R.string.success else R.string.failed)
                        }) { Text(stringResource(android.R.string.copy)) }
                    }
                    Button(enabled = draft.text.isNotBlank(), onClick = {
                        error = reviewBackup()
                    }) { Text(stringResource(R.string.review_restore)) }
                    TextButton(onClick = {
                        draft.text = GZipUtils.gzipCompress(getExportContent(applicationContext))
                        error = 0
                    }) { Text(stringResource(R.string.create_backup)) }
                }
            }
        }
    }

    private fun reviewBackup(): Int {
        val json = GZipUtils.gzipDecompress(draft.text.trim())
        try {
            JSONObject(json)
        } catch (_: JSONException) {
            return R.string.backup_invalid
        }
        // A backup containing long lists can exceed the Binder limit for an Intent extra.
        return try {
            val file = File.createTempFile("restore-", ".json", cacheDir)
            file.writeText(json)
            startActivity(Intent(this, BackupImportChooserActivity::class.java).putExtra("backupFile", file.name))
            0
        } catch (_: IOException) {
            R.string.failed
        }
    }
}
