package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import cf.playhi.freezeyou.utils.MoreUtils;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class STAAFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.stma_add_pr);
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
            case "stma_add_time":
                String time = sharedPreferences.getString("stma_add_time", "09:09");
                if (time != null) {
                    if (time.contains(":")) {
                        String sHour = time.substring(0, time.indexOf(":"));
                        String sMin = time.substring(time.indexOf(":") + 1);
                        if ("".equals(sHour))
                            sHour = "0";
                        if ("".equals(sMin))
                            sMin = "0";

                        int hour;
                        int minutes;
                        try {
                            hour = Integer.valueOf(sHour);
                            minutes = Integer.valueOf(sMin);
                        } catch (Exception e) {
                            showToast(getActivity(),
                                    getString(R.string.minutesShouldBetween)
                                            + System.getProperty("line.separator")
                                            + getString(R.string.hourShouldBetween));
                            break;
                        }

                        if (hour < 0 || hour >= 24) {
                            showToast(getActivity(), R.string.hourShouldBetween);
                        }
                        if (minutes < 0 || minutes > 59) {
                            showToast(getActivity(), R.string.minutesShouldBetween);
                        }
                    } else {
                        showToast(getActivity(), R.string.mustContainColon);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case "stma_add_help":
                    MoreUtils.requestOpenWebSite(getActivity(), "https://zidon.net/zh-CN/guide/schedules.html");
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
