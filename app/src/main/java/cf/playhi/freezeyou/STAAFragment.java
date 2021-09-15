package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import cf.playhi.freezeyou.utils.MoreUtils;

import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class STAAFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.stma_add_pr);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
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
                            hour = Integer.parseInt(sHour);
                            minutes = Integer.parseInt(sMin);
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
