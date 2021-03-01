package cf.playhi.freezeyou;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setDividerHeight(sp.getBoolean("displayListDivider", false) ? 1 : -1);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.spr);//preferences

        if (isDeviceOwner(getActivity())) {
            ((PreferenceScreen) findPreference("dangerZone"))
                    .removePreference(findPreference("clearAllUserData"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((PreferenceScreen) findPreference("backgroundService")).removeAll();
            ((PreferenceScreen) findPreference("root"))
                    .removePreference(findPreference("backgroundService"));
        }
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        final AppPreferences appPreferences = new AppPreferences(getActivity());
        syncAndCheckSharedPreference(
                getActivity().getApplicationContext(),
                getActivity(), sharedPreferences, s, appPreferences);
        updatePrefSummary(findPreference(s));
    }

    @Override
    public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case "clearAllCache":
                    try {
                        getActivity().getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().clear().apply();
                        deleteAllFiles(getActivity().getCacheDir(), false);
                        deleteAllFiles(getActivity().getExternalCacheDir(), false);
                        deleteAllFiles(new File(getActivity().getFilesDir() + "/icon"), false);
                        showToast(getActivity(), R.string.success);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(getActivity(), R.string.failed);
                    }
                    break;
                case "clearNameCache":
                    getActivity().getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().clear().apply();
                    showToast(getActivity(), R.string.success);
                    break;
                case "clearIconCache":
                    try {
                        deleteAllFiles(new File(getActivity().getFilesDir() + "/icon"), false);
                        deleteAllFiles(new File(getActivity().getCacheDir() + "/icon"), false);
                        showToast(getActivity(), R.string.success);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(getActivity(), R.string.failed);
                    }
                    break;
                case "checkUpdate":
                    checkUpdate(getActivity());
                    break;
                case "helpTranslate":
                    requestOpenWebSite(
                            getActivity(),
                            "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
                    );
                    break;
                case "thanksList":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.net/thanks.html");
                    break;
                case "configureAccessibilityService":
                    openAccessibilitySettings(getActivity());
                    break;
                case "faq":
                    requestOpenWebSite(getActivity(),
                            String.format("https://www.zidon.net/%1$s/faq/",
                                    getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
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
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                File file;
                                for (String name : new String[]{"scheduledTasks", "scheduledTriggerTasks"}) {
                                    file = getActivity().getApplicationContext().getDatabasePath(name);
                                    if (file.exists())
                                        file.delete();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .create().show();
                    break;
                case "clearAllUserData":
                    buildAlertDialog(getActivity(), R.mipmap.ic_launcher_new_round, R.string.clearAllUserData, R.string.notice)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
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
                            })
                            .setNegativeButton(R.string.no, null)
                            .create().show();
                    break;
                case "manageIpaAutoAllow":
                    getActivity().startActivity(
                            new Intent(getActivity(), UriAutoAllowManageActivity.class)
                                    .putExtra("isIpaMode", true)
                    );
                    break;
                case "notificationBar_more":
                    startAppNotificationSettingsSystemActivity(
                            getActivity(), getActivity().getPackageName(),
                            getActivity().getApplicationInfo().uid
                    );
                    break;
                case "clearUninstalledPkgsInOKFFList":
                    if (removeUninstalledFromOneKeyList(getActivity(),
                            getString(R.string.sAutoFreezeApplicationList))) {
                        showToast(getActivity(), R.string.success);
                    } else {
                        showToast(getActivity(), R.string.failed);
                    }
                    break;
                case "clearUninstalledPkgsInOKUFList":
                    if (removeUninstalledFromOneKeyList(getActivity(),
                            getString(R.string.sOneKeyUFApplicationList))) {
                        showToast(getActivity(), R.string.success);
                    } else {
                        showToast(getActivity(), R.string.failed);
                    }
                    break;
                case "clearUninstalledPkgsInFOQList":
                    if (removeUninstalledFromOneKeyList(getActivity(),
                            getString(R.string.sFreezeOnceQuit))) {
                        showToast(getActivity(), R.string.success);
                    } else {
                        showToast(getActivity(), R.string.failed);
                    }
                    break;
                case "backupAndRestore":
                    startActivity(new Intent(getActivity(), BackupMainActivity.class));
                    break;
                case "howToUse":
                    requestOpenWebSite(getActivity(),
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
        return super.onPreferenceTreeClick(preference);
    }

    private void askIfResetTimes(final String dbName) {
        buildAlertDialog(getActivity(), android.R.drawable.ic_dialog_alert, R.string.askIfDel, R.string.caution)
                .setPositiveButton(R.string.yes, (dialog, which) -> resetTimes(getActivity(), dbName))
                .setNegativeButton(R.string.no, null)
                .create().show();
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
