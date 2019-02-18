package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.ToastUtils.showToast;

//Install and uninstall
public class InstallPackagesActivity extends Activity {
    private boolean isObsd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        processSetTheme(this);
//        processAddTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        Intent intent = getIntent();
        final Uri packageUri = intent.getData();

        if (packageUri == null) {
            showToast(this, "INVALID_URI");
            finish();
            return;
        }

        String scheme = packageUri.getScheme();

        if ((!ContentResolver.SCHEME_FILE.equals(scheme)
                && !ContentResolver.SCHEME_CONTENT.equals(scheme)) && !"package".equals(scheme)) {
            showToast(this, "INVALID_URI");
            finish();
            return;
        }

        final boolean install = !(Intent.ACTION_DELETE.equals(intent.getAction()) || Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction()));

        String apkFileName = "package" + new Date().getTime() + "F.apk";
        final String apkFilePath = getExternalCacheDir() + File.separator + apkFileName;

        StringBuilder alertDialogMessage = new StringBuilder();
        if (install) {
            try {
                InputStream in = getContentResolver().openInputStream(packageUri);
                if (in == null) {
                    return;
                }
                OutputStream out = new FileOutputStream(apkFilePath);
                byte[] buffer = new byte[1024 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                in.close();
                PackageManager pm = getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0);
                packageInfo.applicationInfo.sourceDir = apkFilePath;
                packageInfo.applicationInfo.publicSourceDir = apkFilePath;
                alertDialogMessage.append("来源应用：");
                alertDialogMessage.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 22) {
                    Uri referrerUri = getReferrer();
                    if (referrerUri == null || !"android-app".equals(referrerUri.getScheme())) {
                        alertDialogMessage.append("Unknown");
                    } else {
                        String refererPackageLabel =
                                getApplicationLabel(
                                        this, null, null,
                                        referrerUri.getEncodedSchemeSpecificPart().substring(2));
                        if (refererPackageLabel.equals(getString(R.string.uninstalled))) {
                            alertDialogMessage.append("Unknown");
                        } else {
                            alertDialogMessage.append(refererPackageLabel);
                        }
                    }
                } else {
                    alertDialogMessage.append("Unknown");
                }
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append("安装应用：");
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append("应用：");
                alertDialogMessage.append(pm.getApplicationLabel(packageInfo.applicationInfo));
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append("包名：");
                alertDialogMessage.append(packageInfo.packageName);
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append("版本：");
                alertDialogMessage.append(packageInfo.versionName);
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append(System.getProperty("line.separator"));
                alertDialogMessage.append("是否允许？");
            } catch (Exception e) {
                alertDialogMessage.append("无法安装：").append(e.getLocalizedMessage());
            }
        } else {
            String packageName = packageUri.getEncodedSchemeSpecificPart();
            if (packageName == null) {
                showToast(this, "Invalid package name in URI: " + packageUri);
                finish();
                return;
            }
            alertDialogMessage.append("来源应用：");
            alertDialogMessage.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 22) {
                Uri referrerUri = getReferrer();
                if (referrerUri != null && "android-app".equals(referrerUri.getScheme())) {
                    String refererPackageLabel =
                            getApplicationLabel(
                                    this, null, null,
                                    referrerUri.getEncodedSchemeSpecificPart().substring(2));
                    if (refererPackageLabel.equals(getString(R.string.uninstalled))) {
                        alertDialogMessage.append("Unknown");
                    } else {
                        alertDialogMessage.append(refererPackageLabel);
                    }
                } else {
                    alertDialogMessage.append("Unknown");
                }
            } else {
                alertDialogMessage.append("Unknown");
            }
            alertDialogMessage.append(System.getProperty("line.separator"));
            alertDialogMessage.append(System.getProperty("line.separator"));
            alertDialogMessage.append("卸载应用：");
            alertDialogMessage.append(System.getProperty("line.separator"));
            alertDialogMessage.append("应用：");
            alertDialogMessage.append(getApplicationLabel(this, null, null, packageName));
            alertDialogMessage.append(System.getProperty("line.separator"));
            alertDialogMessage.append("包名：");
            alertDialogMessage.append(packageName);
            alertDialogMessage.append(System.getProperty("line.separator"));
            alertDialogMessage.append("是否允许？");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(install ? "安装" : "卸载");
        builder.setMessage(alertDialogMessage);
        builder.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (true) {
                    showToast(InstallPackagesActivity.this, "Dangerous");
                } else {
                    ServiceUtils.startService(
                            InstallPackagesActivity.this,
                            new Intent(InstallPackagesActivity.this, InstallPackagesService.class)
                                    .putExtra("install", install)
                                    .putExtra("packageUri", packageUri)
                                    .putExtra("apkFilePath", apkFilePath));
                    finish();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
//        try {
//            getDevicePolicyManager(this).clearPackagePersistentPreferredActivities(
//                    DeviceAdminReceiver.getComponentName(this), getPackageName()
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        isObsd = (ev.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0;
        showToast(this, isObsd ? "1" : "not");
        return super.dispatchTouchEvent(ev);
    }
}

