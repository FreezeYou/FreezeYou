package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;

public class UriFreezeActivity extends Activity {

    private static final int MODE_FREEZE = 11;
    private static final int MODE_UNFREEZE = 21;
    private static final int MODE_FUF = 31;
    private static final int MODE_UNFREEZEANDRUN = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void init() {
        Intent intent = getIntent();
        String pkgName;
        if (intent != null) {
            if ("freezeyou".equals(intent.getScheme())) {
                Uri dataUri = intent.getData();
                if (dataUri != null) {
                    String action = dataUri.getQueryParameter("action");
                    if (action == null || "".equals(action))
                        action = "fuf";
                    pkgName = dataUri.getQueryParameter("pkgName");

                    switch (action) {
                        case "freeze":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FREEZE);
                            break;
                        case "unfreeze":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_UNFREEZE);
                            break;
                        case "fuf":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FUF);
                            break;
                        case "unFreezeAndRun":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_UNFREEZEANDRUN);
                            break;
                        default://按照 fuf 方案执行
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FUF);
                            break;
                    }

                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void checkAndCreateUserCheckDialog(final Intent intent, final String pkgName, final int mode) {

        boolean suitableForAutoAllow = mode != MODE_FUF;

        ObsdAlertDialog obsdAlertDialog = new ObsdAlertDialog(this);

        String refererPackageLabel;
        if (Build.VERSION.SDK_INT >= 22
                && intent.getParcelableExtra(Intent.EXTRA_REFERRER) == null
                && intent.getStringExtra(Intent.EXTRA_REFERRER_NAME) == null) {
            Uri referrerUri = getReferrer();
            if (referrerUri != null && "android-app".equals(referrerUri.getScheme())) {
                refererPackageLabel =
                        getApplicationLabel(
                                UriFreezeActivity.this, null, null,
                                referrerUri.getEncodedSchemeSpecificPart().substring(2));
                if (refererPackageLabel.equals(getString(R.string.uninstalled))) {
                    refererPackageLabel = getString(R.string.unknown);
                }
            } else {
                refererPackageLabel = getString(R.string.unknown);
            }
        } else {
            refererPackageLabel = getString(R.string.unknown);
        }

        if (suitableForAutoAllow) {
            View checkBoxView = View.inflate(this, R.layout.checkbox, null);//https://stackoverflow.com/questions/9763643/how-to-add-a-check-box-to-an-alert-dialog
            CheckBox checkBox = checkBoxView.findViewById(R.id.checkBox);
            if (refererPackageLabel.equals(getString(R.string.unknown))) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setText(String.format("总是允许 %1$s", refererPackageLabel));
            }
            obsdAlertDialog.setView(checkBoxView);
        }
        obsdAlertDialog.setTitle("标题");
        obsdAlertDialog.setMessage(pkgName);
        obsdAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "允许", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (((ObsdAlertDialog) dialog).isObsd()) {
                    AlertDialogUtils.buildAlertDialog(
                            UriFreezeActivity.this,
                            android.R.drawable.ic_dialog_alert,
                            R.string.alert_isObsd,
                            R.string.dangerous)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkAndCreateUserCheckDialog(intent, pkgName, mode);
                                }
                            })
                            .create().show();
                } else {
                    CheckBox checkBox = ((ObsdAlertDialog) dialog).findViewById(R.id.checkBox);
                    if (checkBox != null) {
                        checkBox.isChecked();
                    }

                    finish();
                }
            }
        });
        obsdAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastUtils.showToast(UriFreezeActivity.this, "拒绝");
                finish();
            }
        });
        obsdAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastUtils.showToast(UriFreezeActivity.this, "取消");
                finish();
            }
        });
        obsdAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        obsdAlertDialog.show();
        Window w = obsdAlertDialog.getWindow();
        if (w != null) {
            View v = (View) w.findViewById(android.R.id.custom).getParent();
            if (v != null) {
                v.setMinimumHeight(0);
            }
        }
    }

}
