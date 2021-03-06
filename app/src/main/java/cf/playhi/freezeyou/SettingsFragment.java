package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.SettingsUtils.syncAndCheckSharedPreference;
import static cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings;
import static cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.utils.DataStatisticsUtils.resetTimes;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FileUtils.deleteAllFiles;
import static cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite;
import static cf.playhi.freezeyou.utils.NotificationUtils.startAppNotificationSettingsSystemActivity;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.removeUninstalledFromOneKeyList;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;
import static cf.playhi.freezeyou.utils.VersionUtils.checkUpdate;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.spr, rootKey);//preferences

        if (isDeviceOwner(getActivity())) {
            PreferenceScreen dangerZonePreferenceScreen = findPreference("dangerZone");
            if (dangerZonePreferenceScreen != null) {
                dangerZonePreferenceScreen.removePreference(findPreference("clearAllUserData"));
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PreferenceScreen backgroundServicePreferenceScreen = findPreference("backgroundService");
            if (backgroundServicePreferenceScreen != null) {
                backgroundServicePreferenceScreen.removeAll();
            }
            PreferenceScreen rootPreferenceScreen = findPreference("root");
            if (rootPreferenceScreen != null) {
                rootPreferenceScreen.removePreference(findPreference("backgroundService"));
            }
        }
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null)
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (getActivity() != null) {
            final AppPreferences appPreferences = new AppPreferences(getActivity());
            syncAndCheckSharedPreference(
                    getActivity().getApplicationContext(),
                    getActivity(), sharedPreferences, s, appPreferences);
            updatePrefSummary(findPreference(s));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
        Activity activity = getActivity();
        if (activity != null) {
            String key = preference.getKey();
            if (key != null) {
                switch (key) {
                    case "clearAllCache":
                        try {
                            activity.getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().clear().apply();
                            deleteAllFiles(activity.getCacheDir(), false);
                            deleteAllFiles(activity.getExternalCacheDir(), false);
                            deleteAllFiles(new File(activity.getFilesDir() + "/icon"), false);
                            showToast(activity, R.string.success);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(activity, R.string.failed);
                        }
                        break;
                    case "clearNameCache":
                        activity.getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().clear().apply();
                        showToast(activity, R.string.success);
                        break;
                    case "clearIconCache":
                        try {
                            deleteAllFiles(new File(activity.getFilesDir() + "/icon"), false);
                            deleteAllFiles(new File(activity.getCacheDir() + "/icon"), false);
                            showToast(activity, R.string.success);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(activity, R.string.failed);
                        }
                        break;
                    case "checkUpdate":
                        checkUpdate(activity);
                        break;
                    case "helpTranslate":
                        requestOpenWebSite(
                                activity,
                                "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
                        );
                        break;
                    case "thanksList":
                        requestOpenWebSite(activity, "https://freezeyou.playhi.net/thanks.html");
                        break;
                    case "configureAccessibilityService":
                        openAccessibilitySettings(activity);
                        break;
                    case "faq":
                        requestOpenWebSite(activity,
                                String.format("https://www.zidon.net/%1$s/faq/",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        break;
                    case "uninstall":
                        Intent uninstall =
                                new Intent(
                                        Intent.ACTION_DELETE,
                                        Uri.parse("package:cf.playhi.freezeyou")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(uninstall);
                        break;
                    case "deleteAllScheduledTasks":
                        buildAlertDialog(activity, android.R.drawable.ic_dialog_alert, R.string.askIfDel, R.string.caution)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    File file;
                                    for (String name : new String[]{"scheduledTasks", "scheduledTriggerTasks"}) {
                                        file = activity.getApplicationContext().getDatabasePath(name);
                                        if (file.exists())
                                            file.delete();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .create().show();
                        break;
                    case "clearAllUserData":
                        buildAlertDialog(activity, R.mipmap.ic_launcher_new_round, R.string.clearAllUserData, R.string.notice)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                                    if (activityManager != null && Build.VERSION.SDK_INT >= 19) {
                                        try {
                                            showToast(activity, activityManager.clearApplicationUserData() ? R.string.success : R.string.failed);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            showToast(activity, R.string.failed);
                                        }
                                    } else {
                                        showToast(activity, R.string.sysVerLow);
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .create().show();
                        break;
                    case "manageIpaAutoAllow":
                        activity.startActivity(
                                new Intent(activity, UriAutoAllowManageActivity.class)
                                        .putExtra("isIpaMode", true)
                        );
                        break;
                    case "notificationBar_more":
                        startAppNotificationSettingsSystemActivity(
                                activity, activity.getPackageName(),
                                activity.getApplicationInfo().uid
                        );
                        break;
                    case "clearUninstalledPkgsInOKFFList":
                        if (removeUninstalledFromOneKeyList(activity,
                                getString(R.string.sAutoFreezeApplicationList))) {
                            showToast(activity, R.string.success);
                        } else {
                            showToast(activity, R.string.failed);
                        }
                        break;
                    case "clearUninstalledPkgsInOKUFList":
                        if (removeUninstalledFromOneKeyList(activity,
                                getString(R.string.sOneKeyUFApplicationList))) {
                            showToast(activity, R.string.success);
                        } else {
                            showToast(activity, R.string.failed);
                        }
                        break;
                    case "clearUninstalledPkgsInFOQList":
                        if (removeUninstalledFromOneKeyList(activity,
                                getString(R.string.sFreezeOnceQuit))) {
                            showToast(activity, R.string.success);
                        } else {
                            showToast(activity, R.string.failed);
                        }
                        break;
                    case "backupAndRestore":
                        startActivity(new Intent(activity, BackupMainActivity.class));
                        break;
                    case "howToUse":
                        requestOpenWebSite(activity,
                                String.format("https://www.zidon.net/%1$s/guide/how-to-use.html",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        break;
                    case "resetFreezeTimes":
                        askIfResetTimes("ApplicationsFreezeTimes");
                        break;
                    case "resetUFTimes":
                        askIfResetTimes("ApplicationsUFreezeTimes");
                        break;
                    case "resetUseTimes":
                        askIfResetTimes("ApplicationsUseTimes");
                        break;
                    default:
                        break;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void askIfResetTimes(final String dbName) {
        Activity activity = getActivity();
        if (activity!=null) {
            buildAlertDialog(
                    activity, android.R.drawable.ic_dialog_alert, R.string.askIfDel, R.string.caution)
                    .setPositiveButton(R.string.yes, (dialog, which) -> resetTimes(activity, dbName))
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        }
    }

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
        if (getActivity() != null)
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(this);
    }
}
