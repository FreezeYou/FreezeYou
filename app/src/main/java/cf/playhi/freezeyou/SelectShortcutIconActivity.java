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
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;

public class SelectShortcutIconActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());
        setContentView(R.layout.ssia_main);
        init();
    }

    private void init() {
        final ArrayList<HashMap<String, Drawable>> icons = new ArrayList<>();

        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher_new_round));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher_round));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.screenlock));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.ic_notification));

        List<ApplicationInfo> applicationInfoS = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        if (applicationInfoS != null) {
            for (ApplicationInfo applicationInfo : applicationInfoS) {
                if (applicationInfo != null) {
                    addToIconsArrayList(
                            icons,
                            getApplicationIcon(
                                    SelectShortcutIconActivity.this,
                                    applicationInfo.packageName,
                                    applicationInfo,
                                    false));
                }
            }
        }

        //选择更多
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.grid_add));

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, icons,
                R.layout.ssia_main_grid_item, new String[]{"Icon"},
                new int[]{R.id.ssia_mgi_imageButton});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageButton && data instanceof Drawable) {
                    ImageButton imageButton = (ImageButton) view;
                    imageButton.setImageDrawable((Drawable) data);
                    return true;
                } else
                    return false;
            }
        });

        GridView gridView = findViewById(R.id.ssia_main_gridView);

        gridView.setAdapter(simpleAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Drawable drawable = icons.get(position).get("Icon");
                if (getResources().getDrawable(R.drawable.grid_add).equals(drawable)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 21);
                    }
                } else {
                    setResult(
                            RESULT_OK,
                            new Intent()
                                    .putExtra(
                                            "Icon",
                                            getBitmapFromDrawable(drawable)));
                    finish();
                }
            }
        });

    }

    private void addToIconsArrayList(ArrayList<HashMap<String, Drawable>> icons, Drawable drawable) {
        HashMap<String, Drawable> map = new HashMap<>();
        map.put("Icon", drawable);
        icons.add(map);
    }
}
