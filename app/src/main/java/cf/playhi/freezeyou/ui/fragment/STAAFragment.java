package cf.playhi.freezeyou.ui.fragment;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.utils.MoreUtils;

public class STAAFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.stma_add_pr);
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

}
