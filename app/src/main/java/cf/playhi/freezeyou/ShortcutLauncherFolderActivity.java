package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
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
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ApplicationIconUtils.getGrayBitmap;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
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

            slf_apps_gridView.setColumnWidth((int) (getResources().getDimension(android.R.dimen.app_icon_size) * 1.8));
//            if (Build.VERSION.SDK_INT>=16)
//                slf_apps_gridView.setVerticalSpacing(slf_apps_gridView.getRequestedColumnWidth()/slf_apps_gridView.getNumColumns());

            ArrayList<HashMap<String, Object>> folderItems = new ArrayList<>();


            final List<ApplicationInfo> applicationInfo = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            int size = applicationInfo == null ? 0 : applicationInfo.size();
            String pkgName;
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> map = new HashMap<>();
                pkgName = applicationInfo.get(i).packageName;
                map.put("Icon",
                        Support.realGetFrozenStatus(this, pkgName, null) ? new BitmapDrawable(getGrayBitmap(getBitmapFromDrawable(getApplicationIcon(this, pkgName, applicationInfo.get(i), false)))) : getApplicationIcon(this, pkgName, applicationInfo.get(i), false)
                );
                map.put("Label", getApplicationLabel(this, null, null, pkgName));

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
                    Support.checkFrozenStatusAndStartApp(
                            ShortcutLauncherFolderActivity.this,
                            applicationInfo.get(position).packageName);
                }
            });

        }
    }

}
