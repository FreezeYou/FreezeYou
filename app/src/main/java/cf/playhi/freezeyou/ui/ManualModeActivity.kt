package cf.playhi.freezeyou.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.viewmodel.ManualModeActivityViewModel

class ManualModeActivity : FreezeYouBaseActivity() {

    private val viewModel: ManualModeActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manualmode)
        processActionBar(supportActionBar)

        val packageNameEditText = findViewById<EditText>(R.id.manualMode_packageNameEditText)
        val selectFUFModeButton = findViewById<Button>(R.id.manualMode_selectFUFMode_button)
        val disableButton = findViewById<Button>(R.id.manualMode_disable_button)
        val enableButton = findViewById<Button>(R.id.manualMode_enable_button)
        val context = applicationContext
        val modeSelections = arrayOf(
            resources.getStringArray(R.array.selectFUFModeSelection),
            resources.getStringArray(R.array.selectFUFModeSelectionValues)
        )
        packageNameEditText.setText(viewModel.getCurrentPackageName())
        packageNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.setCurrentPackageName(s.toString())
                if (s.isEmpty()) {
                    disableButton.isEnabled = false
                    enableButton.isEnabled = false
                } else {
                    disableButton.isEnabled = true
                    enableButton.isEnabled = true
                }
            }
        })
        viewModel.getSelectedModeCheckedPosition().observe(this) {
            selectFUFModeButton.text =
                if (it < 0 || it >= modeSelections[0].size) {
                    getString(R.string.selectFUFMode)
                } else {
                    modeSelections[0][it]
                }
        }
        selectFUFModeButton.setOnClickListener {
            FreezeYouAlertDialogBuilder(this@ManualModeActivity)
                .setTitle(R.string.selectFUFMode)
                .setSingleChoiceItems(
                    modeSelections[0],
                    viewModel.getSelectedModeCheckedPosition().value ?: -1
                ) { dialog: DialogInterface, which: Int ->
                    viewModel.setSelectedModeCheckedPosition(which)
                    viewModel.setSelectedMode(modeSelections[1][which].toInt())
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        disableButton.setOnClickListener {
            viewModel.processFUFOperation(
                packageNameEditText.text.toString(),
                context,
                true
            )
        }
        enableButton.setOnClickListener {
            viewModel.processFUFOperation(
                packageNameEditText.text.toString(),
                context,
                false
            )
        }
    }

}