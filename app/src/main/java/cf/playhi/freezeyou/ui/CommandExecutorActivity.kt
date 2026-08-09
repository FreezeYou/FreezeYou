package cf.playhi.freezeyou.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ThemeUtils

class CommandExecutorActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.processSetTheme(this)
        super.onCreate(savedInstanceState)
        ThemeUtils.processActionBar(supportActionBar)
        setContent {
            FreezeYouTheme {
                var input by remember { mutableStateOf("") }
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { finish() }) {
                            Text(stringResource(R.string.finish))
                        }
                    }
                }
            }
        }
    }
}
