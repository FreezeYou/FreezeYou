package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import static cf.playhi.freezeyou.Support.showToast;

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
                if (time.contains(":")){
                    int hour = Integer.valueOf(time.substring(0, time.indexOf(":")));
                    int minutes = Integer.valueOf(time.substring(time.indexOf(":") + 1));
                    if (hour < 0 || hour >= 24) {
                        showToast(getActivity(),R.string.hourShouldBetween);
                    }
                    if (minutes < 0 || minutes > 59) {
                        showToast(getActivity(),R.string.minutesShouldBetween);
                    }
                } else {
                    showToast(getActivity(),R.string.mustContainColon);
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
                    Support.requestOpenWebSite(getActivity(), "https://wiki.playhi.cf/index.php?title=%E8%AE%A1%E5%88%92%E4%BB%BB%E5%8A%A1_-_FreezeYou");
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

    //https://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    //https://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }
}
