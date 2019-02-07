package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ApplicationIconUtils.getGrayBitmap;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class MainGridActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        finishActivity(23001);
        setContentView(R.layout.main_grid_main);
        LinearLayout linearLayout = findViewById(R.id.mgm_linearLayout);

        final ArrayList<HashMap<String, Object>> folderItems = new ArrayList<>();

        final List<ApplicationInfo> applicationInfo = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

        final SimpleAdapter simpleAdapter = new SimpleAdapter(this, folderItems,
                R.layout.main_grid_main_item, new String[]{"Icon"},
                new int[]{R.id.mgmi_imageView});

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

        GridView gridView = generateGridView(simpleAdapter, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Support.checkFrozenStatusAndStartApp(
                        MainGridActivity.this,
                        (String) ((HashMap) simpleAdapter.getItem(position)).get("PkgName"),
                        null,null);
            }
        });

        linearLayout.addView(gridView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = applicationInfo == null ? 0 : applicationInfo.size();
                String pkgName;
                for (int i = 0; i < size; i++) {
                    HashMap<String, Object> map = new HashMap<>();
                    pkgName = applicationInfo.get(i).packageName;
                    map.put("Icon", checkAndGetFrozenStatusProcessedApplicationIcon(pkgName, applicationInfo.get(i)));
                    map.put("Label", getApplicationLabel(MainGridActivity.this, null, null, pkgName));
                    map.put("PkgName", pkgName);
                    folderItems.add(map);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            simpleAdapter.notifyDataSetChanged();
                        }
                    });
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("Icon", getResources().getDrawable(R.drawable.grid_add));
                map.put("Label", getString(R.string.add));
                map.put("PkgName", "freezeyou@add");
                folderItems.add(map);
            }
        }).start();

    }

    private Drawable checkAndGetFrozenStatusProcessedApplicationIcon(String pkgName, ApplicationInfo applicationInfo) {
        return Support.realGetFrozenStatus(MainGridActivity.this, pkgName, null)
                ?
                new BitmapDrawable(
                        getGrayBitmap(
                                getBitmapFromDrawable(
                                        getApplicationIcon(MainGridActivity.this, pkgName, applicationInfo, false))))
                :
                getApplicationIcon(
                        MainGridActivity.this, pkgName, applicationInfo, false);
    }

    private GridView generateGridView(SimpleAdapter simpleAdapter, AdapterView.OnItemClickListener onItemClickListener) {
        GridView gv = new GridView(MainGridActivity.this);
        gv.setColumnWidth((int) (getResources().getDimension(android.R.dimen.app_icon_size) * 1.5));
        gv.setAdapter(simpleAdapter);
        gv.setPadding(5, 5, 5, 5);
        gv.setNumColumns(GridView.AUTO_FIT);
        gv.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        gv.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
        gv.setFastScrollEnabled(true);
        gv.setOnItemClickListener(onItemClickListener);
        return gv;
    }
}
