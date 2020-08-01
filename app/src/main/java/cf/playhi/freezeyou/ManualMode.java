package cf.playhi.freezeyou;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.fuf.FUFSinglePackage;
import cf.playhi.freezeyou.utils.FUFUtils;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class ManualMode extends FreezeYouBaseActivity {

    static int selectedMode = -1;
    static int selectedModeCheckedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manualmode);
        processActionBar(getActionBar());

        final EditText packageNameEditText = findViewById(R.id.manualMode_packageNameEditText);
        final Button selectFUFModeButton = findViewById(R.id.manualMode_selectFUFMode_button);
        final Button disableButton = findViewById(R.id.manualMode_disable_button);
        final Button enableButton = findViewById(R.id.manualMode_enable_button);
        final Context context = getApplicationContext();

        String[][] modeSelections = {
                getResources().getStringArray(R.array.selectFUFModeSelection),
                getResources().getStringArray(R.array.selectFUFModeSelectionValues)
        };

        packageNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    disableButton.setEnabled(false);
                    enableButton.setEnabled(false);
                } else {
                    disableButton.setEnabled(true);
                    enableButton.setEnabled(true);
                }
            }
        });

        selectFUFModeButton.setOnClickListener(view ->
                new AlertDialog.Builder(ManualMode.this)
                        .setTitle(R.string.selectFUFMode)
                        .setSingleChoiceItems(
                                modeSelections[0],
                                selectedModeCheckedPosition,
                                (dialog, which) -> {
                                    selectedModeCheckedPosition = which;
                                    selectedMode = Integer.parseInt(modeSelections[1][which]);
                                    selectFUFModeButton.setText(modeSelections[0][which]);
                                    dialog.dismiss();
                                }
                        )
                        .setNegativeButton(R.string.cancel, null)
                        .show()
        );
        disableButton.setOnClickListener(view ->
                processFUFOperation(packageNameEditText.getText().toString(), context, true)
        );
        enableButton.setOnClickListener(view ->
                processFUFOperation(packageNameEditText.getText().toString(), context, false)
        );
    }

    private void processFUFOperation(String pkgName, Context context, boolean freeze) {
        FUFSinglePackage fufSinglePackage = new FUFSinglePackage(context);
        fufSinglePackage.setActionMode(
                freeze ? FUFSinglePackage.ACTION_MODE_FREEZE : FUFSinglePackage.ACTION_MODE_UNFREEZE);
        fufSinglePackage.setAPIMode(selectedMode);
        fufSinglePackage.setSinglePackageName(pkgName);
        FUFUtils.preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                context, fufSinglePackage.commit(), true);
    }
}
