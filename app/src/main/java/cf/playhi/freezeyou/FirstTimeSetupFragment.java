package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;
import static cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite;

public class FirstTimeSetupFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.first_time_setup_pr);
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(android.R.id.list);
        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final AppPreferences appPreferences = new AppPreferences(getActivity());
        SettingsUtils.syncAndCheckSharedPreference(
                getActivity().getApplicationContext(),
                getActivity(), sharedPreferences, key, appPreferences);
        updatePrefSummary(findPreference(key));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            switch (key) {
                case "howToUse":
                    requestOpenWebSite(getActivity(),
                            String.format("https://www.zidon.net/%1$s/guide/how-to-use.html",
                                    getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                    break;
                default:
                    break;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

}
