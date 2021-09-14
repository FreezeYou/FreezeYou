package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import static cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite;
import static cf.playhi.freezeyou.utils.VersionUtils.checkUpdate;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.moreSettings);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.spr, rootKey);//preferences

        PreferenceScreen rootPreferenceScreen = getPreferenceScreen();
        if (rootPreferenceScreen != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                rootPreferenceScreen.removePreference(findPreference("backgroundService"));
            }
        }

    }

    @Override
    public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
        Activity activity = getActivity();
        if (activity != null) {
            String key = preference.getKey();
            if (key != null) {
                switch (key) {
                    case "checkUpdate":
                        checkUpdate(activity);
                        break;
                    case "helpTranslate":
                        requestOpenWebSite(
                                activity,
                                "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
                        );
                        break;
                    case "thanksList":
                        requestOpenWebSite(activity,
                                String.format("https://www.zidon.net/%1$s/thanks/",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        break;
                    case "faq":
                        requestOpenWebSite(activity,
                                String.format("https://www.zidon.net/%1$s/faq/",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        break;
                    case "backupAndRestore":
                        startActivity(new Intent(activity, BackupMainActivity.class));
                        break;
                    case "howToUse":
                        requestOpenWebSite(activity,
                                String.format("https://www.zidon.net/%1$s/guide/how-to-use.html",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        break;
                    default:
                        break;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

}
