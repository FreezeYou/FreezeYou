package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

final class BackupUtils {

    static String convertSharedPreference(
            SharedPreferences sharedPreferences, String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    static String convertSharedPreference(
            AppPreferences appPreferences, String key, String defValue) {
        return appPreferences.getString(key, defValue);
    }

    static boolean convertSharedPreference(
            SharedPreferences sharedPreferences, String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    static boolean convertSharedPreference(
            AppPreferences appPreferences, String key, boolean defValue) {
        return appPreferences.getBoolean(key, defValue);
    }

    private static void importStringSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, AppPreferences ap, String key) {
        sp.edit().putString(key, jsonObject.optString(key, "")).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key, ap);
    }

    private static void importBooleanSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, AppPreferences ap, String key) {
        sp.edit().putBoolean(key, jsonObject.optBoolean(key, false)).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key, ap);
    }

    private static void importIntSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, AppPreferences ap, String key) {
        sp.edit().putInt(key, jsonObject.optInt(key, 0)).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key, ap);
    }

    static boolean importUserTimeTasksJSONArray(Context context, JSONObject jsonObject) {
        JSONArray userTimeScheduledTasksJSONArray =
                jsonObject.optJSONArray("userTimeScheduledTasks");
        if (userTimeScheduledTasksJSONArray == null) {
            return false;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTasks", Context.MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        boolean isCompletelySuccess = true;
        JSONObject oneUserTimeScheduledTaskJSONObject;
        try {
            for (int i = 0; i < userTimeScheduledTasksJSONArray.length(); ++i) {
                oneUserTimeScheduledTaskJSONObject = userTimeScheduledTasksJSONArray.getJSONObject(i);
                db.execSQL(
                        "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                                + oneUserTimeScheduledTaskJSONObject.getInt("hour") + ","
                                + oneUserTimeScheduledTaskJSONObject.getInt("minutes") + ","
                                + oneUserTimeScheduledTaskJSONObject.getString("repeat") + ","
                                + oneUserTimeScheduledTaskJSONObject.getInt("enabled") + ","
                                + "'" + oneUserTimeScheduledTaskJSONObject.getString("label") + "'" + ","
                                + "'" + oneUserTimeScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
                );
            }
        } catch (JSONException e) {
            isCompletelySuccess = false;
        }

        db.close();
        TasksUtils.checkTimeTasks(context);

        return isCompletelySuccess;
    }

    static boolean importUserTriggerTasksJSONArray(Context context, JSONObject jsonObject) {
        JSONArray userTriggerScheduledTasksJSONArray =
                jsonObject.optJSONArray("userTriggerScheduledTasks");
        if (userTriggerScheduledTasksJSONArray == null) {
            return false;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTriggerTasks", Context.MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        JSONObject oneUserTriggerScheduledTaskJSONObject;
        boolean isCompletelySuccess = true;
        try {
            for (int i = 0; i < userTriggerScheduledTasksJSONArray.length(); ++i) {
                oneUserTriggerScheduledTaskJSONObject = userTriggerScheduledTasksJSONArray.getJSONObject(i);
                db.execSQL(
                        "insert into tasks(_id,tg,tgextra,enabled,label,task,column1,column2) VALUES (null,"
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tg") + "'" + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tgextra") + "'" + ","
                                + oneUserTriggerScheduledTaskJSONObject.getInt("enabled") + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("label") + "'" + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
                );
            }
        } catch (JSONException e) {
            isCompletelySuccess = false;
        }

        db.close();
        TasksUtils.checkTriggerTasks(context);

        return isCompletelySuccess;
    }

    static boolean importOneKeyLists(Context context, JSONObject jsonObject, AppPreferences appPreferences) {
        JSONArray array = jsonObject.optJSONArray("oneKeyList");
        if (array == null) {
            return false;
        }
        JSONObject oneKeyListJSONObject = array.optJSONObject(0);
        if (oneKeyListJSONObject == null) {
            return false;
        }

        importOneKeyListsFromProcessedJSONObject(
                context, oneKeyListJSONObject, appPreferences);

        return true;
    }

    static void importOneKeyListsFromProcessedJSONObject(
            Context context, JSONObject oneKeyListJSONObject, AppPreferences appPreferences) {
        Iterator<String> it = oneKeyListJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "okff":
                    appPreferences.put(
                            context.getString(R.string.sAutoFreezeApplicationList),
                            oneKeyListJSONObject.optString("okff"));
                    break;
                case "okuf":
                    appPreferences.put(
                            context.getString(R.string.sOneKeyUFApplicationList),
                            oneKeyListJSONObject.optString("okuf"));
                    break;
                case "foq":
                    appPreferences.put(
                            context.getString(R.string.sFreezeOnceQuit),
                            oneKeyListJSONObject.optString("foq"));
                    break;
                default:
                    break;
            }
        }
    }


    static boolean importIntSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // Int 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_int");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsIntJSONObject = array.optJSONObject(0);
        if (generalSettingsIntJSONObject == null) {
            return false;
        }

        importIntSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsIntJSONObject, defSP, appPreferences);
        // Int 结束
        return true;
    }

    static void importIntSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsIntJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsIntJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "onClickFunctionStatus":
                    importIntSharedPreference(
                            context, activity, generalSettingsIntJSONObject, defSP, appPreferences, s);
                    break;
                default:
                    break;
            }
        }
    }

    static boolean importStringSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // String 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_string");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsStringJSONObject = array.optJSONObject(0);
        if (generalSettingsStringJSONObject == null) {
            return false;
        }
        importStringSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsStringJSONObject, defSP, appPreferences);
        // String 结束
        return true;
    }

    static void importStringSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsStringJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsStringJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "onClickFuncChooseActionStyle":
                case "uiStyleSelection":
                case "launchMode":
                case "organizationName":
                case "shortCutOneKeyFreezeAdditionalOptions":
                    importStringSharedPreference(
                            context, activity, generalSettingsStringJSONObject, defSP, appPreferences, s);
                    break;
                default:
                    break;
            }
        }
    }

    static boolean importBooleanSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // boolean 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_boolean");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsBooleanJSONObject = array.optJSONObject(0);
        if (generalSettingsBooleanJSONObject == null) {
            return false;
        }

        importBooleanSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsBooleanJSONObject, defSP, appPreferences);
        // boolean 结束
        return true;
    }

    static void importBooleanSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsBooleanJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsBooleanJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "allowEditWhenCreateShortcut":
                case "noCaution":
                case "saveOnClickFunctionStatus":
                case "saveSortMethodStatus":
                case "cacheApplicationsIcons":
                case "showInRecents":
                case "lesserToast":
                case "notificationBarFreezeImmediately":
                case "notificationBarDisableSlideOut":
                case "notificationBarDisableClickDisappear":
                case "onekeyFreezeWhenLockScreen":
                case "freezeOnceQuit":
                case "avoidFreezeForegroundApplications":
                case "avoidFreezeNotifyingApplications":
                case "openImmediately":
                case "openAndUFImmediately":
                case "shortcutAutoFUF":
                case "needConfirmWhenFreezeUseShortcutAutoFUF":
                case "openImmediatelyAfterUnfreezeUseShortcutAutoFUF":
                case "enableInstallPkgFunc":
                case "tryDelApkAfterInstalled":
                case "useForegroundService":
                case "debugModeEnabled":
                    importBooleanSharedPreference(
                            context, activity, generalSettingsBooleanJSONObject, defSP, appPreferences, s);
                    break;
                default:
                    break;
            }
        }
    }

}
