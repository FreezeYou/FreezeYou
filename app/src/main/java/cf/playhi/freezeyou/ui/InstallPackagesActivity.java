package cf.playhi.freezeyou.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.app.ObsdAlertDialog;
import cf.playhi.freezeyou.service.InstallPackagesService;
import cf.playhi.freezeyou.utils.AccessibilityUtils;
import cf.playhi.freezeyou.utils.AlertDialogUtils;
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;
import cf.playhi.freezeyou.utils.FUFUtils;
import cf.playhi.freezeyou.utils.FileUtils;
import cf.playhi.freezeyou.utils.InstallPackagesUtils;
import cf.playhi.freezeyou.utils.MoreUtils;
import cf.playhi.freezeyou.utils.ServiceUtils;

import static cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilderKt.FreezeYouAlertDialogBuilder;
import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.notAllowInstallWhenIsObsd;
import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.tryToAvoidUpdateWhenUsing;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

/**
 * Install and uninstall
 */
public class InstallPackagesActivity extends FreezeYouBaseActivity {
    private static final String ILLEGALPKGNAME = "Fy^&IllegalPN*@!128`+=：:,.[";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);

        init();
    }

    private void clearTempFile(String filePath) {
        InstallPackagesUtils.deleteTempFile(this, filePath, false);
    }

    private void init() {

        final Intent intent = getIntent();
        final Uri packageUri = intent.getData();

        if (packageUri == null) {
            showToast(this, String.format(getString(R.string.invalidUriToast), "null"));
            finish();
            return;
        }

        String scheme = packageUri.getScheme();

        if ((!ContentResolver.SCHEME_FILE.equals(scheme)
                && !ContentResolver.SCHEME_CONTENT.equals(scheme)) && !"package".equals(scheme)) {
            showToast(this, String.format(getString(R.string.invalidUriToast), packageUri));
            finish();
            return;
        }

        final boolean install =
                !(Intent.ACTION_DELETE.equals(intent.getAction()) ||
                        Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction()));

        final String apkFilePath;

        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            // Check Storage Permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder b = AlertDialogUtils
                            .buildAlertDialog(
                                    this,
                                    R.drawable.ic_warning,
                                    R.string.needStoragePermission,
                                    R.string.notice)
                            .setOnCancelListener(dialog -> finish())
                            .setPositiveButton(R.string.okay, (dialog, which) ->
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            301
                                    )
                            )
                            .setNegativeButton(R.string.cancel, (dialog, which) -> finish());
                    if (!isFinishing()) {
                        b.show();
                    }
                } else {
                    apkFilePath = packageUri.getPath();
                    checkAutoAndPrepareInstallDialog(install, packageUri, apkFilePath);
                }
            } else {
                apkFilePath = packageUri.getEncodedPath();
                checkAutoAndPrepareInstallDialog(install, packageUri, apkFilePath);
            }
        } else {
            String apkFileName = "package" + new Date().getTime() + "F.apk";
            apkFilePath = getExternalCacheDir() + File.separator + "ZDF-" + apkFileName;

            checkAutoAndPrepareInstallDialog(install, packageUri, apkFilePath);
        }

    }

    private void checkAutoAndPrepareInstallDialog(boolean install, Uri packageUri, String apkFilePath) {

        final String fromPkgLabel;
        final String fromPkgName;
        if (Build.VERSION.SDK_INT >= 22) {
            Uri referrerUri = getReferrer();
            if (referrerUri == null || !"android-app".equals(referrerUri.getScheme())) {
                fromPkgLabel = ILLEGALPKGNAME;
                fromPkgName = ILLEGALPKGNAME;
            } else {
                fromPkgName = referrerUri.getEncodedSchemeSpecificPart().substring(2);
                String refererPackageLabel =
                        getApplicationLabel(
                                InstallPackagesActivity.this,
                                null, null,
                                fromPkgName
                        );
                if (refererPackageLabel.equals(getString(R.string.uninstalled))) {
                    fromPkgLabel = ILLEGALPKGNAME;
                } else {
                    fromPkgLabel = refererPackageLabel;
                }
            }
        } else {
            fromPkgLabel = ILLEGALPKGNAME;
            fromPkgName = ILLEGALPKGNAME;
        }

        prepareInstallDialog(install, packageUri, apkFilePath, fromPkgLabel, fromPkgName);
    }

    private void prepareInstallDialog(final boolean install, final Uri packageUri, final String apkFilePath, final String fromPkgLabel, final String fromPkgName) {
        final StringBuilder alertDialogMessage = new StringBuilder();

        if (isFinishing()) return;

        final ProgressDialog progressDialog =
                ProgressDialog.show(this, getString(R.string.plsWait), getString(R.string.loading___));
        final String nl = System.getProperty("line.separator");
        if (install) {
            new Thread(() -> {
                try {
                    if (apkFilePath.startsWith(getExternalCacheDir() + File.separator + "ZDF-")) {
                        InputStream in = getContentResolver().openInputStream(packageUri);
                        if (in == null) {
                            finish();
                            return;
                        }
                        FileUtils.copyFile(in, apkFilePath);
                    }

                    PackageManager pm = getPackageManager();
                    final PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0);
                    packageInfo.applicationInfo.sourceDir = apkFilePath;
                    packageInfo.applicationInfo.publicSourceDir = apkFilePath;

                    //Check AutoAllow
                    AppPreferences sp = new AppPreferences(InstallPackagesActivity.this);
                    String originData = sp.getString("installPkgs_autoAllowPkgs_allows", "");
                    if (originData != null
                            && !ILLEGALPKGNAME.equals(fromPkgLabel)
                            && MoreUtils.convertToList(originData, ",").contains(
                            Base64.encodeToString(fromPkgName.getBytes(), Base64.DEFAULT))) {
                        //Allow
                        ServiceUtils.startService(
                                InstallPackagesActivity.this,
                                new Intent(InstallPackagesActivity.this, InstallPackagesService.class)
                                        .putExtra("install", true)
                                        .putExtra("packageUri", packageUri)
                                        .putExtra("apkFilePath", apkFilePath)
                                        .putExtra("packageInfo", packageInfo)
                                        .putExtra("waitForLeaving",
                                                tryToAvoidUpdateWhenUsing.getValue(null)
                                        )
                        );

                        if (isFinishing()) return;

                        runOnUiThread(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.cancel();
                            finish();
                        });
                    }

                    alertDialogMessage.append(getString(R.string.requestFromPackage_colon));
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(
                            ILLEGALPKGNAME.equals(fromPkgLabel) ?
                                    getString(R.string.unknown) : fromPkgLabel);
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(getString(R.string.installPackage_colon));
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(
                            String.format(
                                    getString(R.string.application_colon_app),
                                    pm.getApplicationLabel(packageInfo.applicationInfo)
                            )
                    );
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(
                            String.format(
                                    getString(R.string.pkgName_colon_pkgName),
                                    packageInfo.packageName
                            )
                    );
                    try {
                        PackageInfo pi =
                                getPackageManager().getPackageInfo(
                                        packageInfo.packageName,
                                        PackageManager.GET_UNINSTALLED_PACKAGES
                                );
                        alertDialogMessage.append(nl);
                        alertDialogMessage.append(
                                String.format(
                                        getString(R.string.existed_colon_vN_longVC),
                                        pi.versionName,
                                        Build.VERSION.SDK_INT < 28 ?
                                                Integer.toString(pi.versionCode) :
                                                Long.toString(pi.getLongVersionCode())
                                )
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(
                            String.format(
                                    getString(R.string.version_colon_vN_longVC),
                                    packageInfo.versionName,
                                    Build.VERSION.SDK_INT < 28 ?
                                            Integer.toString(packageInfo.versionCode) :
                                            Long.toString(packageInfo.getLongVersionCode())
                            )
                    );
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(nl);
                    alertDialogMessage.append(getString(R.string.whetherAllow));

                    if (isFinishing()) return;

                    runOnUiThread(() -> showInstallDialog(
                            progressDialog, 1,
                            alertDialogMessage, apkFilePath,
                            packageUri, fromPkgLabel, fromPkgName, packageInfo
                    ));
                } catch (Exception e) {
                    alertDialogMessage.append(
                            String.format(
                                    getString(R.string.cannotInstall_colon_msg),
                                    e.getLocalizedMessage()
                            )
                    );

                    if (isFinishing()) return;

                    runOnUiThread(() -> showInstallDialog(
                            progressDialog, 2,
                            alertDialogMessage, apkFilePath,
                            packageUri, fromPkgLabel, fromPkgName, null
                    ));
                }
            }).start();
        } else {
            String packageName = packageUri.getEncodedSchemeSpecificPart();
            if (packageName == null) {
                showToast(this, String.format(getString(R.string.invalidUriToast), packageUri));
                finish();
                return;
            }
            alertDialogMessage.append(getString(R.string.requestFromPackage_colon));
            alertDialogMessage.append(nl);
            alertDialogMessage.append(
                    ILLEGALPKGNAME.equals(fromPkgLabel) ?
                            getString(R.string.unknown) : fromPkgLabel);
            alertDialogMessage.append(nl);
            alertDialogMessage.append(nl);
            alertDialogMessage.append(getString(R.string.uninstallPackage_colon));
            alertDialogMessage.append(nl);
            alertDialogMessage.append(
                    String.format(
                            getString(R.string.application_colon_app),
                            getApplicationLabel(this, null, null, packageName)
                    )
            );
            alertDialogMessage.append(nl);
            alertDialogMessage.append(
                    String.format(
                            getString(R.string.pkgName_colon_pkgName),
                            packageName
                    )
            );
            alertDialogMessage.append(nl);
            alertDialogMessage.append(getString(R.string.whetherAllow));
            showInstallDialog(
                    progressDialog, 0,
                    alertDialogMessage, apkFilePath,
                    packageUri, fromPkgLabel, fromPkgName, null
            );
        }
    }

    //install: 0-uninstall, 1-install, 2-failed.
    private void showInstallDialog(final ProgressDialog progressDialog, final int install, final CharSequence alertDialogMessage, final String apkFilePath, final Uri packageUri, final String fromPkgLabel, final String fromPkgName, final PackageInfo processedPackageInfo) {
        final ObsdAlertDialog installPackagesAlertDialog = new ObsdAlertDialog(this);
        if (install == 1) {
            //Init CheckBox
            View checkBoxView = View.inflate(this, R.layout.ipa_dialog_checkbox, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.ipa_dialog_checkBox);
            if (fromPkgLabel.equals(ILLEGALPKGNAME)) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setText(String.format(getString(R.string.alwaysAllow_name), fromPkgLabel));
            }
            installPackagesAlertDialog.setView(checkBoxView);
        }

        switch (install) {
            case 0:
                installPackagesAlertDialog.setTitle(R.string.uninstall);
                break;
            case 1:
                installPackagesAlertDialog.setTitle(R.string.install);
                break;
            case 2:
                installPackagesAlertDialog.setTitle(R.string.failed);
                break;
            default:
                break;
        }

        final boolean preDefinedTryToAvoidUpdateWhenUsing =
                tryToAvoidUpdateWhenUsing.getValue(null);

        installPackagesAlertDialog.setMessage(alertDialogMessage);
        installPackagesAlertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.yes),
                (dialog, which) -> {
                    if (notAllowInstallWhenIsObsd.getValue(null)
                            && installPackagesAlertDialog.isObsd()) {
                        AlertDialogUtils.buildAlertDialog(
                                        InstallPackagesActivity.this,
                                        R.drawable.ic_warning,
                                        R.string.alert_isObsd,
                                        R.string.dangerous)
                                .setPositiveButton(R.string.retry, (dialog1, which1) ->
                                        showInstallDialog(
                                                progressDialog, install,
                                                alertDialogMessage, apkFilePath, packageUri,
                                                fromPkgLabel, fromPkgName, processedPackageInfo)
                                )
                                .setNegativeButton(R.string.cancel, (dialog12, which12) -> {
                                    if (install != 0) clearTempFile(apkFilePath);
                                    finish();
                                })
                                .setOnCancelListener(dialog13 ->
                                        showInstallDialog(progressDialog, install,
                                                alertDialogMessage, apkFilePath, packageUri,
                                                fromPkgLabel, fromPkgName, processedPackageInfo)
                                )
                                .create().show();
                    } else {
                        if (install == 1) {
                            CheckBox checkBox = ((ObsdAlertDialog) dialog).findViewById(R.id.ipa_dialog_checkBox);
                            if (checkBox != null && checkBox.isChecked()) {
                                AppPreferences sp = new AppPreferences(InstallPackagesActivity.this);
                                String originData = sp.getString("installPkgs_autoAllowPkgs_allows", "");
                                List<String> originData_list = MoreUtils.convertToList(originData, ",");
                                if (!ILLEGALPKGNAME.equals(fromPkgLabel)
                                        &&
                                        (originData == null ||
                                                !MoreUtils.convertToList(originData, ",").contains(
                                                        Base64.encodeToString(
                                                                fromPkgName.getBytes(), Base64.DEFAULT)))) {
                                    originData_list.add(
                                            Base64.encodeToString(fromPkgName.getBytes(), Base64.DEFAULT));
                                    sp.put(
                                            "installPkgs_autoAllowPkgs_allows",
                                            MoreUtils.listToString(originData_list, ",")
                                    );
                                }
                            }
                        }
                        if (install == 2) {
                            clearTempFile(apkFilePath);
                            finish();
                        } else {
                            if (DevicePolicyManagerUtils
                                    .isDeviceOwner(InstallPackagesActivity.this) ||
                                    FUFUtils.checkRootPermission()) {
                                ServiceUtils.startService(
                                        InstallPackagesActivity.this,
                                        new Intent(InstallPackagesActivity.this,
                                                InstallPackagesService.class)
                                                .putExtra("install", install == 1)
                                                .putExtra("packageUri", packageUri)
                                                .putExtra("apkFilePath", apkFilePath)
                                                .putExtra("packageInfo", processedPackageInfo)
                                                .putExtra("waitForLeaving", preDefinedTryToAvoidUpdateWhenUsing));
                                finish();
                            } else {
                                showInstallPermissionCheckFailedDialog(
                                        install, apkFilePath, packageUri,
                                        processedPackageInfo, preDefinedTryToAvoidUpdateWhenUsing);
                            }
                        }
                    }
                });
        installPackagesAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialog, which) -> {
            if (install != 0) clearTempFile(apkFilePath);
            finish();
        });
        if (!preDefinedTryToAvoidUpdateWhenUsing
                && processedPackageInfo != null
                && AccessibilityUtils.isAccessibilitySettingsOn(this)) {
            installPackagesAlertDialog.setButton(
                    DialogInterface.BUTTON_NEUTRAL,
                    getString(R.string.installWhenNotUsing),
                    (dialog, which) -> {
                        if (notAllowInstallWhenIsObsd.getValue(null)
                                && installPackagesAlertDialog.isObsd()) {
                            AlertDialogUtils.buildAlertDialog(
                                            InstallPackagesActivity.this,
                                            R.drawable.ic_warning,
                                            R.string.alert_isObsd,
                                            R.string.dangerous)
                                    .setPositiveButton(R.string.retry, (dialog14, which13) ->
                                            showInstallDialog(
                                                    progressDialog, install,
                                                    alertDialogMessage, apkFilePath,
                                                    packageUri, fromPkgLabel,
                                                    fromPkgName, processedPackageInfo)
                                    )
                                    .setNegativeButton(R.string.cancel, (dialog15, which14) -> {
                                        if (install != 0) clearTempFile(apkFilePath);
                                        finish();
                                    })
                                    .create().show();
                        } else {
                            if (install == 1) {
                                CheckBox checkBox = ((ObsdAlertDialog) dialog).findViewById(R.id.ipa_dialog_checkBox);
                                if (checkBox != null && checkBox.isChecked()) {
                                    AppPreferences sp = new AppPreferences(InstallPackagesActivity.this);
                                    String originData = sp.getString("installPkgs_autoAllowPkgs_allows", "");
                                    List<String> originData_list = MoreUtils.convertToList(originData, ",");
                                    if (!ILLEGALPKGNAME.equals(fromPkgLabel)
                                            &&
                                            (originData == null ||
                                                    !MoreUtils.convertToList(originData, ",").contains(
                                                            Base64.encodeToString(
                                                                    fromPkgName.getBytes(), Base64.DEFAULT)))) {
                                        originData_list.add(
                                                Base64.encodeToString(fromPkgName.getBytes(), Base64.DEFAULT));
                                        sp.put(
                                                "installPkgs_autoAllowPkgs_allows",
                                                MoreUtils.listToString(originData_list, ",")
                                        );
                                    }
                                }
                            }
                            if (install == 2) {
                                clearTempFile(apkFilePath);
                            } else {
                                ServiceUtils.startService(
                                        InstallPackagesActivity.this,
                                        new Intent(InstallPackagesActivity.this,
                                                InstallPackagesService.class)
                                                .putExtra("install", install == 1)
                                                .putExtra("packageUri", packageUri)
                                                .putExtra("apkFilePath", apkFilePath)
                                                .putExtra("packageInfo", processedPackageInfo)
                                                .putExtra("waitForLeaving", true));
                            }
                            finish();
                        }
                    });
        }
        installPackagesAlertDialog.setOnCancelListener(dialog -> {
            if (install != 0) clearTempFile(apkFilePath);
            finish();
        });

        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }

        if (isFinishing()) return;
        installPackagesAlertDialog.show();
        Window w = installPackagesAlertDialog.getWindow();
        if (w != null) {
            View v = (View) w.findViewById(android.R.id.custom).getParent();
            if (v != null) {
                v.setMinimumHeight(0);
            }
        }
    }

    private void showInstallPermissionCheckFailedDialog(final int install, final String apkFilePath,
                                                        final Uri packageUri,
                                                        final PackageInfo processedPackageInfo,
                                                        final boolean preDefinedTryToAvoidUpdateWhenUsing) {
        AlertDialog.Builder adbd = FreezeYouAlertDialogBuilder(InstallPackagesActivity.this);
        adbd.setMessage(R.string.installPerimisionCheckFailed_ifContinue);
        adbd.setTitle(R.string.notice);
        adbd.setPositiveButton(R.string.yes, (dialog, which) -> {
            ServiceUtils.startService(
                    InstallPackagesActivity.this,
                    new Intent(InstallPackagesActivity.this,
                            InstallPackagesService.class)
                            .putExtra("install", install == 1)
                            .putExtra("packageUri", packageUri)
                            .putExtra("apkFilePath", apkFilePath)
                            .putExtra("packageInfo", processedPackageInfo)
                            .putExtra("waitForLeaving", preDefinedTryToAvoidUpdateWhenUsing));
            finish();
        });
        adbd.setNegativeButton(R.string.no, (dialog, which) -> finish());
        adbd.setNeutralButton(R.string.jumpToSysInstaller, (dialog, which) -> {
            if (install == 0) {
                InstallPackagesActivity.this.startActivity(
                        new Intent(
                                Intent.ACTION_DELETE,
                                Uri.parse("package:" + packageUri.getEncodedSchemeSpecificPart())
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
                finish();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (getPackageManager().canRequestPackageInstalls()) {
                        requestSysInstallPkg(apkFilePath);
                        finish();
                    } else {
                        showInstallPermissionCheckFailedDialog(
                                install, apkFilePath, packageUri,
                                processedPackageInfo, preDefinedTryToAvoidUpdateWhenUsing);
                        Uri packageUri1 = Uri.parse("package:cf.playhi.freezeyou");
                        Intent intent =
                                new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri1);
                        startActivity(intent);
                    }
                } else {
                    requestSysInstallPkg(apkFilePath);
                    finish();
                }
            }
        });
        adbd.setOnCancelListener(dialog -> finish());
        adbd.show();
    }

    private void requestSysInstallPkg(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File file = new File(filePath);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, "cf.playhi.freezeyou.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        Intent chooser = Intent.createChooser(intent, getString(R.string.plsSelect));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 301) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                finish();
            }
        }
    }

}
