package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class BackupImportChooserActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getActionBar());
        setContentView(R.layout.bica_main);

        onCreateInit();
    }

    private void onCreateInit() {

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        final ListView mainListView = findViewById(R.id.bica_main_listView);
        final ArrayList<HashMap<String, String>> titleAndSpKeyArrayList = new ArrayList<>();

        intent.getParcelableExtra("jsonObject");

        if (titleAndSpKeyArrayList.size() == 0) {
            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put("title", getString(R.string.failed));
            keyValuePair.put("spKey", "Failed!");
            titleAndSpKeyArrayList.add(keyValuePair);
        }

        final SimpleAdapter adapter =
                new SimpleAdapter(
                        this,
                        titleAndSpKeyArrayList,
                        R.layout.bica_list_item,
                        new String[]{"title"},
                        new int[]{R.id.bica_list_item_switch});

        mainListView.setAdapter(adapter);

    }
}
