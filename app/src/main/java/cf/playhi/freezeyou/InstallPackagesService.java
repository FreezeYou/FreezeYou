package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.ToastUtils.showToast;

//Install and uninstall
public class InstallPackagesService extends Service {

    private final ArrayList<Intent> intentArrayList = new ArrayList<>();
    private boolean processing = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return super.onStartCommand(null, flags, startId);

        final Intent i = new Intent(intent);
        i.putExtra("requestTime", new Date().getTime());
        if (processing) {
            intentArrayList.add(i);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    installAndUninstall(i);
                }
            }).start();
        }
        return super.onStartCommand(i, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.installAndUninstall));
            NotificationChannel channel =
                    new NotificationChannel("InstallPackages",
                            getString(R.string.installAndUninstall), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId("InstallPackages");
            startForeground(5, mBuilder.build());
        } else {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.installAndUninstall));
            startForeground(5, mBuilder.getNotification());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void installAndUninstall(Intent intent) {
        processing = true;

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ?
                new Notification.Builder(this, "InstallPackages") :
                new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (intent.getBooleanExtra("install", true)) {//Install
            install(intent, builder, notificationManager);
        } else {//Uninstall
            uninstall(intent, builder, notificationManager);
        }

        //移除已完成的
        intentArrayList.remove(intent);
        if (intentArrayList.isEmpty()) {
            processing = false;
            stopSelf();
        } else {
            installAndUninstall(intentArrayList.get(0));
        }
    }

    private void uninstall(Intent intent, Notification.Builder builder, NotificationManager notificationManager) {

        final Uri packageUri = intent.getParcelableExtra("packageUri");
        String packageName = packageUri.getEncodedSchemeSpecificPart();
        String willBeUninstalledName = getApplicationLabel(this, null, null, packageName);
        try {
            if (packageName == null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getApplicationContext(), getString(R.string.invalidArguments) + " " + packageUri);
                    }
                });
                return;
            }

            Drawable willBeUninstalledIcon =
                    getApplicationIcon(
                            this,
                            packageName,
                            ApplicationInfoUtils.getApplicationInfoFromPkgName(packageName, this),
                            false);

            builder.setContentTitle(getString(R.string.uninstalling) + " " + willBeUninstalledName);
            builder.setLargeIcon(getBitmapFromDrawable(willBeUninstalledIcon));
            notificationManager.notify(packageName.hashCode(), builder.getNotification());

            if (Build.VERSION.SDK_INT >= 21 && Support.isDeviceOwner(this)) {
                getPackageManager().getPackageInstaller().uninstall(packageName,
                        PendingIntent.getBroadcast(this, packageName.hashCode(),
                                new Intent(
                                        this,
                                        InstallPackagesFinishedReceiver.class)
                                        .putExtra("name", willBeUninstalledName)
                                        .putExtra("pkgName", packageName)
                                        .putExtra("install", false), PendingIntent.FLAG_UPDATE_CURRENT)
                                .getIntentSender());
            } else {
                // Root Mode
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                outputStream.writeBytes("pm uninstall -k \"" + packageName + "\"\n");
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                process.waitFor();
                destroyProcess(outputStream, process);
                InstallPackagesUtils
                        .notifyFinishNotification(
                                this, notificationManager, builder,
                                false,
                                packageName,
                                willBeUninstalledName + " " + getString(R.string.uninstallFinished),
                                null,
                                true);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            InstallPackagesUtils
                    .notifyFinishNotification(
                            this, notificationManager, builder,
                            false,
                            packageName,
                            getString(R.string.uninstallFailed),
                            e.getLocalizedMessage(),
                            false);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(getApplicationContext(), String.format(getString(R.string.errorUninstallToast), e.getLocalizedMessage()));
                }
            });
        }
    }

    private void install(Intent intent, Notification.Builder builder, NotificationManager notificationManager) {
        try {
            String apkFilePath = intent.getStringExtra("apkFilePath");
            if (apkFilePath == null || "".equals(apkFilePath) || !new File(apkFilePath).exists()) {
                Uri packageUri = intent.getParcelableExtra("packageUri");
                InputStream in = getContentResolver().openInputStream(packageUri);
                if (in == null) {
                    return;
                }

                String apkFileName = "package" + new Date().getTime() + "F.apk";
                apkFilePath = getExternalCacheDir() + File.separator + "ZDF-" + apkFileName;

                OutputStream out = new FileOutputStream(apkFilePath);
                byte[] buffer = new byte[1024 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                in.close();
            }

            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0);
            String willBeInstalledPackageName = packageInfo.packageName;
            packageInfo.applicationInfo.sourceDir = apkFilePath;
            packageInfo.applicationInfo.publicSourceDir = apkFilePath;
            String willBeInstalledName = pm.getApplicationLabel(packageInfo.applicationInfo).toString();
            Drawable willBeInstalledIcon = pm.getApplicationIcon(packageInfo.applicationInfo);

            builder.setContentTitle(getString(R.string.installing) + " " + willBeInstalledName);
            builder.setProgress(100, 0, true);
            builder.setLargeIcon(getBitmapFromDrawable(willBeInstalledIcon));
            notificationManager.notify(willBeInstalledPackageName.hashCode(), builder.getNotification());

            if (Build.VERSION.SDK_INT >= 21 && Support.isDeviceOwner(this)) {
                PackageInstaller packageInstaller = pm.getPackageInstaller();
                PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                params.setAppPackageName(willBeInstalledPackageName);
                int sessionId = packageInstaller.createSession(params);
                PackageInstaller.Session session = packageInstaller.openSession(sessionId);
                OutputStream outputStream = session.openWrite(
                        Integer.toString(apkFilePath.hashCode()), 0, -1);
                InputStream in1 = new FileInputStream(apkFilePath);
                byte[] buffer = new byte[1024 * 1024];
                int bytesRead;
                while ((bytesRead = in1.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                session.fsync(outputStream);
                outputStream.close();
                in1.close();
                session.commit(
                        PendingIntent.getBroadcast(this, sessionId,
                                new Intent(
                                        this,
                                        InstallPackagesFinishedReceiver.class)
                                        .putExtra("name", willBeInstalledName)
                                        .putExtra("pkgName", willBeInstalledPackageName)
                                        .putExtra("apkFilePath", apkFilePath),
                                PendingIntent.FLAG_UPDATE_CURRENT)
                                .getIntentSender());
            } else {
                // Root Mode
                String result = null;
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                    outputStream.writeBytes("pm install -r \"" + apkFilePath + "\"\n");
                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    process.waitFor();
                    destroyProcess(outputStream, process);

                    // Delete Temp File
                    InstallPackagesUtils.deleteTempFile(this, apkFilePath, false);

                    InputStream pi = process.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pi));
                    result = bufferedReader.readLine();
                } finally {
                    if (result != null && result.toLowerCase().contains("success")) {
                        InstallPackagesUtils
                                .notifyFinishNotification(
                                        this, notificationManager, builder,
                                        true,
                                        willBeInstalledPackageName,
                                        willBeInstalledName + " " + getString(R.string.installFinished),
                                        null,
                                        true);
                        if (PreferenceManager.getDefaultSharedPreferences(this)
                                .getBoolean("tryDelApkAfterInstalled", false))
                            InstallPackagesUtils.deleteTempFile(this, apkFilePath, true);
                    } else {
                        InstallPackagesUtils
                                .notifyFinishNotification(
                                        this, notificationManager, builder,
                                        true,
                                        willBeInstalledPackageName,
                                        willBeInstalledName + " " + getString(R.string.installFailed),
                                        String.format(getString(R.string.reason_colon), result),
                                        false);
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            InstallPackagesUtils
                    .notifyFinishNotification(
                            this, notificationManager, builder,
                            true,
                            null,
                            getString(R.string.installFailed),
                            e.getLocalizedMessage(),
                            false);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(getApplicationContext(), String.format(getString(R.string.errorInstallToast), e.getLocalizedMessage()));
                }
            });

        }

    }
}
