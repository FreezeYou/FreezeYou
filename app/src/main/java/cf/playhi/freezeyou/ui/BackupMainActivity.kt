package cf.playhi.freezeyou.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.BackupUtils.getExportContent
import cf.playhi.freezeyou.utils.ClipboardUtils
import cf.playhi.freezeyou.utils.GZipUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils
import cf.playhi.freezeyou.ui.compose.ActionButton
import cf.playhi.freezeyou.ui.compose.EqualButtons
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.ui.compose.SectionDivider

class BackupMainActivity : FreezeYouBaseActivity() {
    //    Camera mCamera = null; 先把文本方式稳定下来，再做 QRCode
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        setContent {
            FreezeYouTheme {
                var content by remember { mutableStateOf("") }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
                ) {
                    EqualButtons {
                        ActionButton(stringResource(android.R.string.copy)) {
                            val copied = ClipboardUtils.copyToClipboard(applicationContext, content)
                            ToastUtils.showToast(
                                this@BackupMainActivity,
                                if (copied) R.string.success else R.string.failed
                            )
                        }
                        ActionButton(stringResource(android.R.string.paste)) {
                            content = ClipboardUtils.getClipboardItemText(applicationContext).toString()
                        }
                    }
                    EqualButtons {
                        ActionButton(stringResource(R.string.expt)) {
                            content = GZipUtils.gzipCompress(getExportContent(applicationContext))
                        }
                        ActionButton(stringResource(R.string.impt)) {
                            startActivity(
                                Intent(
                                    this@BackupMainActivity,
                                    BackupImportChooserActivity::class.java
                                ).putExtra(
                                    "jsonObjectString",
                                    GZipUtils.gzipDecompress(content)
                                )
                            )
                        }
                    }
                    SectionDivider()
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
