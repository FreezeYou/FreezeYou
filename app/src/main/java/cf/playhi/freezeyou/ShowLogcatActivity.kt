package cf.playhi.freezeyou

import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import cf.playhi.freezeyou.ThemeUtils.processActionBar
import cf.playhi.freezeyou.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity

class ShowLogcatActivity : FreezeYouBaseActivity() {

    private val viewModel: ShowLogcatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showlogcat_activity)
        processActionBar(supportActionBar)

        val editText = findViewById<EditText>(R.id.sla_log_editText)

        editText.setText(R.string.loading___)

        viewModel.getLog().observe(this) { content ->
            editText.setText(content)
            editText.setSelection(editText.text.length)
        }

        viewModel.loadLog()
    }
}