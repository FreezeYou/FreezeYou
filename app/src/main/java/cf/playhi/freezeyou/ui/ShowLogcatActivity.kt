package cf.playhi.freezeyou.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.viewmodel.ShowLogcatViewModel
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme

class ShowLogcatActivity : FreezeYouBaseActivity() {

    private val viewModel: ShowLogcatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        var logContent by mutableStateOf(getString(R.string.loading___))
        setContent {
            FreezeYouTheme {
                OutlinedTextField(
                    value = logContent,
                    onValueChange = { logContent = it },
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )
            }
        }

        viewModel.getLog().observe(this) { content ->
            logContent = content
        }

        viewModel.loadLog()
    }
}
