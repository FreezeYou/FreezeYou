package cf.playhi.freezeyou.ui

import android.content.DialogInterface
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.viewmodel.ManualModeActivityViewModel
import cf.playhi.freezeyou.ui.compose.ActionButton
import cf.playhi.freezeyou.ui.compose.EqualButtons
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme

class ManualModeActivity : FreezeYouBaseActivity() {

    private val viewModel: ManualModeActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        val context = applicationContext
        val modeSelections = arrayOf(
            resources.getStringArray(R.array.selectFUFModeSelection),
            resources.getStringArray(R.array.selectFUFModeSelectionValues)
        )
        var selectedPosition by mutableIntStateOf(
            viewModel.getSelectedModeCheckedPosition().value ?: -1
        )
        viewModel.getSelectedModeCheckedPosition().observe(this) {
            selectedPosition = it
        }
        setContent {
            FreezeYouTheme {
                var packageName by remember { mutableStateOf(viewModel.getCurrentPackageName()) }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(5.dp)
                ) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = {
                            packageName = it
                            viewModel.setCurrentPackageName(it)
                        },
                        label = { Text(stringResource(R.string.packageName)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            FreezeYouAlertDialogBuilder(this@ManualModeActivity)
                                .setTitle(R.string.selectFUFMode)
                                .setSingleChoiceItems(
                                    modeSelections[0], selectedPosition
                                ) { dialog: DialogInterface, which: Int ->
                                    viewModel.setSelectedModeCheckedPosition(which)
                                    viewModel.setSelectedMode(modeSelections[1][which].toInt())
                                    dialog.dismiss()
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedPosition in modeSelections[0].indices) {
                                modeSelections[0][selectedPosition]
                            } else {
                                stringResource(R.string.selectFUFMode)
                            }
                        )
                    }
                    EqualButtons {
                        ActionButton(stringResource(R.string.freeze), packageName.isNotEmpty()) {
                            viewModel.processFUFOperation(packageName, context, true)
                        }
                        ActionButton(stringResource(R.string.unfreeze), packageName.isNotEmpty()) {
                            viewModel.processFUFOperation(packageName, context, false)
                        }
                    }
                }
            }
        }
    }

}
