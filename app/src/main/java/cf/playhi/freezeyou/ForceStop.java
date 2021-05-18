package cf.playhi.freezeyou;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ServiceUtils;

import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class ForceStop extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pkgName = getIntent().getStringExtra("pkgName");

        if (pkgName == null || pkgName.equals("")) {
            finish();
            return;
        }

        String[] packages = null;

        if (pkgName.startsWith("FORCESTOPCATEGORY")) {
            String categoryLabel = pkgName.substring("FORCESTOPCATEGORY".length());

            SQLiteDatabase userDefinedDb = openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null);
            userDefinedDb.execSQL(
                    "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
            );
            Cursor cursor =
                    userDefinedDb.query(
                            "categories",
                            new String[]{"packages"},
                            "label = '" + categoryLabel + "'",
                            null, null, null, null
                    );
            if (cursor.moveToFirst()) {
                packages = cursor.getString(cursor.getColumnIndex("packages")).split(",");
            } else {
                showToast(this, R.string.failed);
            }
            cursor.close();
            userDefinedDb.close();
        } else {
            packages = new String[] {pkgName};
        }

        ServiceUtils.startService(
                this,
                new Intent(getApplicationContext(), ForceStopService.class)
                        .putExtra("packages", packages)
        );
        finish();
    }
}
