package cf.playhi.freezeyou;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.showToast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    PackageManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.spr);//preferences
        pm = getActivity().getPackageManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "firstIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.FirstIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.FirstIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(),R.string.ciFinishedToast);
                break;
            case "secondIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.SecondIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.SecondIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(),R.string.ciFinishedToast);
                break;
            case "thirdIconEnabled":
                if (sharedPreferences.getBoolean(s, true)) {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.ThirdIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pm.setComponentEnabledSetting(new ComponentName(getActivity(), "cf.playhi.freezeyou.ThirdIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                showToast(getActivity(),R.string.ciFinishedToast);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
