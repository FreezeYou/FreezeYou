package cf.playhi.freezeyou;

import android.content.DialogInterface;
import android.os.Bundle;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.doLockScreen;

public class AskLockScreenActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        buildAlertDialog(this, R.mipmap.ic_launcher_new_round, R.string.askIfLockScreen, R.string.notice)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doLockScreen(AskLockScreenActivity.this);
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .create().show();
    }
}
