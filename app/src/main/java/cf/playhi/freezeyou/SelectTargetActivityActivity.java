package cf.playhi.freezeyou;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ApplicationIconUtils;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;

public class SelectTargetActivityActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getSupportActionBar());
        setContentView(R.layout.staa_main);
        init();
    }

    private void init() {
        final ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        } else {
            final String pkgName = intent.getStringExtra("pkgName");
            if (pkgName == null) {
                finish();
            } else {
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("Img",
                        getApplicationIcon(
                                this,
                                pkgName,
                                getApplicationInfoFromPkgName(pkgName, this),
                                false));
                hm.put("Name", getString(R.string.launch));
                hm.put("Label",
                        getApplicationLabel(
                                this, getPackageManager(),
                                getApplicationInfoFromPkgName(pkgName, this), pkgName)
                );
                arrayList.add(hm);

                HashMap<String, Object> hm2 = new HashMap<>();
                hm2.put("Img",
                        getApplicationIcon(
                                this,
                                pkgName,
                                getApplicationInfoFromPkgName(pkgName, this),
                                false));
                hm2.put("Name", getString(R.string.onlyUnfreeze));
                hm2.put("Label",
                        getApplicationLabel(
                                this, getPackageManager(),
                                getApplicationInfoFromPkgName(pkgName, this), pkgName)
                );
                arrayList.add(hm2);

                try {
                    PackageManager pm = getPackageManager();
                    ActivityInfo[] activityInfos = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES).activities;
                    if (activityInfos != null) {
                        for (ActivityInfo activityInfo : activityInfos) {
                            String ais = activityInfo.name;
                            if (ais != null && activityInfo.exported) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("Img", activityInfo.loadIcon(pm));
                                hashMap.put("Name", ais);
                                hashMap.put("Label", activityInfo.loadLabel(pm).toString());
                                arrayList.add(hashMap);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final SimpleAdapter adapter =
                        new SimpleAdapter(
                                SelectTargetActivityActivity.this,
                                arrayList,
                                R.layout.staa_main_item,
                                new String[]{"Img", "Label", "Name"},
                                new int[]{
                                        R.id.staa_main_item_imageView,
                                        R.id.staa_main_item_textView,
                                        R.id.staa_main_item_subtitle_textView
                                });

                adapter.setViewBinder((view, data, textRepresentation) -> {
                    if (view instanceof ImageView && data instanceof Drawable) {
                        ((ImageView) view).setImageDrawable((Drawable) data);
                        return true;
                    } else {
                        return false;
                    }
                });

                ListView staaMainListView = findViewById(R.id.staa_main_listView);

                staaMainListView.setAdapter(adapter);

                staaMainListView.setOnItemClickListener((parent, view, position, id) -> {
                    String name = (String) arrayList.get(position).get("Name");
                    String label = (String) arrayList.get(position).get("Label");
                    Drawable drawable = (Drawable) arrayList.get(position).get("Img");
                    Bitmap icon = drawable == null ?
                            null : ApplicationIconUtils.getBitmapFromDrawable(drawable);
                    setResult(
                            RESULT_OK,
                            new Intent()
                                    .putExtra("name", name)
                                    .putExtra("icon", icon)
                                    .putExtra("label", label)
                                    .putExtra("id", "FreezeYou!" + pkgName + " " + name));
                    finish();
                });
            }
        }

    }
}
