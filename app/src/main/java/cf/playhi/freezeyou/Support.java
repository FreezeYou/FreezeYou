package cf.playhi.freezeyou;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Calendar;

import cf.playhi.freezeyou.receiver.NotificationDeletedReceiver;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

class Support {
    private static Drawable drawable;
    private static Bitmap bitmap;

    private static void makeDialog(final String title, final String message, final Context context, @Nullable final ApplicationInfo applicationInfo, final String pkgName, final boolean enabled, @Nullable final Activity activity, final boolean finish) {
        AlertDialog.Builder builder =
                buildAlertDialog(context, getApplicationIcon(context, pkgName, applicationInfo, true), message, title)
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkAndDoActivityFinish(activity, finish);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                checkAndDoActivityFinish(activity, finish);
                            }
                        });
        if (enabled) {
            builder.setPositiveButton(R.string.launch, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    checkAndStartApp(context, pkgName, activity, finish);
                }
            });
            builder.setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    processFreezeAction(context, pkgName, true, activity, finish);
                }
            });
        } else {
            builder.setPositiveButton(R.string.unfreeze, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    processUnfreezeAction(context, pkgName, true, activity, finish);
                }
            });
        }
        builder.create().show();
    }

    private static void destroyProcess(@Nullable DataOutputStream dataOutputStream, @Nullable Process process1) {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (process1 != null) {
                process1.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void showToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    static void showToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    static AlertDialog.Builder buildAlertDialog(Context context, int icon, int message, int title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    static AlertDialog.Builder buildAlertDialog(Context context, Drawable icon, CharSequence message, CharSequence title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    static void joinQQGroup(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D92NGzlhmCK_UFrL_oEAV7Fe6QrvFR5y_"));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            requestOpenWebSite(context, "https://shang.qq.com/wpa/qunwpa?idkey=cbc8ae71402e8a1bc9bb4c39384bcfe5b9f7d18ff1548ea9bdd842f036832f3d");
        }
    }

    static boolean isDeviceOwner(Context context) {
        return Build.VERSION.SDK_INT >= 18 && getDevicePolicyManager(context).isDeviceOwnerApp(context.getPackageName());
    }

    static DevicePolicyManager getDevicePolicyManager(Context context) {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    static boolean checkMRootFrozen(Context context, String pkgName) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isDeviceOwner(context) && getDevicePolicyManager(context).isApplicationHidden(DeviceAdminReceiver.getComponentName(context), pkgName);
    }

    static boolean checkRootFrozen(Context context, String pkgName, PackageManager packageManager) {
        int tmp;
        try {
            tmp = packageManager == null ? context.getPackageManager().getApplicationEnabledSetting(pkgName) : packageManager.getApplicationEnabledSetting(pkgName);
        } catch (Exception e) {
            tmp = -1;
        }
        return ((tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) || (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED));
    }

    private static void askRun(final Context context, final String pkgName, @Nullable Activity activity, boolean finish) {
        AppPreferences sharedPref = new AppPreferences(context);
        if ((sharedPref.getBoolean("openImmediately", false)) || (sharedPref.getBoolean("openAndUFImmediately", false))) {
            checkAndStartApp(context, pkgName, activity, finish);
        } else {
            context.startActivity(new Intent(context, AskRunActivity.class)
                    .putExtra("pkgName", pkgName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @TargetApi(21)
    static void shortcutMakeDialog(Context context, String title, String message, final Activity activity, @Nullable final ApplicationInfo applicationInfo, final String pkgName, int ot, boolean auto, boolean finish) {
        if (new AppPreferences(context).getBoolean("openAndUFImmediately", false) && auto) {
            if (ot == 2) {
                checkAndStartApp(context, pkgName, activity, finish);
            } else {
                processUnfreezeAction(context, pkgName, true, activity, finish);//ot==1
            }
        } else {
            makeDialog(title, message, context, applicationInfo, pkgName, ot == 2, activity, finish);
        }
    }

    private static void checkAndDoActivityFinish(Activity activity, boolean finish) {
        if (activity != null && finish) {
            activity.finish();
        }
    }

    //最初参考 http://www.cnblogs.com/zhou2016/p/6281678.html

    /**
     * Drawable转Bitmap
     *
     * @param drawable drawable
     * @return Bitmap
     */
    static Bitmap getBitmapFromDrawable(Drawable drawable) {
        try {
            return ((BitmapDrawable) drawable).getBitmap();
        } catch (Exception e) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    static Drawable getApplicationIcon(Context context, String pkgName, @Nullable ApplicationInfo applicationInfo, boolean resize) {
        String path = context.getFilesDir() + "/icon/" + pkgName + ".png";
        if (new File(path).exists()) {
            drawable = BitmapDrawable.createFromPath(path);
        } else if (applicationInfo != null) {
            drawable = applicationInfo.loadIcon(context.getPackageManager());
            folderCheck(context.getFilesDir() + "/icon");
            writeBitmapToFile(path, getBitmapFromDrawable(drawable));
        } else if (!"".equals(pkgName)) {
            try {
                drawable = context.getPackageManager().getApplicationIcon(pkgName);
                folderCheck(context.getFilesDir() + "/icon");
                writeBitmapToFile(path, getBitmapFromDrawable(drawable));
            } catch (PackageManager.NameNotFoundException e) {
                drawable = context.getResources().getDrawable(android.R.drawable.ic_menu_delete);
            } catch (Exception e) {
                drawable = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
            }
        }
        if ((drawable == null) || (drawable.getIntrinsicWidth() <= 0) || (drawable.getIntrinsicHeight() <= 0)) {
            drawable = context.getResources().getDrawable(R.mipmap.ic_launcher_round);
        }
        if (resize) {
            bitmap = getBitmapFromDrawable(drawable);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) 72) / width;
            float scaleHeight = ((float) 72) / height;
            matrix.postScale(scaleWidth, scaleHeight);
            return new BitmapDrawable(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true));
        } else {
            return drawable;
        }
    }

    private static void writeBitmapToFile(String filePath, Bitmap b) {
        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void folderCheck(String path) {
        try {
            File file = new File(path);
            if (!file.isDirectory()) {
                file.delete();
            }
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getApplicationLabel(Context context, @Nullable PackageManager packageManager, @Nullable ApplicationInfo applicationInfo, String pkgName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(pkgName, "");
        if (!"".equals(name)) {
            return name;
        }
        PackageManager pm = packageManager == null ? context.getPackageManager() : packageManager;
        if (applicationInfo != null) {
            name = applicationInfo.loadLabel(pm).toString();
            sharedPreferences.edit().putString(pkgName, name).apply();
            return name;
        } else {
            try {
                name = pm.getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES).loadLabel(pm).toString();
                sharedPreferences.edit().putString(pkgName, name).apply();
                return name;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return context.getString(R.string.uninstalled);
            } catch (Exception e) {
                e.printStackTrace();
                return pkgName;
            }
        }
    }

    private static int fAURoot(String pkgName, Boolean enable) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        if (enable) {
            outputStream.writeBytes("pm enable " + pkgName + "\n");
        } else {
            outputStream.writeBytes("pm disable " + pkgName + "\n");
        }
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        int i = process.waitFor();
        destroyProcess(outputStream, process);
        return i;
    }

    static void createShortCut(String title, String pkgName, Drawable icon, Class<?> cls, String id, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            createShortCutOldApi(title, pkgName, icon, cls, context);
        } else {
            ShortcutManager mShortcutManager =
                    context.getSystemService(ShortcutManager.class);
            if (mShortcutManager != null) {
                if (mShortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo.Builder shortcutInfoBuilder =
                            new ShortcutInfo.Builder(context, id);
                    shortcutInfoBuilder.setIcon(Icon.createWithBitmap(getBitmapFromDrawable(icon)));
                    shortcutInfoBuilder.setIntent(
                            new Intent(context, cls)
                                    .setAction(Intent.ACTION_MAIN)
                                    .putExtra("pkgName", pkgName)
                    );
                    shortcutInfoBuilder.setShortLabel(title);
                    shortcutInfoBuilder.setLongLabel(title);
                    // Assumes there's already a shortcut with the ID "my-shortcut".
                    // The shortcut must be enabled.
                    ShortcutInfo pinShortcutInfo = shortcutInfoBuilder.build();
                    // Create the PendingIntent object only if your app needs to be notified
                    // that the user allowed the shortcut to be pinned. Note that, if the
                    // pinning operation fails, your app isn't notified. We assume here that the
                    // app has implemented a method called createShortcutResultIntent() that
                    // returns a broadcast intent.
                    Intent pinnedShortcutCallbackIntent =
                            mShortcutManager.createShortcutResultIntent(pinShortcutInfo);

                    // Configure the intent so that your app's broadcast receiver gets
                    // the callback successfully.
                    PendingIntent successCallback = PendingIntent.getBroadcast(context, 0,
                            pinnedShortcutCallbackIntent, 0);

                    mShortcutManager.requestPinShortcut(pinShortcutInfo,
                            successCallback.getIntentSender());
                    showToast(context, R.string.requested);
                } else {
                    createShortCutOldApi(title, pkgName, icon, cls, context);
                }
            } else {
                createShortCutOldApi(title, pkgName, icon, cls, context);
            }
        }
    }

    private static void createShortCutOldApi(String title, String pkgName, Drawable icon, Class<?> cls, Context context) {
        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Intent intent = new Intent(context, cls);
        intent.putExtra("pkgName", pkgName);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        try {
            BitmapDrawable bd = (BitmapDrawable) icon;
            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
            context.sendBroadcast(addShortCut);
            showToast(context, R.string.requested);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                Bitmap bitmap = getBitmapFromDrawable(icon);
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
                context.sendBroadcast(addShortCut);
                showToast(context, R.string.requested);
            } catch (Exception ee) {
                ee.printStackTrace();
                try {
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.2f, 0.2f);
                    Bitmap bitmap = getBitmapFromDrawable(icon);
                    addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
                    context.sendBroadcast(addShortCut);
                    showToast(context, R.string.requested);
                } catch (Exception eee) {
                    showToast(context, context.getString(R.string.requestFailed) + eee.getMessage());
                }
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    static void createNotification(Context context, String pkgName, int iconResId, @Nullable Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            AppPreferences preferenceManager = new AppPreferences(context);
            boolean notificationBarFreezeImmediately = preferenceManager.getBoolean("notificationBarFreezeImmediately", true);
            String description = notificationBarFreezeImmediately ? context.getString(R.string.freezeImmediately) : context.getString(R.string.disableAEnable);
            Notification.Builder mBuilder = new Notification.Builder(context);
            int mId = pkgName.hashCode();
            String name = getApplicationLabel(context, null, null, pkgName);
            if (!context.getString(R.string.uninstalled).equals(name)) {
                mBuilder.setSmallIcon(iconResId);
                mBuilder.setLargeIcon(bitmap);
                mBuilder.setContentTitle(name);
                mBuilder.setContentText(description);
                mBuilder.setAutoCancel(!preferenceManager.getBoolean("notificationBarDisableClickDisappear", false));
                mBuilder.setOngoing(preferenceManager.getBoolean("notificationBarDisableSlideOut", false));

                Intent intent = new Intent(context, NotificationDeletedReceiver.class).putExtra("pkgName", pkgName);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, mId, intent, 0);
                mBuilder.setDeleteIntent(pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String CHANNEL_ID = "FAUf";
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, description, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    if (notificationManager != null)
                        notificationManager.createNotificationChannel(channel);
                    mBuilder.setChannelId(CHANNEL_ID);
                }
                // Create an Intent for the activity you want to start
                Intent resultIntent;
                PendingIntent resultPendingIntent;
                if (notificationBarFreezeImmediately) {
                    resultIntent = new Intent(context, FUFService.class)
                            .putExtra("pkgName", pkgName)
                            .putExtra("single", true)
                            .putExtra("freeze", true);
                    resultPendingIntent = PendingIntent.getService(context, mId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    resultIntent = new Intent(context, Freeze.class).putExtra("pkgName", pkgName).putExtra("auto", false);
                    resultPendingIntent = PendingIntent.getActivity(context, mId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
//                // Create the TaskStackBuilder and add the intent, which inflates the back stack
//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                stackBuilder.addNextIntent(resultIntent);
//                // Get the PendingIntent containing the entire back stack
//                PendingIntent resultPendingIntent =
//                        stackBuilder.getPendingIntent(mId, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    mNotificationManager.notify(mId, mBuilder.build());
                    AppPreferences appPreferences = new AppPreferences(context);
                    String notifying = appPreferences.getString("notifying", "");
                    if (notifying != null && !notifying.contains(pkgName + ",")) {
                        appPreferences.put("notifying", notifying + pkgName + ",");
                    }
                }
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private static void deleteNotification(Context context, String pkgName) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(pkgName.hashCode());
            deleteNotifying(context, pkgName);
        }
    }

    static boolean deleteNotifying(Context context, String pkgName) {
        AppPreferences defaultSharedPreferences = new AppPreferences(context);
        String notifying = defaultSharedPreferences.getString("notifying", "");
        return notifying == null || !notifying.contains(pkgName + ",") || defaultSharedPreferences.put("notifying", notifying.replace(pkgName + ",", ""));
    }

    static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return "";
    }

    static void processRootAction(final String pkgName, final Context context, final boolean enable, final boolean askRun, @Nullable Activity activity, boolean finish) {
        try {
            final int exitValue = fAURoot(pkgName, enable);
            if (exitValue == 0) {
                if (enable) {
                    showToast(context, R.string.executed);
                    createNotification(context, pkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
                    if (askRun) {
                        askRun(context, pkgName, activity, finish);
                    }
                } else {
                    showToast(context, R.string.executed);
                    deleteNotification(context, pkgName);
                }
            } else {
                showToast(context, R.string.mayUnrootedOrOtherEx);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            showToast(context, context.getString(R.string.exception) + e.getMessage());
            if (e.getMessage().toLowerCase().contains("permission denied") || e.getMessage().toLowerCase().contains("not found")) {
                showToast(context, R.string.mayUnrooted);
            }
        }
        sendStatusChangedBroadcast(context);
    }

    @TargetApi(21)
    static void processMRootAction(Context context, String pkgName, boolean hidden, boolean askRun, @Nullable Activity activity, boolean finish) {
        if (getDevicePolicyManager(context).setApplicationHidden(
                DeviceAdminReceiver.getComponentName(context), pkgName, hidden)) {
            if (hidden) {
                sendStatusChangedBroadcast(context);
                showToast(context, R.string.freezeCompleted);
                deleteNotification(context, pkgName);
            } else {
                sendStatusChangedBroadcast(context);
                showToast(context, R.string.UFCompleted);
                createNotification(context, pkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
                if (askRun) {
                    askRun(context, pkgName, activity, finish);
                }
            }
        } else {
            sendStatusChangedBroadcast(context);
            showToast(context, R.string.failed);
        }
    }

    static void checkAndStartApp(Context context, String pkgName, @Nullable Activity activity, boolean finish) {
        if (context.getPackageManager().getLaunchIntentForPackage(pkgName) != null) {
            Intent intent = new Intent(
                    context.getPackageManager().getLaunchIntentForPackage(pkgName));
            context.startActivity(intent);
        } else {
            showToast(context,
                    R.string.unrootedOrCannotFindTheLaunchIntent);
        }
        checkAndDoActivityFinish(activity, finish);
    }

    static void processUnfreezeAction(Context context, String pkgName, boolean askRun, @Nullable Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", false)
                .putExtra("single", true));
        checkAndDoActivityFinish(activity, finish);
//        if (checkMRootFrozen(context, pkgName)) {
//            processMRootAction(context, pkgName, false, askRun);
//        } else {
//            processRootAction(pkgName, context, true, askRun);
//        }
    }

    static void processFreezeAction(Context context, String pkgName, boolean askRun, @Nullable Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", true)
                .putExtra("single", true));
        checkAndDoActivityFinish(activity, finish);
//        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
//            processMRootAction(context, pkgName, true, askRun);
//        } else {
//            processRootAction(pkgName, context, false, askRun);
//        }
    }

    static ApplicationInfo getApplicationInfoFromPkgName(String pkgName, Context context) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applicationInfo;
    }

    static void oneKeyActionRoot(Context context, boolean freeze, String[] pkgNameList) {
        if (pkgNameList != null) {
            String currentPackage = MainApplication.getCurrentPackage();
            Process process = null;
            DataOutputStream outputStream = null;
            try {
                process = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(process.getOutputStream());
                if (freeze) {
                    for (String aPkgNameList : pkgNameList) {
                        if (!currentPackage.equals(aPkgNameList)) {
                            try {
                                int tmp = context.getPackageManager().getApplicationEnabledSetting(aPkgNameList);
                                if (tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER && tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                                    outputStream.writeBytes(
                                            "pm disable " + aPkgNameList + "\n");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(context, R.string.plsRemoveUninstalledApplications);
                            }
                        }
                    }
                } else {
                    for (String aPkgNameList : pkgNameList) {
                        try {
                            int tmp = context.getPackageManager().getApplicationEnabledSetting(aPkgNameList);
                            if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                                outputStream.writeBytes(
                                        "pm enable " + aPkgNameList + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(context, R.string.plsRemoveUninstalledApplications);
                        }
                    }
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                int exitValue = process.waitFor();
                if (exitValue == 0) {
                    if (freeze) {
                        for (String aPkgNameList : pkgNameList) {
                            deleteNotification(context, aPkgNameList);
                        }
                    } else {
                        for (String aPkgNameList : pkgNameList) {
                            createNotification(context, aPkgNameList, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgNameList, null, false)));
                        }
                    }
                    showToast(context, R.string.executed);
                } else {
                    showToast(context, R.string.mayUnrootedOrOtherEx);
                }
                destroyProcess(outputStream, process);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(context, context.getString(R.string.exception) + e.getMessage());
                if (e.getMessage().toLowerCase().contains("permission denied") || e.getMessage().toLowerCase().contains("not found")) {
                    showToast(context, R.string.mayUnrooted);
                }
                destroyProcess(outputStream, process);
            }
            sendStatusChangedBroadcast(context);
        }
    }

    @TargetApi(21)
    static void oneKeyActionMRoot(Context context, boolean freeze, String[] pkgNameList) {
        if (pkgNameList != null) {
            String currentPackage = MainApplication.getCurrentPackage();
            for (String aPkgNameList : pkgNameList) {
                try {
                    if (freeze) {
                        if (!currentPackage.equals(aPkgNameList) && !checkMRootFrozen(context, aPkgNameList)) {
                            if (!getDevicePolicyManager(context).setApplicationHidden(
                                    DeviceAdminReceiver.getComponentName(context), aPkgNameList, true)) {
                                showToast(context, aPkgNameList + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                            } else {
                                deleteNotification(context, aPkgNameList);
                            }
                        }
                    } else {
                        if (checkMRootFrozen(context, aPkgNameList)) {
                            if (!getDevicePolicyManager(context).setApplicationHidden(
                                    DeviceAdminReceiver.getComponentName(context), aPkgNameList, false)) {
                                showToast(context, aPkgNameList + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                            } else {
                                createNotification(context, aPkgNameList, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgNameList, null, false)));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(context, context.getString(R.string.exceptionHC) + e.getLocalizedMessage());
                }
            }
            sendStatusChangedBroadcast(context);
            showToast(context, R.string.executed);
        }
    }

    private static void sendStatusChangedBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction("cf.playhi.freezeyou.action.packageStatusChanged");
        intent.setPackage("cf.playhi.freezeyou");
        context.sendBroadcast(intent);
    }

    static boolean addToOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames + pkgName + ",");
    }

    static boolean removeFromOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return !existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames.replace(pkgName + ",", ""));
    }

    static boolean existsInOneKeyList(@Nullable String pkgNames, String pkgName) {
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    static boolean existsInOneKeyList(Context context, String onekeyName, String pkgName) {
        final String pkgNames = new AppPreferences(context).getString(onekeyName, "");
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    static void openDevicePolicyManager(Context context) {
        showToast(context, R.string.needActiveAccessibilityService);
        ComponentName componentName = new ComponentName(context, DeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        context.startActivity(intent);
    }

    static void processSetTheme(Context context) {
        try {
            switch (PreferenceManager.getDefaultSharedPreferences(context).getString("uiStyleSelection", "default")) {
                case "blue":
                    context.setTheme(R.style.AppTheme_Default_Blue);
                    break;
                case "orange":
                    context.setTheme(R.style.AppTheme_Default_Orange);
                    break;
                case "green":
                    context.setTheme(R.style.AppTheme_Default_Green);
                    break;
                case "pink":
                    context.setTheme(R.style.AppTheme_Default_Pink);
                    break;
                case "yellow":
                    context.setTheme(R.style.AppTheme_Default_Yellow);
                    break;
                case "black":
                    context.setTheme(R.style.AppTheme_Default);
                    break;
                default:
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.setTheme(R.style.AppTheme_Default_Blue);
                    } else {
                        context.setTheme(R.style.AppTheme_Default);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void processAddTranslucent(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawableResource(R.color.realTranslucent);
            if (Build.VERSION.SDK_INT >= 19) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    static void processActionBar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    static void checkUpdate(Context context) {
        requestOpenWebSite(context, "https://freezeyou.playhi.cf/checkupdate.php?v=" + getVersionCode(context));
    }

    static void requestOpenWebSite(Context context, String url) {
        Uri webPage = Uri.parse(url);
        Intent about = new Intent(Intent.ACTION_VIEW, webPage);
        if (about.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(about);
        } else {
            showToast(context, context.getString(R.string.plsVisit) + " " + url);
        }
    }

    static void doLockScreen(Context context) {
        //先走ROOT，有权限的话就可以不影响SmartLock之类的了
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("input keyevent KEYCODE_POWER" + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            destroyProcess(outputStream, process);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (pm == null || pm.isScreenOn()) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName componentName = new ComponentName(context, DeviceAdminReceiver.class);
            if (devicePolicyManager != null) {
                if (devicePolicyManager.isAdminActive(componentName)) {
                    devicePolicyManager.lockNow();
                } else {
                    openDevicePolicyManager(context);
                }
            } else {
                showToast(context, R.string.devicePolicyManagerNotFound);
            }
        }
    }


    static void openAccessibilitySettings(Context context) {
        try {
            Intent accessibilityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(accessibilityIntent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, R.string.failed);
        }
    }

    //https://stackoverflow.com/questions/18094982/detect-if-my-accessibility-service-is-enabled
    static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + AccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    static int getThemeDot(Context context) {
        int resId;
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("uiStyleSelection", "default")) {
            case "blue":
                resId = R.drawable.shapedotblue;
                break;
            case "orange":
                resId = R.drawable.shapedotorange;
                break;
            case "green":
                resId = R.drawable.shapedotgreen;
                break;
            case "pink":
                resId = R.drawable.shapedotpink;
                break;
            case "yellow":
                resId = R.drawable.shapedotyellow;
                break;
            default:
                resId = R.drawable.shapedotblue;
                break;
        }
        return resId;
    }


    static void publishTask(Context context, int id, int hour, int minute, String repeat, String task) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, TasksNeedExecuteReceiver.class)
                .putExtra("id", id)
                .putExtra("task", task)
                .putExtra("repeat", repeat)
                .putExtra("hour", hour)
                .putExtra("minute", minute);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        long systemTime = System.currentTimeMillis();
        calendar.setTimeInMillis(systemTime);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (alarmMgr != null) {
            if ("0".equals(repeat)) {
                if (systemTime >= calendar.getTimeInMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                setTask(alarmMgr, calendar.getTimeInMillis(), alarmIntent);
            } else {
                long timeInterval = Long.MAX_VALUE;
                long timeTmp;
                for (int i = 0; i < repeat.length(); i++) {
                    switch (repeat.substring(i, i + 1)) {
                        case "1":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            break;
                        case "2":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            break;
                        case "3":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                            break;
                        case "4":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                            break;
                        case "5":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                            break;
                        case "6":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                            break;
                        case "7":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                            break;
                        default:
                            break;
                    }
                    timeTmp = calculateTimeInterval(systemTime, calendar.getTimeInMillis());
                    if (timeTmp <= 0) {
                        timeTmp = timeTmp + 604800000;
                    }
                    if (timeTmp > 0 && timeTmp < timeInterval) {
                        timeInterval = timeTmp;
                    }
                }
                setTask(alarmMgr, systemTime + timeInterval, alarmIntent);
            }
        } else {
            showToast(context, R.string.requestFailedPlsRetry);
        }
    }

    private static void setTask(@NonNull AlarmManager alarmManager, long triggerAtMillis, PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    private static long calculateTimeInterval(long first, long last) {
        return last - first;
    }

    private static void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
