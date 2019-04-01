package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import net.grandcentrix.tray.AppPreferences;

import java.util.List;

import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;

public class UriFreezeActivity extends Activity {

    private static final int MODE_FREEZE = 11;
    private static final int MODE_UNFREEZE = 21;
    private static final int MODE_FUF = 31;
    private static final int MODE_UNFREEZEANDRUN = 22;
    private static final String ILLEGALPKGNAME = "Fy^&IllegalPN*@!1024`+=：:";

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

                    switch (action.toLowerCase()) {
                        case "freeze":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FREEZE);
                            break;
                        case "unfreeze":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_UNFREEZE);
                            break;
                        case "fuf":
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FUF);
                            break;
                        case "unfreezeandrun"://unFreezeAndRun
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

        final boolean suitableForAutoAllow = mode != MODE_FUF;
        final boolean isFrozen = Support.realGetFrozenStatus(this, pkgName, getPackageManager());

        ObsdAlertDialog obsdAlertDialog = new ObsdAlertDialog(this);

        String refererPackageLabel;
        final String refererPackage;
        if (Build.VERSION.SDK_INT >= 22
                && intent.getParcelableExtra(Intent.EXTRA_REFERRER) == null
                && intent.getStringExtra(Intent.EXTRA_REFERRER_NAME) == null) {
            Uri referrerUri = getReferrer();
            if (referrerUri != null && "android-app".equals(referrerUri.getScheme())) {
                refererPackage = referrerUri.getEncodedSchemeSpecificPart().substring(2);
                refererPackageLabel =
                        getApplicationLabel(
                                UriFreezeActivity.this,
                                null,
                                ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, this),
                                refererPackage
                        );
                if (refererPackageLabel.equals(getString(R.string.uninstalled))) {
                    refererPackageLabel = ILLEGALPKGNAME;
                }
            } else {
                refererPackageLabel = ILLEGALPKGNAME;
                refererPackage = ILLEGALPKGNAME;
            }
        } else {
            refererPackageLabel = ILLEGALPKGNAME;
            refererPackage = ILLEGALPKGNAME;
        }

        if (suitableForAutoAllow) {
            //Check AutoAllow
            AppPreferences sp = new AppPreferences(this);
            String originData = sp.getString("uriAutoAllowPkgs_allows", "");
            if (originData != null &&
                    !ILLEGALPKGNAME.equals(refererPackage) &&
                    MoreUtils.convertToList(originData, ",")
                            .contains(
                                    Base64.encodeToString(
                                            refererPackage.getBytes(),
                                            Base64.DEFAULT))) {
                doSuitableForAutoAllowAllow(mode, pkgName, isFrozen);
            }
            //Init CheckBox
            View checkBoxView = View.inflate(this, R.layout.ufa_dialog_checkbox, null);//https://stackoverflow.com/questions/9763643/how-to-add-a-check-box-to-an-alert-dialog
            CheckBox checkBox = checkBoxView.findViewById(R.id.ufa_dialog_checkBox);
            if (refererPackageLabel.equals(ILLEGALPKGNAME)) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setText(String.format(getString(R.string.alwaysAllow_name), refererPackageLabel));
            }
            obsdAlertDialog.setView(checkBoxView);
        }
        obsdAlertDialog.setTitle(R.string.plsConfirm);
        StringBuilder message = new StringBuilder();
        String nl = System.getProperty("line.separator");
        message.append(getString(R.string.target_colon));
        message.append(nl);
        message.append(getString(R.string.application_colon));
        message.append(
                getApplicationLabel(
                        this, null,
                        null, pkgName));
        message.append(nl);
        message.append(getString(R.string.pkgName_colon));
        message.append(pkgName);
        message.append(nl);
        message.append(nl);
        message.append(getString(R.string.execute_colon));
        message.append(nl);
        switch (mode) {
            case MODE_FREEZE:
                message.append(getString(R.string.freeze));
                break;
            case MODE_UNFREEZE:
                message.append(getString(R.string.unfreeze));
                break;
            case MODE_UNFREEZEANDRUN:
                message.append(getString(R.string.openImmediatelyAfterUF));
                break;
            default:
                message.append(getString(R.string.plsSelect));
                break;
        }
        obsdAlertDialog.setMessage(message);
        obsdAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(suitableForAutoAllow ?
                        R.string.allow :
                        isFrozen ?
                                R.string.unfreeze :
                                R.string.launch),
                new DialogInterface.OnClickListener() {
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
                            CheckBox checkBox = ((ObsdAlertDialog) dialog).findViewById(R.id.ufa_dialog_checkBox);
                            if (checkBox != null && checkBox.isChecked()) {
                                AppPreferences sp = new AppPreferences(UriFreezeActivity.this);
                                String originData = sp.getString("uriAutoAllowPkgs_allows", "");
                                List<String> originData_list = MoreUtils.convertToList(originData, ",");
                                if (!ILLEGALPKGNAME.equals(refererPackage)
                                        &&
                                        (originData == null ||
                                                !originData_list.contains(
                                                        Base64.encodeToString(
                                                                refererPackage.getBytes(), Base64.DEFAULT)))) {
                                    originData_list.add(
                                            Base64.encodeToString(refererPackage.getBytes(), Base64.DEFAULT));
                                    sp.put(
                                            "uriAutoAllowPkgs_allows",
                                            MoreUtils.listToString(originData_list, ",")
                                    );
                                }
                            }
                            if (suitableForAutoAllow) {
                                doSuitableForAutoAllowAllow(mode, pkgName, isFrozen);
                            } else {
                                if (isFrozen) {
                                    Support.processUnfreezeAction(
                                            UriFreezeActivity.this,
                                            pkgName,
                                            null,
                                            null,
                                            true,
                                            false,
                                            UriFreezeActivity.this,
                                            true
                                    );
                                } else {
                                    Support.checkAndStartApp(
                                            UriFreezeActivity.this,
                                            pkgName,
                                            null,
                                            null,
                                            UriFreezeActivity.this,
                                            true
                                    );
                                }
                            }
                        }
                    }
                });
        obsdAlertDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(
                        suitableForAutoAllow ?
                                R.string.reject : R.string.freeze),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (suitableForAutoAllow) {
                            finish();
                        } else {
                            Support.processFreezeAction(
                                    UriFreezeActivity.this,
                                    pkgName,
                                    null,
                                    null,
                                    false,
                                    UriFreezeActivity.this,
                                    true
                            );
                        }
                    }
                });
        obsdAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

    private void doSuitableForAutoAllowAllow(int mode, String pkgName, boolean isFrozen) {
        switch (mode) {
            case MODE_FREEZE:
                if (!isFrozen)
                    Support.processFreezeAction(
                            UriFreezeActivity.this,
                            pkgName,
                            null,
                            null,
                            false,
                            UriFreezeActivity.this,
                            true
                    );
                else
                    finish();
                break;
            case MODE_UNFREEZE:
                if (isFrozen)
                    Support.processUnfreezeAction(
                            UriFreezeActivity.this,
                            pkgName,
                            null,
                            null,
                            false,
                            false,
                            UriFreezeActivity.this,
                            true
                    );
                else
                    finish();
                break;
            case MODE_UNFREEZEANDRUN:
                if (isFrozen)
                    Support.processUnfreezeAction(
                            UriFreezeActivity.this,
                            pkgName,
                            null,
                            null,
                            true,
                            true,
                            UriFreezeActivity.this,
                            true
                    );
                else
                    Support.checkAndStartApp(
                            this,
                            pkgName,
                            null,
                            null,
                            this,
                            true
                    );
                break;
            default:
                finish();
                break;
        }
    }

}
