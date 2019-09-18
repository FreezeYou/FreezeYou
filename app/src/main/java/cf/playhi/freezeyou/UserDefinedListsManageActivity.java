package cf.playhi.freezeyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ClipboardUtils;
import cf.playhi.freezeyou.utils.ToastUtils;

public class UserDefinedListsManageActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udlma_main);
        ThemeUtils.processActionBar(getActionBar());

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
                String title =
                        new String(
                                Base64.decode(
                                        cursor.getString(
                                                cursor.getColumnIndex("label")
                                        ), Base64.DEFAULT
                                )
                        );
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

    private void displayUserDefinedLists(final ArrayList<HashMap<String, Object>> listsDataArrayList) {
        ProgressBar progressBar = findViewById(R.id.udlmam_progressBar);
        ListView listView = findViewById(R.id.udlmam_listView);
        final SimpleAdapter simpleAdapter =
                new SimpleAdapter(
                        this,
                        listsDataArrayList,
                        R.layout.udlma_list_item,
                        new String[]{"title", "packages"},
                        new int[]{R.id.udlmali_title_textView, R.id.udlmali_subTitle_textView}
                );
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Object itemDataHashMap = simpleAdapter.getItem(position);
                if (itemDataHashMap instanceof HashMap) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserDefinedListsManageActivity.this);
                    String title = ((String) ((HashMap) itemDataHashMap).get("title"));
                    if (title != null) {
                        final String titleBase64 = Base64.encodeToString(title.getBytes(), Base64.DEFAULT);
                        builder.setTitle(title);
                        builder.setMessage(
                                ((HashMap) itemDataHashMap).get("packages")
                                        + File.separator
                                        + titleBase64
                        );
                        builder.setPositiveButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ClipboardUtils.copyToClipboard(getApplicationContext(), titleBase64)) {
                                    ToastUtils.showToast(getApplicationContext(), R.string.success);
                                } else {
                                    ToastUtils.showToast(getApplicationContext(), R.string.failed);
                                }
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, null);
                        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserDefinedListById((int) ((HashMap) itemDataHashMap).get("id"));
                            }
                        });
                        builder.show();
                    }
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void deleteUserDefinedListById(int id) {
        SQLiteDatabase userDefinedListsDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
        userDefinedListsDb.execSQL(
                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        );
        userDefinedListsDb.delete("categories", "_id = " + id, null);
        userDefinedListsDb.close();

        loadUserDefinedLists();
    }
}
