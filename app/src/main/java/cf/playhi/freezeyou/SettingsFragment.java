package cf.playhi.freezeyou;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;

import static cf.playhi.freezeyou.AccessibilityUtils.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.AccessibilityUtils.openAccessibilitySettings;
import static cf.playhi.freezeyou.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.DevicePolicyManagerUtils.getDevicePolicyManager;
import static cf.playhi.freezeyou.DevicePolicyManagerUtils.openDevicePolicyManager;
import static cf.playhi.freezeyou.MoreUtils.requestOpenWebSite;
import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.ToastUtils.showToast;
import static cf.playhi.freezeyou.VersionUtils.checkUpdate;
import static cf.playhi.freezeyou.VersionUtils.getVersionName;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PackageManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.spr);//preferences
        if (getVersionName(getActivity()).contains("g"))
            ((PreferenceCategory) findPreference("more")).removePreference(findPreference("donate"));
        pm = getActivity().getPackageManager();
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        final AppPreferences appPreferences = new AppPreferences(getActivity());
        switch (s) {
            case "firstIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.FirstIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.FirstIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(), R.string.ciFinishedToast);
                break;
            case "secondIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.SecondIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.SecondIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(), R.string.ciFinishedToast);
                break;
            case "thirdIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.ThirdIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.ThirdIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(), R.string.ciFinishedToast);
                break;
            case "shortCutOneKeyFreezeAdditionalOptions":
                if (!"nothing".equals(sharedPreferences.getString(s, "nothing"))) {
                    appPreferences.put(s, sharedPreferences.getString(s, "nothing"));
                    DevicePolicyManager devicePolicyManager = getDevicePolicyManager(getActivity());
                    if (devicePolicyManager != null && !devicePolicyManager.isAdminActive(
                            new ComponentName(getActivity(), DeviceAdminReceiver.class))) {
                        openDevicePolicyManager(getActivity());
                    }
                }
                break;
            case "uiStyleSelection":
                showToast(getActivity(), R.string.willTakeEffectsNextLaunch);
                getActivity().recreate();
                break;
            case "onekeyFreezeWhenLockScreen":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if (sharedPreferences.getBoolean(s, false)) {
                    ServiceUtils.startService(
                            getActivity(),
                            new Intent(getActivity().getApplicationContext(), ScreenLockOneKeyFreezeService.class));
                } else {
                    getActivity().stopService(new Intent(getActivity().getApplicationContext(), ScreenLockOneKeyFreezeService.class));
                }
                break;
            case "freezeOnceQuit":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if (sharedPreferences.getBoolean(s, false) && !isAccessibilitySettingsOn(getActivity())) {
                    showToast(getActivity(), R.string.needActiveAccessibilityService);
                    openAccessibilitySettings(getActivity());
                }
                break;
            case "useForegroundService":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "openImmediately":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "openAndUFImmediately":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "notificationBarFreezeImmediately":
                appPreferences.put(s, sharedPreferences.getBoolean(s, true));
                break;
            case "notificationBarDisableSlideOut":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "notificationBarDisableClickDisappear":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "showInRecents":
                appPreferences.put(s, sharedPreferences.getBoolean(s, true));
                break;
            case "lesserToast":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "debugModeEnabled":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                break;
            case "avoidFreezeForegroundApplications":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if ((sharedPreferences.getBoolean(s, false)) && !isAccessibilitySettingsOn(getActivity())) {
                    showToast(getActivity(), R.string.needActiveAccessibilityService);
                    openAccessibilitySettings(getActivity());
                }
                break;
            case "organizationName":
                Support.checkAndSetOrganizationName(getActivity(), sharedPreferences.getString(s, null));
                break;
            case "avoidFreezeNotifyingApplications":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if (Build.VERSION.SDK_INT >= 21) {//这项不兼容 5.0 以下了
                    String enabledNotificationListeners = Settings.Secure.getString(getActivity().getContentResolver(), "enabled_notification_listeners");
                    if (enabledNotificationListeners != null && !enabledNotificationListeners.contains("cf." + "playhi." + "freezeyou")) {
                        try {
                            getActivity().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        } catch (Exception e) {
                            showToast(getActivity(), R.string.failed);
                        }
                    }
                }
                break;
//            case "languagePref":
//                checkLanguage(getActivity().getApplicationContext());
//                showToast(getActivity(),R.string.willTakeEffectsNextLaunch);
//                break;
            default:
                break;
        }
        updatePrefSummary(findPreference(s));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case "clearNameCache":
                    getActivity().getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().clear().apply();
                    break;
                case "clearIconCache":
                    try {
                        File file = new File(getActivity().getFilesDir() + "/icon");
                        if (file.exists() && file.isDirectory()) {
                            File[] childFile = file.listFiles();
                            if (childFile == null || childFile.length == 0) {
                                file.delete();
                            } else {
                                for (File f : childFile) {
                                    if (f.isFile()) {
                                        f.delete();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "checkUpdate":
                    checkUpdate(getActivity());
                    break;
                case "helpTranslate":
                    requestOpenWebSite(getActivity(), "https://crwd.in/freezeyou");
                    break;
                case "donate":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.net/sponsorship.html");
                    break;
                case "thanksList":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.net/thanks.html");
                    break;
                case "configureAccessibilityService":
                    openAccessibilitySettings(getActivity());
                    break;
                case "faq":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.net/faq.html");
                    break;
                case "uninstall":
                    Intent uninstall =
                            new Intent(
                                    Intent.ACTION_DELETE,
                                    Uri.parse("package:cf.playhi.freezeyou")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(uninstall);
                    break;
                case "deleteAllScheduledTasks":
                    buildAlertDialog(getActivity(), android.R.drawable.ic_dialog_alert, R.string.askIfDel, R.string.caution)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File file;
                                    for (String name : new String[]{"scheduledTasks", "scheduledTriggerTasks"}) {
                                        file = getActivity().getApplicationContext().getDatabasePath(name);
                                        if (file.exists())
                                            file.delete();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create().show();
                    break;
                case "clearAllUserData":
                    buildAlertDialog(getActivity(), R.mipmap.ic_launcher_new_round, R.string.clearAllUserData, R.string.notice)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                                    if (activityManager != null && Build.VERSION.SDK_INT >= 19) {
                                        try {
                                            showToast(getActivity(), activityManager.clearApplicationUserData() ? R.string.success : R.string.failed);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            showToast(getActivity(), R.string.failed);
                                        }
                                    } else {
                                        showToast(getActivity(), R.string.sysVerLow);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .create().show();
                    break;
//                case "backup":
//
//                    break;
//                case "restore":
//
//                    break;
                default:
                    break;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
//
//    private void deleteAllFiles(File file, boolean deleteSelfFolder) throws IOException {
//        if (file.exists()) {
//            if (file.isFile()) {
//                if (!file.delete())
//                    throw new IOException(file.getAbsolutePath() + " delete failed");
//            } else if (file.isDirectory()) {
//                String[] strings = file.list();
//                if (deleteSelfFolder && strings == null) {
//                    if (!file.delete())
//                        throw new IOException(file.getAbsolutePath() + " delete failed");
//                } else {
//                    for (String s : strings) {
//                        deleteAllFiles(new File(s), true);
//                    }
//                    if (deleteSelfFolder) {
//                        if (!file.delete())
//                            throw new IOException(file.getAbsolutePath() + " delete failed");
//                    }
//                }
//            }
//        }
//    }
//
//    private void copyAllFiles(File file, String destination) throws IOException {
//        if (file.exists()) {
//            if (file.isFile()) {
//                if (new File(destination).getParentFile().exists()) {
//                    if (!new File(destination).getParentFile().mkdirs())
//                        throw new IOException("Cannot make dirs for " + destination);
//                }
//                if (!new File(destination).exists())
//                    if (!new File(destination).createNewFile())
//                        throw new IOException("Cannot create file " + destination);
//                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
//                int i;
//                byte[] bt = new byte[Byte.MAX_VALUE];
//                while ((i = bis.read(bt)) != -1) {
//                    bos.write(bt, 0, i);
//                }
//                bos.close();
//                bis.close();
//            } else if (file.isDirectory()) {
//                File[] files = file.listFiles();
//                for (File f : files) {
//                    copyAllFiles(f, destination + File.separator + f.getName());
//                }
//            }
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
