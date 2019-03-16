package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

// Important!
// Also used to deal with ipa_autoAllow
public class UriAutoAllowManageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uaam_main);
        processActionBar(getActionBar());

        init();
    }

    private void init() {
        ListView uaamListView = findViewById(R.id.uaam_listView);
        final boolean ipaMode = getIntent().getBooleanExtra("isIpaMode", false);//Install Package
        if (ipaMode)
            setTitle(R.string.manageIpaAutoAllow);
        final AppPreferences defaultSharedPreferences = new AppPreferences(this);
        final String string =
                defaultSharedPreferences.getString(
                        ipaMode ?
                                "installPkgs_autoAllowPkgs_allows" :
                                "uriAutoAllowPkgs_allows",
                        ""
                );
        if (string != null && !"".equals(string)) {
            List<HashMap<String, String>> pkgList = new ArrayList<>();
            String[] strings = string.split(",");
            for (String aString : strings) {
                String s = new String(Base64.decode(aString, Base64.DEFAULT));
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Name",
                        ApplicationLabelUtils.getApplicationLabel(
                                this, null, null, s));
                hashMap.put("PkgName", s);
                pkgList.add(hashMap);
            }
            final SimpleAdapter adapter = new SimpleAdapter(this, pkgList,
                    R.layout.uaam_list, new String[]{"Name", "PkgName"}, new int[]{R.id.uaaml_name, R.id.uaaml_pkgName});
            uaamListView.setAdapter(adapter);
            uaamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, String> hashMap = ((HashMap<String, String>) adapter.getItem(position));
                    final String pkgName = hashMap.get("PkgName");
                    if (pkgName != null) {
                        AlertDialogUtils.buildAlertDialog(
                                UriAutoAllowManageActivity.this,
                                null,
                                hashMap.get("Name") + System.getProperty("line.separator") + pkgName,
                                getString(R.string.askIfDel))
                                .setNegativeButton(R.string.no, null)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        defaultSharedPreferences.put(
                                                ipaMode ?
                                                        "installPkgs_autoAllowPkgs_allows" :
                                                        "uriAutoAllowPkgs_allows",
                                                string.replace(
                                                        Base64.encodeToString(
                                                                pkgName.getBytes(), Base64.DEFAULT) + ",",
                                                        "")
                                        );
                                        init();
                                    }
                                })
                                .create().show();
                    }
                }
            });
        } else {
            List<HashMap<String, String>> pkgList = new ArrayList<>();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Name", getString(R.string.notAvailable));
            hashMap.put("PkgName", getString(R.string.notAvailable));
            pkgList.add(hashMap);
            final SimpleAdapter adapter = new SimpleAdapter(this, pkgList,
                    R.layout.uaam_list, new String[]{"Name", "PkgName"}, new int[]{R.id.uaaml_name, R.id.uaaml_pkgName});
            uaamListView.setAdapter(adapter);
            uaamListView.setOnItemClickListener(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
