package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;
import java.util.Arrays;

import static android.content.Context.POWER_SERVICE;
import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;
import static cf.playhi.freezeyou.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.NotificationUtils.createNotification;
import static cf.playhi.freezeyou.NotificationUtils.deleteNotification;
import static cf.playhi.freezeyou.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.ProcessUtils.fAURoot;
import static cf.playhi.freezeyou.ServiceUtils.startService;
import static cf.playhi.freezeyou.TasksUtils.onUFApplications;
import static cf.playhi.freezeyou.ToastUtils.showToast;

class Support {

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
                    processUnfreezeAction(context, pkgName, true, false, activity, finish);
                }
            });
        }
        builder.create().show();
    }

    static boolean isDeviceOwner(Context context) {
        return Build.VERSION.SDK_INT >= 18 && getDevicePolicyManager(context).isDeviceOwnerApp(context.getPackageName());
    }

    static DevicePolicyManager getDevicePolicyManager(Context context) {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    static boolean checkMRootFrozen(Context context, String pkgName) {
        try {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isDeviceOwner(context) && getDevicePolicyManager(context).isApplicationHidden(DeviceAdminReceiver.getComponentName(context), pkgName);
        } catch (Exception e) {
            return false;
        }
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

    static void askRun(final Context context, final String pkgName, final boolean runImmediately, @Nullable Activity activity, boolean finish) {
        if (runImmediately || (new AppPreferences(context).getBoolean("openImmediately", false))) {
            checkAndStartApp(context, pkgName, activity, finish);
        } else {
            context.startActivity(new Intent(context, AskRunActivity.class)
                    .putExtra("pkgName", pkgName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    static void shortcutMakeDialog(Context context, String title, String message, final Activity activity, @Nullable final ApplicationInfo applicationInfo, final String pkgName, int ot, boolean auto, boolean finish) {
        if (new AppPreferences(context).getBoolean("openAndUFImmediately", false) && auto) {
            if (ot == 2) {
                checkAndStartApp(context, pkgName, activity, finish);
            } else {
                processUnfreezeAction(context, pkgName, true, true, activity, finish);//ot==1
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

    static void processRootAction(final String pkgName, final Context context, final boolean enable, final boolean askRun, boolean runImmediately, @Nullable Activity activity, boolean finish) {
        String currentPackage = " ";
        if (new AppPreferences(context).getBoolean("avoidFreezeForegroundApplications", false)) {
            currentPackage = MainApplication.getCurrentPackage();
        }
        if ((!"cf.playhi.freezeyou".equals(pkgName)) && (!pkgName.equals(currentPackage))) {
            try {
                final int exitValue = fAURoot(pkgName, enable);
                if (exitValue == 0) {
                    if (enable) {
                        onUFApplications(context, pkgName);
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.executed);
                        }
                        createNotification(context, pkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
                        if (askRun) {
                            askRun(context, pkgName, runImmediately, activity, finish);
                        }
                    } else {
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.executed);
                        }
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
    }

    @TargetApi(21)
    static void processMRootAction(Context context, String pkgName, boolean hidden, boolean askRun, boolean runImmediately, @Nullable Activity activity, boolean finish) {
        String currentPackage = " ";
        if (new AppPreferences(context).getBoolean("avoidFreezeForegroundApplications", false)) {
            currentPackage = MainApplication.getCurrentPackage();
        }
        if ((!"cf.playhi.freezeyou".equals(pkgName)) && (!pkgName.equals(currentPackage))) {
            if (getDevicePolicyManager(context).setApplicationHidden(
                    DeviceAdminReceiver.getComponentName(context), pkgName, hidden)) {
                if (hidden) {
                    sendStatusChangedBroadcast(context);
                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                        showToast(context, R.string.freezeCompleted);
                    }
                    deleteNotification(context, pkgName);
                } else {
                    onUFApplications(context, pkgName);
                    sendStatusChangedBroadcast(context);
                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                        showToast(context, R.string.UFCompleted);
                    }
                    createNotification(context, pkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
                    if (askRun) {
                        askRun(context, pkgName, runImmediately, activity, finish);
                    }
                }
            } else {
                sendStatusChangedBroadcast(context);
                showToast(context, R.string.failed);
            }
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

    static void processUnfreezeAction(Context context, String pkgName, boolean askRun, boolean runImmediately, @Nullable Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", false)
                .putExtra("single", true)
                .putExtra("runImmediately", runImmediately));
        checkAndDoActivityFinish(activity, finish);
    }

    static void processFreezeAction(Context context, String pkgName, boolean askRun, @Nullable Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", true)
                .putExtra("single", true));
        checkAndDoActivityFinish(activity, finish);
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
            String currentPackage = " ";
            if (new AppPreferences(context).getBoolean("avoidFreezeForegroundApplications", false)) {
                currentPackage = MainApplication.getCurrentPackage();
            }
            Process process = null;
            DataOutputStream outputStream = null;
            try {
                process = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(process.getOutputStream());
                if (freeze) {
                    for (String aPkgNameList : pkgNameList) {
                        if ((!"cf.playhi.freezeyou".equals(aPkgNameList)) && (!currentPackage.equals(aPkgNameList))) {
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
                            onUFApplications(context, aPkgNameList);
                            createNotification(context, aPkgNameList, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgNameList, null, false)));
                        }
                    }
                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                        showToast(context, R.string.executed);
                    }
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
            String currentPackage = " ";
            if (new AppPreferences(context).getBoolean("avoidFreezeForegroundApplications", false)) {
                currentPackage = MainApplication.getCurrentPackage();
            }
            for (String aPkgNameList : pkgNameList) {
                try {
                    if (freeze) {
                        if ((!"cf.playhi.freezeyou".equals(aPkgNameList)) && (!currentPackage.equals(aPkgNameList)) && (!checkMRootFrozen(context, aPkgNameList))) {
                            if (getDevicePolicyManager(context).setApplicationHidden(
                                    DeviceAdminReceiver.getComponentName(context), aPkgNameList, true)) {
                                deleteNotification(context, aPkgNameList);
                            } else {
                                showToast(context, aPkgNameList + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                            }
                        }
                    } else {
                        if (checkMRootFrozen(context, aPkgNameList)) {
                            if (getDevicePolicyManager(context).setApplicationHidden(
                                    DeviceAdminReceiver.getComponentName(context), aPkgNameList, false)) {
                                onUFApplications(context, aPkgNameList);
                                createNotification(context, aPkgNameList, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgNameList, null, false)));
                            } else {
                                showToast(context, aPkgNameList + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(context, context.getString(R.string.exceptionHC) + e.getLocalizedMessage());
                }
            }
            sendStatusChangedBroadcast(context);
            if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                showToast(context, R.string.executed);
            }
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

//    static void checkLanguage(Context context) {
//        Resources resources = context.getResources();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        Configuration config = resources.getConfiguration();
//        String s = PreferenceManager.getDefaultSharedPreferences(context).getString("languagePref", "Default");
//        if (s == null) {
//            s = "Default";
//        }
//        switch (s) {
//            case "Default":
//                config.locale = Locale.getDefault();
//                break;
//            case "tc":
//                config.locale = Locale.TRADITIONAL_CHINESE;
//                break;
//            case "sc":
//                config.locale = Locale.SIMPLIFIED_CHINESE;
//                break;
//            case "en":
//                config.locale = Locale.ENGLISH;
//                break;
//            default:
//                config.locale = Locale.getDefault();
//                break;
//        }
//        resources.updateConfiguration(config, dm);
//    }
}
