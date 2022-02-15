package cf.playhi.freezeyou.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.fuf.FUFSinglePackage
import cf.playhi.freezeyou.utils.FUFUtils.preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess

class ManualModeActivity : FreezeYouBaseActivity() {

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
        packageNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    disableButton.isEnabled = false
                    enableButton.isEnabled = false
                } else {
                    disableButton.isEnabled = true
                    enableButton.isEnabled = true
                }
            }
        })
        selectFUFModeButton.setOnClickListener {
            AlertDialog.Builder(this@ManualModeActivity)
                .setTitle(R.string.selectFUFMode)
                .setSingleChoiceItems(
                    modeSelections[0],
                    selectedModeCheckedPosition
                ) { dialog: DialogInterface, which: Int ->
                    selectedModeCheckedPosition = which
                    selectedMode = modeSelections[1][which].toInt()
                    selectFUFModeButton.text = modeSelections[0][which]
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        disableButton.setOnClickListener {
            processFUFOperation(
                packageNameEditText.text.toString(),
                context,
                true
            )
        }
        enableButton.setOnClickListener {
            processFUFOperation(
                packageNameEditText.text.toString(),
                context,
                false
            )
        }
    }

    private fun processFUFOperation(pkgName: String, context: Context, freeze: Boolean) {
        preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
            context,
            FUFSinglePackage(
                context,
                pkgName,
                if (freeze) FUFSinglePackage.ACTION_MODE_FREEZE else FUFSinglePackage.ACTION_MODE_UNFREEZE,
                selectedMode
            ).commit(),
            true
        )
    }

    companion object {
        private var selectedMode = -1
        private var selectedModeCheckedPosition = -1
    }
}