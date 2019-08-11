package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

public final class DataStatisticsUtils {

    public static void addFreezeTimes(Context context, String pkgNameString) {
        SQLiteDatabase db = context.openOrCreateDatabase("ApplicationsFreezeTimes", Context.MODE_PRIVATE, null);
        addTimes(db, pkgNameString);
        db.close();
    }

    public static void addUFreezeTimes(Context context, String pkgNameString) {
        SQLiteDatabase db = context.openOrCreateDatabase("ApplicationsUFreezeTimes", Context.MODE_PRIVATE, null);
        addTimes(db, pkgNameString);
        db.close();
    }

    public static void addUseTimes(Context context, String pkgNameString) {
        SQLiteDatabase db = context.openOrCreateDatabase("ApplicationsUseTimes", Context.MODE_PRIVATE, null);
        addTimes(db, pkgNameString);
        db.close();
    }

    private static void addTimes(SQLiteDatabase db, String pkgNameString) {

        if (db == null) {
            return;
        }

        db.execSQL(
                "create table if not exists TimesList(_id integer primary key autoincrement,pkg varchar,times int)"
        );
        Cursor cursor =
                db.query("TimesList", new String[]{"pkg", "times"}, "pkg = '"
                        + Base64.encodeToString(pkgNameString.getBytes(), Base64.DEFAULT)
                        + "'", null, null, null, null);

        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {
            db.execSQL("UPDATE TimesList SET times = '"
                    + (Integer.parseInt(cursor.getString(cursor.getColumnIndex("times"))) + 1)
                    + "' WHERE pkg = '" + Base64.encodeToString(pkgNameString.getBytes(), Base64.DEFAULT) + "';");
        } else {
            db.execSQL("insert into TimesList(pkg,times) values('"
                    + Base64.encodeToString(pkgNameString.getBytes(), Base64.DEFAULT)
                    + "','0');");
        }
        cursor.close();

    }

    public static void resetTimes(Context context, String dbName) {
        SQLiteDatabase db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);

        if (db == null) {
            return;
        }

        db.execSQL(
                "create table if not exists TimesList(_id integer primary key autoincrement,pkg varchar,times int)"
        );

        db.execSQL("UPDATE TimesList SET times = '0';");

        db.close();
    }
}
