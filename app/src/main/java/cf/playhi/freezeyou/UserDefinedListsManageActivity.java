package cf.playhi.freezeyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                    final String title = ((String) ((HashMap) itemDataHashMap).get("title"));
                    if (title != null) {
                        showListViewOnItemClickPopupMenu(view, title, (HashMap) itemDataHashMap);
                    }
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void showListViewOnItemClickPopupMenu(View view, final String title, final HashMap itemDataHashMap) {
        PopupMenu popup = new PopupMenu(UserDefinedListsManageActivity.this, view);
        popup.inflate(R.menu.udlmna_single_choose_action);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.udlmna_sca_menu_copyId:
                        if (ClipboardUtils.copyToClipboard(getApplicationContext(),
                                Base64.encodeToString(title.getBytes(), Base64.DEFAULT))) {
                            ToastUtils.showToast(getApplicationContext(), R.string.success);
                        } else {
                            ToastUtils.showToast(getApplicationContext(), R.string.failed);
                        }
                        break;
                    case R.id.udlmna_sca_menu_share:
                        try {
                            JSONObject finalOutputJsonObject = new JSONObject();
                            JSONArray userDefinedCategoriesJSONArray = new JSONArray();
                            JSONObject oneUserDefinedCategoriesJSONObject = new JSONObject();
                            oneUserDefinedCategoriesJSONObject.put(
                                    "label",
                                    Base64.encodeToString(title.getBytes(), Base64.DEFAULT)
                            );
                            oneUserDefinedCategoriesJSONObject.put(
                                    "packages",
                                    itemDataHashMap.get("packages")
                            );
                            userDefinedCategoriesJSONArray.put(oneUserDefinedCategoriesJSONObject);
                            finalOutputJsonObject.put("userDefinedCategories", userDefinedCategoriesJSONArray);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,
                                    GZipUtils.gzipCompress(finalOutputJsonObject.toString()));
                            shareIntent = Intent.createChooser(shareIntent, getString(R.string.share));
                            startActivity(shareIntent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtils.showToast(UserDefinedListsManageActivity.this, R.string.failed);
                        }
                        break;
                    case R.id.udlmna_sca_menu_delete:
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(UserDefinedListsManageActivity.this);
                        builder.setTitle(R.string.plsConfirm);
                        builder.setMessage(R.string.askIfDel);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserDefinedListById((int) itemDataHashMap.get("id"));
                            }
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
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
