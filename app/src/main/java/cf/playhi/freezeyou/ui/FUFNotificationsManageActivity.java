package cf.playhi.freezeyou.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.AlertDialogUtils;
import cf.playhi.freezeyou.utils.ApplicationLabelUtils;
import cf.playhi.freezeyou.utils.NotificationUtils;

import static cf.playhi.freezeyou.utils.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;

public class FUFNotificationsManageActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fufnm_main);
        processActionBar(getSupportActionBar());

        init();
    }

    private void init() {
        ListView fufnmListView = findViewById(R.id.fufnm_listView);
        final AppPreferences defaultSharedPreferences = new AppPreferences(this);
        final String string = defaultSharedPreferences.getString("notifying", "");
        if (string != null && !"".equals(string)) {
            List<HashMap<String, String>> pkgList = new ArrayList<>();
            String[] strings = string.split(",");
            for (String aString : strings) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Name", ApplicationLabelUtils.getApplicationLabel(this, null, null, aString));
                hashMap.put("PkgName", aString);
                pkgList.add(hashMap);
            }
            final SimpleAdapter adapter = new SimpleAdapter(this, pkgList,
                    R.layout.fufnm_list, new String[]{"Name", "PkgName"}, new int[]{R.id.fufnml_name, R.id.fufnml_pkgName});
            fufnmListView.setAdapter(adapter);
            fufnmListView.setOnItemClickListener((parent, view, position, id) -> {
                HashMap<String, String> hashMap = ((HashMap<String, String>) adapter.getItem(position));
                final String pkgName = hashMap.get("PkgName");
                AlertDialogUtils.buildAlertDialog(
                        FUFNotificationsManageActivity.this,
                        null,
                        hashMap.get("Name") + System.getProperty("line.separator") + pkgName,
                        getString(R.string.askIfDel))
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            defaultSharedPreferences.put("notifying", string.replace(pkgName + ",", ""));
                            NotificationUtils.deleteNotification(FUFNotificationsManageActivity.this, pkgName);
                            init();
                        })
                        .create().show();
            });
        } else {
            List<HashMap<String, String>> pkgList = new ArrayList<>();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Name", getString(R.string.notAvailable));
            hashMap.put("PkgName", getString(R.string.notAvailable));
            pkgList.add(hashMap);
            final SimpleAdapter adapter = new SimpleAdapter(this, pkgList,
                    R.layout.fufnm_list, new String[]{"Name", "PkgName"}, new int[]{R.id.fufnml_name, R.id.fufnml_pkgName});
            fufnmListView.setAdapter(adapter);
            fufnmListView.setOnItemClickListener(null);
        }
    }

}
