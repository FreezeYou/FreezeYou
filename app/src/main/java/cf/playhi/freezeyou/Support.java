package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;

import static cf.playhi.freezeyou.AccessibilityUtils.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.AccessibilityUtils.openAccessibilitySettings;
import static cf.playhi.freezeyou.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.DevicePolicyManagerUtils.getDevicePolicyManager;
import static cf.playhi.freezeyou.LauncherShortcutUtils.createShortCut;
import static cf.playhi.freezeyou.MoreUtils.copyToClipboard;
import static cf.playhi.freezeyou.NotificationUtils.createNotification;
import static cf.playhi.freezeyou.NotificationUtils.deleteNotification;
import static cf.playhi.freezeyou.OneKeyListUtils.addToOneKeyList;
import static cf.playhi.freezeyou.OneKeyListUtils.existsInOneKeyList;
import static cf.playhi.freezeyou.OneKeyListUtils.removeFromOneKeyList;
import static cf.playhi.freezeyou.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.ProcessUtils.fAURoot;
import static cf.playhi.freezeyou.ServiceUtils.startService;
import static cf.playhi.freezeyou.TasksUtils.onFApplications;
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
        if ((!"cf.playhi.freezeyou".equals(pkgName)) && (!pkgName.equals(currentPackage)) && (!isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(context, pkgName))) {
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
                        onFApplications(context, pkgName);
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
        if ((!"cf.playhi.freezeyou".equals(pkgName)) && (!pkgName.equals(currentPackage)) && (!isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(context, pkgName))) {
            if (getDevicePolicyManager(context).setApplicationHidden(
                    DeviceAdminReceiver.getComponentName(context), pkgName, hidden)) {
                if (hidden) {
                    onFApplications(context, pkgName);
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
                        if ((!"cf.playhi.freezeyou".equals(aPkgNameList)) && (!currentPackage.equals(aPkgNameList)) && (!isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(context, aPkgNameList))) {
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
                            onFApplications(context, aPkgNameList);
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
                        if ((!"cf.playhi.freezeyou".equals(aPkgNameList)) && (!currentPackage.equals(aPkgNameList)) && (!checkMRootFrozen(context, aPkgNameList)) && (!isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(context, aPkgNameList))) {
                            if (getDevicePolicyManager(context).setApplicationHidden(
                                    DeviceAdminReceiver.getComponentName(context), aPkgNameList, true)) {
                                onFApplications(context, aPkgNameList);
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
        return (checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName));
    }

    static void checkAndSetOrganizationName(Context context, String name) {
        if (Build.VERSION.SDK_INT >= 24 && isDeviceOwner(context))
            getDevicePolicyManager(context).setOrganizationName(DeviceAdminReceiver.getComponentName(context), name);
    }

    @TargetApi(21)
    private static boolean isAppStillNotifying(String pkgName) {
        if (pkgName != null) {
            StatusBarNotification[] statusBarNotifications = MyNotificationListenerService.getStatusBarNotifications();
            if (statusBarNotifications != null) {
                for (StatusBarNotification aStatusBarNotifications : statusBarNotifications) {
                    if (pkgName.equals(aStatusBarNotifications.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(Context context, String pkgName) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new AppPreferences(context).getBoolean("avoidFreezeNotifyingApplications", false) && isAppStillNotifying(pkgName);
        } else {
            return false;
        }
    }


    static void checkAddOrRemove(Context context, String pkgNames, String pkgName, String oneKeyName) {
        if (existsInOneKeyList(pkgNames, pkgName)) {
            showToast(context,
                    removeFromOneKeyList(context,
                            oneKeyName,
                            pkgName) ? R.string.removed : R.string.removeFailed);
        } else {
            showToast(context,
                    addToOneKeyList(context,
                            oneKeyName,
                            pkgName) ? R.string.added : R.string.addFailed);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (context.getString(R.string.sFreezeOnceQuit).equals(oneKeyName)) {
                if (!preferences.getBoolean("freezeOnceQuit", false)) {
                    preferences.edit().putBoolean("freezeOnceQuit", true).apply();
                    new AppPreferences(context).put("freezeOnceQuit", true);
                }
                if (!isAccessibilitySettingsOn(context)) {
                    showToast(context, R.string.needActiveAccessibilityService);
                    openAccessibilitySettings(context);
                }
            }
        }
    }

    static void showChooseActionPopupMenu(final Context context, View view, final String pkgName, final String name) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.main_single_choose_action_menu);

        final AppPreferences sharedPreferences = new AppPreferences(context);

        final String pkgNames = sharedPreferences.getString(context.getString(R.string.sAutoFreezeApplicationList), "");
        if (existsInOneKeyList(pkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToOneKeyList).setTitle(R.string.removeFromOneKeyList);
        }

        final String FreezeOnceQuitPkgNames = sharedPreferences.getString(context.getString(R.string.sFreezeOnceQuit), "");
        if (existsInOneKeyList(FreezeOnceQuitPkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToFreezeOnceQuit).setTitle(R.string.removeFromFreezeOnceQuit);
        }

        final String UFPkgNames = sharedPreferences.getString(context.getString(R.string.sOneKeyUFApplicationList), "");
        if (existsInOneKeyList(UFPkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToOneKeyUFList).setTitle(R.string.removeFromOneKeyUFList);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.main_sca_menu_addToFreezeOnceQuit:
                        Support.checkAddOrRemove(context, FreezeOnceQuitPkgNames, pkgName, context.getString(R.string.sFreezeOnceQuit));
                        break;
                    case R.id.main_sca_menu_addToOneKeyList:
                        Support.checkAddOrRemove(context, pkgNames, pkgName, context.getString(R.string.sAutoFreezeApplicationList));
                        break;
                    case R.id.main_sca_menu_addToOneKeyUFList:
                        Support.checkAddOrRemove(context, UFPkgNames, pkgName, context.getString(R.string.sOneKeyUFApplicationList));
                        break;
                    case R.id.main_sca_menu_appDetail:
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", pkgName, null);
                        intent.setData(uri);
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(context, e.getLocalizedMessage());
                        }
                        break;
                    case R.id.main_sca_menu_copyPkgName:
                        copyToClipboard(context, pkgName);
                        break;
                    case R.id.main_sca_menu_disableAEnable:
                        if (!(context.getString(R.string.notAvailable).equals(name))) {
                            context.startActivity(new Intent(context, Freeze.class).putExtra("pkgName", pkgName).putExtra("auto", false));
                        }
                        break;
                    case R.id.main_sca_menu_createDisEnableShortCut:
                        createShortCut(
                                name,
                                pkgName,
                                getApplicationIcon(context, pkgName, getApplicationInfoFromPkgName(pkgName, context), false),
                                Freeze.class,
                                "FreezeYou! " + pkgName,
                                context
                        );
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
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
