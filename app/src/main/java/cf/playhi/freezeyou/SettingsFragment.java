package cf.playhi.freezeyou;

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
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.Support.buildAlertDialog;
import static cf.playhi.freezeyou.Support.checkUpdate;
import static cf.playhi.freezeyou.Support.getDevicePolicyManager;
import static cf.playhi.freezeyou.Support.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.Support.openAccessibilitySettings;
import static cf.playhi.freezeyou.Support.openDevicePolicyManager;
import static cf.playhi.freezeyou.Support.requestOpenWebSite;
import static cf.playhi.freezeyou.Support.showToast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PackageManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.spr);//preferences
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
                break;
            case "onekeyFreezeWhenLockScreen":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if (sharedPreferences.getBoolean(s, false)) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        getActivity().startForegroundService(new Intent(getActivity().getApplicationContext(), ScreenLockOneKeyFreezeService.class));
                    } else {
                        getActivity().startService(new Intent(getActivity().getApplicationContext(), ScreenLockOneKeyFreezeService.class));
                    }
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
            case "avoidFreezeForegroundApplications":
                appPreferences.put(s, sharedPreferences.getBoolean(s, false));
                if ((sharedPreferences.getBoolean(s, false)) && !isAccessibilitySettingsOn(getActivity())) {
                    showToast(getActivity(), R.string.needActiveAccessibilityService);
                    openAccessibilitySettings(getActivity());
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
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.cf/sponsorship.html");
                    break;
                case "thanksList":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.cf/thanks.html");
                    break;
                case "configureAccessibilityService":
                    openAccessibilitySettings(getActivity());
                    break;
                case "faq":
                    requestOpenWebSite(getActivity(), "https://freezeyou.playhi.cf/faq.html");
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
                    buildAlertDialog(getActivity(),android.R.drawable.ic_dialog_alert,R.string.askIfDel,R.string.caution)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File file;
                                    for (String name:new String[]{"scheduledTasks","scheduledTriggerTasks"}){
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
                default:
                    break;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
