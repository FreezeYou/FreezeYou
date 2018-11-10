package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.Window.FEATURE_NO_TITLE;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class ShortcutLauncherFolderActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, ShortcutLauncherFolderActivity.class));
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_new_round));
            setResult(RESULT_OK, intent);
            finish();
        } else {
            setContentView(R.layout.shortcut_launcher_folder);
            GridView slf_apps_gridView = findViewById(R.id.slf_apps_gridView);

            slf_apps_gridView.setColumnWidth((int) (getResources().getDimension(android.R.dimen.app_icon_size) * 1.5));

            ArrayList<HashMap<String, Object>> folderItems = new ArrayList<>();


            final List<ApplicationInfo> applicationInfo = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            int size = applicationInfo.size();
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> map = new HashMap<>();

                map.put("Icon", ApplicationIconUtils.getApplicationIcon(this, applicationInfo.get(i).packageName, null, false));
                map.put("Label", ApplicationLabelUtils.getApplicationLabel(this, null, null, applicationInfo.get(i).packageName));

                folderItems.add(map);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(this, folderItems,
                    R.layout.shortcut_launcher_folder_item, new String[]{"Icon", "Label"},
                    new int[]{R.id.slfi_imageView, R.id.slfi_textView});

            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data,
                                            String textRepresentation) {
                    if (view instanceof ImageView && data instanceof Drawable) {
                        ImageView imageView = (ImageView) view;
                        imageView.setImageDrawable((Drawable) data);
                        return true;
                    } else
                        return false;
                }
            });

            slf_apps_gridView.setAdapter(simpleAdapter);

            slf_apps_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Support.checkAndStartApp(
                            ShortcutLauncherFolderActivity.this,
                            applicationInfo.get(position).packageName,
                            null,
                            false);
                }
            });
        }
    }
}
