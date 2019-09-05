package cf.playhi.freezeyou;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

public class UserDefinedListsManageActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udlma_main);

        loadUserDefinedLists();
    }

    private void loadUserDefinedLists() {
        ArrayList<HashMap<String, Object>> listsDataArrayList = new ArrayList<>();
        SQLiteDatabase userDefinedListsDb =
                openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
        userDefinedListsDb.execSQL(
                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        );
        Cursor cursor = userDefinedListsDb.query("categories", new String[]{"label", "_id", "packages"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                HashMap<String, Object> hm = new HashMap<>();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("label"));
                String packages = cursor.getString(cursor.getColumnIndex("packages"));
                hm.put("id", id);
                hm.put("title", title);
                hm.put("packages", packages);
                listsDataArrayList.add(hm);
                cursor.moveToNext();
            }
        }
        cursor.close();
        userDefinedListsDb.close();
        displayUserDefinedLists(listsDataArrayList);
    }

    private void displayUserDefinedLists(ArrayList<HashMap<String, Object>> listsDataArrayList) {

    }

}
