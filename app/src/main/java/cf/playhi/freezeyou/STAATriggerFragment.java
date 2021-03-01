package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import cf.playhi.freezeyou.utils.AccessibilityUtils;
import cf.playhi.freezeyou.utils.MoreUtils;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class STAATriggerFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.stma_add_trigger_pr);
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updatePrefSummary(findPreference(s));
        switch (s) {
            case "stma_add_trigger":
                String stma_add_trigger = sharedPreferences.getString("stma_add_trigger", "");
                if ("onApplicationsForeground".equals(stma_add_trigger) && !AccessibilityUtils.isAccessibilitySettingsOn(getActivity())) {
                    showToast(getActivity(), R.string.needActiveAccessibilityService);
                    AccessibilityUtils.openAccessibilitySettings(getActivity());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case "stma_add_help":
                    MoreUtils.requestOpenWebSite(getActivity(),
                            String.format("https://www.zidon.net/%1$s/guide/schedules.html",
                                    getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                    break;
                default:
                    break;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
