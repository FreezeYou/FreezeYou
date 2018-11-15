package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;

import static cf.playhi.freezeyou.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.DevicePolicyManagerUtils.getDevicePolicyManager;
import static cf.playhi.freezeyou.NotificationUtils.createNotification;
import static cf.playhi.freezeyou.NotificationUtils.deleteNotification;
import static cf.playhi.freezeyou.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.ProcessUtils.fAURoot;
import static cf.playhi.freezeyou.ServiceUtils.startService;
import static cf.playhi.freezeyou.TasksUtils.onUFApplications;
import static cf.playhi.freezeyou.ToastUtils.showToast;

class Support {

    private static void makeDialog(final String title, final String message, final Context context, final ApplicationInfo applicationInfo, final String pkgName, final boolean enabled, final Activity activity, final boolean finish) {
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

    static void askRun(final Context context, final String pkgName, final boolean runImmediately, Activity activity, boolean finish) {
        if (runImmediately || (new AppPreferences(context).getBoolean("openImmediately", false))) {
            checkAndStartApp(context, pkgName, activity, finish);
        } else {
            context.startActivity(new Intent(context, AskRunActivity.class)
                    .putExtra("pkgName", pkgName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    static void shortcutMakeDialog(Context context, String title, String message, final Activity activity, final ApplicationInfo applicationInfo, final String pkgName, int ot, boolean auto, boolean finish) {
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

    static void processRootAction(final String pkgName, final Context context, final boolean enable, final boolean askRun, boolean runImmediately, Activity activity, boolean finish) {
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
    static void processMRootAction(Context context, String pkgName, boolean hidden, boolean askRun, boolean runImmediately, Activity activity, boolean finish) {
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

    static void checkAndStartApp(Context context, String pkgName, Activity activity, boolean finish) {
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

    static void checkFrozenStatusAndStartApp(Context context, String pkgName) {
        if (realGetFrozenStatus(context, pkgName, null)) {
            processUnfreezeAction(context, pkgName, true, true, null, false);
        } else {
            checkAndStartApp(context, pkgName, null, false);
        }
    }

    static void processUnfreezeAction(Context context, String pkgName, boolean askRun, boolean runImmediately, Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", false)
                .putExtra("single", true)
                .putExtra("runImmediately", runImmediately));
        checkAndDoActivityFinish(activity, finish);
    }

    static void processFreezeAction(Context context, String pkgName, boolean askRun, Activity activity, boolean finish) {
        startService(context, new Intent(context, FUFService.class)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", true)
                .putExtra("single", true));
        checkAndDoActivityFinish(activity, finish);
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

    /**
     * @param packageName 应用包名
     * @return true 则已冻结
     */
    static boolean realGetFrozenStatus(Context context, String packageName, PackageManager pm) {
        return (Support.checkRootFrozen(context, packageName, pm) || Support.checkMRootFrozen(context, packageName));
    }

    static void checkAndSetOrganizationName(Context context, String name) {
        if (Build.VERSION.SDK_INT >= 24 && Support.isDeviceOwner(context))
            getDevicePolicyManager(context).setOrganizationName(DeviceAdminReceiver.getComponentName(context), name);
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
