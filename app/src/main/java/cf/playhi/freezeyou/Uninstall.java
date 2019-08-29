package cf.playhi.freezeyou;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.AlertDialogUtils;
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;

import static cf.playhi.freezeyou.ThemeUtils.processAddTranslucent;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.VersionUtils.checkUpdate;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class Uninstall extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        processAddTranslucent(this);
        super.onCreate(savedInstanceState);
        AlertDialogUtils.buildAlertDialog(this, R.mipmap.ic_launcher_round, R.string.removeNoRootCaution, R.string.plsConfirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DevicePolicyManagerUtils.isDeviceOwner(getApplicationContext())) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    DevicePolicyManagerUtils.getDevicePolicyManager(getApplicationContext()).clearDeviceOwnerApp("cf.playhi.freezeyou");
                                    showToast(getApplicationContext(), R.string.success);
                                } else {
                                    showToast(getApplicationContext(), R.string.noRootNotActivated);
                                }
                            } catch (Exception e) {
                                showToast(getApplicationContext(), R.string.failed);
                            }
                        } else {
                            showToast(getApplicationContext(), R.string.noRootNotActivated);
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkUpdate(getApplicationContext());
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }
}
