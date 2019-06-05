package cf.playhi.freezeyou;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.PreferenceSupport.initSummary;
import static cf.playhi.freezeyou.PreferenceSupport.updatePrefSummary;

public class FirstTimeSetupFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

}
