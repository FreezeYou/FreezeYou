package cf.playhi.freezeyou.ui;

import android.os.Bundle;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.AlertDialogUtils;

import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.doLockScreen;

public class AskLockScreenActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        AlertDialogUtils.buildAlertDialog(this, R.mipmap.ic_launcher_new_round, R.string.askIfLockScreen, R.string.notice)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    doLockScreen(AskLockScreenActivity.this);
                    finish();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> finish())
                .setOnCancelListener(dialogInterface -> finish())
                .create().show();
    }
}
