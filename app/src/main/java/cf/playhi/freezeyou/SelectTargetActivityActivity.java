package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

public class SelectTargetActivityActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());
        init();
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

    private void init() {
        final ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();

        ActivityInfo[] activityInfos = (ActivityInfo[]) getIntent().getParcelableArrayExtra("acInfoS");
        if (activityInfos != null) {
            String ais;
            for (ActivityInfo activityInfo : activityInfos) {
                ais = activityInfo.name;
                if (ais != null && activityInfo.exported) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("Img", activityInfo.icon);
                    hashMap.put("Name", ais);
                    arrayList.add(hashMap);
                }
            }
        }

        final SimpleAdapter adapter =
                new SimpleAdapter(
                        SelectTargetActivityActivity.this,
                        arrayList,
                        R.layout.staa_main_item,
                        new String[]{"Img", "Name"},
                        new int[]{R.id.staa_main_item_imageView, R.id.staa_main_item_textView});

        ListView staa_main_linearLayout = findViewById(R.id.staa_main_linearLayout);

        staa_main_linearLayout.setAdapter(adapter);

        staa_main_linearLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setResult(
                        RESULT_OK,
                        new Intent()
                                .putExtra("name", (String) arrayList.get(position).get("Name")));
                finish();
            }
        });
    }
}
