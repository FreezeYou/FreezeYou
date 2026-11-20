package cf.playhi.freezeyou.utils

import android.app.Activity
import android.content.Context
import android.content.ContentValues
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys
import net.grandcentrix.tray.AppPreferences
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

//导入导出整体结构复杂度高，处理流程复杂度高，待有可用的优化方案时需要进行优化（提示：后续导出时可修改导出时的 version，用以导入时区分方案）
object BackupUtils {
    @JvmStatic
    fun convertSharedPreference(
        appPreferences: AppPreferences, key: String, defValue: String?
    ): String? {
        return appPreferences.getString(key, defValue)
    }

    internal data class RestoreItem(val section: String, val key: String, val title: String, val record: JSONObject) {
        val sectionLabel: Int get() = when (section) {
            TIME, TRIGGER -> R.string.scheduledTasks
            CATEGORY -> R.string.myCustomization
            ONE_KEY -> R.string.app_list_label
            URI_ALLOW, INSTALL_ALLOW -> R.string.allow_list_label
            else -> R.string.moreSettings
        }
    }

    internal data class RestorePlan(val items: List<RestoreItem>, val invalidCount: Int)

    private const val TIME_TABLE = "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
    private const val TRIGGER_TABLE = "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
    private const val CATEGORY_TABLE = "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"

    private const val TIME = "userTimeScheduledTasks"
    private const val TRIGGER = "userTriggerScheduledTasks"
    private const val CATEGORY = "userDefinedCategories"
    private const val URI_ALLOW = "uriAutoAllowPkgs_allows"
    private const val INSTALL_ALLOW = "installPkgs_autoAllowPkgs_allows"
    private const val BOOLEAN = "generalSettings_boolean"
    private const val STRING = "generalSettings_string"
    private const val INTEGER = "generalSettings_int"
    private const val ONE_KEY = "oneKeyList"

    // These settings require explicit user interaction on the receiving device.
    private val excludedBooleans = setOf("firstIconEnabled", "secondIconEnabled", "thirdIconEnabled", "enableAuthentication")
    private val booleanTitles = DefaultSharedPreferenceStorageBooleanKeys.values().associate { it.name to it.titleTextStringId() } +
            DefaultMultiProcessMMKVStorageBooleanKeys.values().associate { it.name to it.titleTextStringId() }
    private val stringTitles = DefaultSharedPreferenceStorageStringKeys.values().associate { it.name to it.titleTextStringId() } +
            DefaultMultiProcessMMKVStorageStringKeys.values().associate { it.name to it.titleTextStringId() }
    private val intTitles = mapOf("onClickFunctionStatus" to R.string.onClickFunctionStatus, "sortMethodStatus" to R.string.sortMethodStatus)
    private val listTitles = mapOf("okff" to R.string.oneKeyFreezeList, "okuf" to R.string.oneKeyUFList, "foq" to R.string.freezeOnceQuitList)
    private val listKeys = mapOf("okff" to R.string.sAutoFreezeApplicationList, "okuf" to R.string.sOneKeyUFApplicationList, "foq" to R.string.sFreezeOnceQuit)

    internal fun readRestorePlan(context: Context, data: JSONObject): RestorePlan {
        val items = mutableListOf<RestoreItem>()
        var invalid = 0
        for (section in data.keys()) {
            if (section !in setOf(BOOLEAN, STRING, INTEGER, ONE_KEY, TIME, TRIGGER, CATEGORY, URI_ALLOW, INSTALL_ALLOW)) continue
            val records = data.optJSONArray(section)
            if (records == null) {
                invalid++
                continue
            }
            val indexed = section == TIME || section == TRIGGER || section == CATEGORY
            val count = if (indexed) records.length() else 1
            for (index in 0 until count) {
                val record = records.optJSONObject(index)
                if (record == null) {
                    invalid++
                    continue
                }
                if (indexed && record.optBoolean("doNotImport")) continue
                when (section) {
                    TIME, TRIGGER, CATEGORY -> {
                        val values = recordValues(section, record)
                        if (values == null) {
                            invalid++
                            continue
                        }
                        val label = values.getAsString("label")
                        val title = if (section == CATEGORY) {
                            try {
                                String(android.util.Base64.decode(label, android.util.Base64.DEFAULT), Charsets.UTF_8)
                            } catch (_: IllegalArgumentException) {
                                invalid++
                                continue
                            }
                        } else label
                        items.add(RestoreItem(section, index.toString(), title, record))
                    }
                    URI_ALLOW, INSTALL_ALLOW -> {
                        if (record.opt("lists") is String) {
                            val title = if (section == URI_ALLOW) R.string.uriAutoAllowList else R.string.ipaAutoAllowList
                            items.add(RestoreItem(section, section, context.getString(title), record))
                        } else invalid++
                    }
                    else -> for (key in record.keys()) {
                        val title = when (section) {
                            BOOLEAN -> booleanTitles[key]?.takeUnless { key in excludedBooleans }
                            STRING -> stringTitles[key]
                            INTEGER -> intTitles[key]
                            else -> listTitles[key]
                        } ?: continue
                        val valid = when (section) {
                            BOOLEAN -> record.opt(key) is Boolean
                            INTEGER -> record.opt(key) is Int
                            else -> record.opt(key) is String
                        }
                        if (valid) items.add(RestoreItem(section, key, context.getString(title), record))
                        else invalid++
                    }
                }
            }
        }
        return RestorePlan(items, invalid)
    }

    private fun recordValues(section: String, record: JSONObject): ContentValues? {
        val strings = when (section) {
            CATEGORY -> listOf("label", "packages")
            TIME -> listOf("label", "task", "repeat")
            else -> listOf("label", "task", "tg", "tgextra")
        }
        val values = ContentValues()
        for (key in strings) {
            val value = record.opt(key) as? String ?: return null
            values.put(key, value)
        }
        if (section != CATEGORY) {
            values.put("enabled", record.opt("enabled") as? Int ?: return null)
            values.put("column1", "")
            values.put("column2", "")
        }
        if (section == TIME) {
            values.put("hour", (record.opt("hour") as? Int)?.takeIf { it in 0..23 } ?: return null)
            values.put("minutes", (record.opt("minutes") as? Int)?.takeIf { it in 0..59 } ?: return null)
        }
        return values
    }

    internal fun restoreSelected(activity: Activity, items: List<RestoreItem>): Int {
        val context = activity.applicationContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val tray = AppPreferences(context)
        var applied = 0
        for ((section, group) in items.groupBy { it.section }) {
            try {
                when (section) {
                    TIME, TRIGGER, CATEGORY -> {
                        applied += restoreRecords(context, section, group)
                        if (section == TIME) TasksUtils.checkTimeTasks(context)
                        if (section == TRIGGER) TasksUtils.checkTriggerTasks(context)
                    }
                    ONE_KEY, URI_ALLOW, INSTALL_ALLOW -> for (item in group) {
                        val success = if (section == ONE_KEY) {
                            tray.put(context.getString(listKeys.getValue(item.key)), item.record.getString(item.key))
                        } else tray.put(section, item.record.getString("lists"))
                        if (success) applied++
                    }
                    else -> {
                        val edit = prefs.edit()
                        for (item in group) {
                            when (section) {
                                BOOLEAN -> edit.putBoolean(item.key, item.record.getBoolean(item.key))
                                STRING -> edit.putString(item.key, item.record.getString(item.key))
                                INTEGER -> edit.putInt(item.key, item.record.getInt(item.key))
                            }
                        }
                        if (edit.commit()) {
                            for (item in group) {
                                try {
                                    SettingsUtils.checkPreferenceData(context, activity, prefs, item.key)
                                    applied++
                                } catch (e: RuntimeException) {
                                    android.util.Log.w("BackupRestore", "Could not apply preference", e)
                                }
                            }
                        }
                    }
                }
            } catch (e: RuntimeException) {
                android.util.Log.w("BackupRestore", "Could not finish restoring $section", e)
            }
        }
        return applied
    }

    private fun restoreRecords(context: Context, section: String, items: List<RestoreItem>): Int {
        val database = when (section) {
            TIME -> "scheduledTasks"
            TRIGGER -> "scheduledTriggerTasks"
            else -> "userDefinedCategories"
        }
        context.openOrCreateDatabase(database, Context.MODE_PRIVATE, null).use { db ->
            val table = if (section == CATEGORY) "categories" else "tasks"
            db.execSQL(when (section) {
                TIME -> TIME_TABLE
                TRIGGER -> TRIGGER_TABLE
                else -> CATEGORY_TABLE
            })
            db.beginTransaction()
            try {
                for (item in items) {
                    val values = requireNotNull(recordValues(section, item.record))
                    if (section == CATEGORY && db.update(table, values, "label = ?", arrayOf(values.getAsString("label"))) > 0) continue
                    db.insertOrThrow(table, null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return items.size
    }

    @JvmStatic
    fun getExportContent(context: Context): String {
        val appPreferences = AppPreferences(context)
        val defSP = PreferenceManager.getDefaultSharedPreferences(context)
        val finalOutputJsonObject = JSONObject()
        try {
            // 标记转出格式版本 开始
            val formatVersionJSONArray = JSONArray()
            val formatVersionJSONObject = JSONObject()
            formatVersionJSONObject.put("version", 1) // 标记备份文件格式版本
            formatVersionJSONObject.put("generateTime", Date().time) // 标记备份文件格式版本
            formatVersionJSONArray.put(formatVersionJSONObject)
            finalOutputJsonObject.put("format_version", formatVersionJSONArray)
            // 标记转出格式版本 结束

            // 通用设置转出（更多设置 中的选项，不转移图标选择相关设置） 开始

            // boolean 开始
            val generalSettingsBooleanJSONArray = JSONArray()
            val generalSettingsBooleanJSONObject = JSONObject()
            for (key in DefaultSharedPreferenceStorageBooleanKeys.values()) {
                generalSettingsBooleanJSONObject.put(key.name, key.getValue(context))
            }
            for (key in DefaultMultiProcessMMKVStorageBooleanKeys.values()) {
                generalSettingsBooleanJSONObject.put(key.name, key.getValue(context))
            }
            generalSettingsBooleanJSONArray.put(generalSettingsBooleanJSONObject)
            finalOutputJsonObject.put("generalSettings_boolean", generalSettingsBooleanJSONArray)
            // boolean 结束

            // String 开始
            val generalSettingsStringJSONArray = JSONArray()
            val generalSettingsStringJSONObject = JSONObject()
            for (key in DefaultSharedPreferenceStorageStringKeys.values()) {
                generalSettingsStringJSONObject.put(key.name, key.getValue(context))
            }
            for (key in DefaultMultiProcessMMKVStorageStringKeys.values()) {
                generalSettingsStringJSONObject.put(key.name, key.getValue(context))
            }
            generalSettingsStringJSONArray.put(generalSettingsStringJSONObject)
            finalOutputJsonObject.put("generalSettings_string", generalSettingsStringJSONArray)
            // String 结束

            // Int 开始
            val generalSettingsIntJSONArray = JSONArray()
            val generalSettingsIntJSONObject = JSONObject()
            generalSettingsIntJSONObject.put(
                "onClickFunctionStatus",
                defSP.getInt("onClickFunctionStatus", 0)
            )
            generalSettingsIntJSONObject.put(
                "sortMethodStatus",
                defSP.getInt("sortMethodStatus", 0)
            )
            generalSettingsIntJSONArray.put(generalSettingsIntJSONObject)
            finalOutputJsonObject.put("generalSettings_int", generalSettingsIntJSONArray)
            // Int 结束

            // 通用设置转出 结束

            // 一键冻结、一键解冻、离开冻结列表 开始
            val oneKeyListJSONArray = JSONArray()
            val oneKeyListJSONObject = JSONObject()
            oneKeyListJSONObject.put(
                "okff",
                appPreferences.getString(context.getString(R.string.sAutoFreezeApplicationList), "")
            )
            oneKeyListJSONObject.put(
                "okuf",
                appPreferences.getString(context.getString(R.string.sOneKeyUFApplicationList), "")
            )
            oneKeyListJSONObject.put(
                "foq",
                appPreferences.getString(context.getString(R.string.sFreezeOnceQuit), "")
            )
            oneKeyListJSONArray.put(oneKeyListJSONObject)
            finalOutputJsonObject.put("oneKeyList", oneKeyListJSONArray)
            // 一键冻结、一键解冻、离开冻结列表 结束

            // 安装应用请求、URI 请求白名单 开始
            finalOutputJsonObject.put(
                "uriAutoAllowPkgs_allows",
                generateUriAutoAllowPkgsJSONArray(context)
            )
            finalOutputJsonObject.put(
                "installPkgs_autoAllowPkgs_allows",
                generateInstallPkgsAutoAllowPkgsJSONArray(context)
            )
            // 安装应用请求、URI 请求白名单 结束

            // 计划任务 - 时间 开始
            finalOutputJsonObject.put("userTimeScheduledTasks", generateUserTimeTasksJSONArray(context))
            // 计划任务 - 时间 结束

            // 计划任务 - 触发器 开始
            finalOutputJsonObject.put(
                "userTriggerScheduledTasks",
                generateUserTriggerTasksJSONArray(context)
            )
            // 计划任务 - 触发器 结束

            // 用户自定分类（我的列表） 开始
            finalOutputJsonObject.put(
                "userDefinedCategories",
                generateUserDefinedCategoriesJSONArray(context)
            )
            // 用户自定分类（我的列表） 结束
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return finalOutputJsonObject.toString()
    }

    @Throws(JSONException::class)
    private fun generateUserTimeTasksJSONArray(context: Context): JSONArray {
        val userTimeScheduledTasksJSONArray = JSONArray()
        val db = context.openOrCreateDatabase("scheduledTasks", Context.MODE_PRIVATE, null)
        db.execSQL(
            TIME_TABLE
        )
        val cursor = db.query(
            "tasks", null, null, null,
            null, null, null
        )
        if (cursor.moveToFirst()) {
            var ifContinue = true
            while (ifContinue) {
                val oneUserTimeScheduledTaskJSONObject = JSONObject()
                oneUserTimeScheduledTaskJSONObject.put(
                    "hour",
                    cursor.getInt(cursor.getColumnIndexOrThrow("hour"))
                )
                oneUserTimeScheduledTaskJSONObject.put(
                    "minutes",
                    cursor.getInt(cursor.getColumnIndexOrThrow("minutes"))
                )
                oneUserTimeScheduledTaskJSONObject.put(
                    "enabled",
                    cursor.getInt(cursor.getColumnIndexOrThrow("enabled"))
                )
                oneUserTimeScheduledTaskJSONObject.put(
                    "label",
                    cursor.getString(cursor.getColumnIndexOrThrow("label"))
                )
                oneUserTimeScheduledTaskJSONObject.put(
                    "task",
                    cursor.getString(cursor.getColumnIndexOrThrow("task"))
                )
                oneUserTimeScheduledTaskJSONObject.put(
                    "repeat",
                    cursor.getString(cursor.getColumnIndexOrThrow("repeat"))
                )
                userTimeScheduledTasksJSONArray.put(oneUserTimeScheduledTaskJSONObject)
                ifContinue = cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return userTimeScheduledTasksJSONArray
    }

    @Throws(JSONException::class)
    private fun generateUserTriggerTasksJSONArray(context: Context): JSONArray {
        val userTriggerScheduledTasksJSONArray = JSONArray()
        val db = context.openOrCreateDatabase("scheduledTriggerTasks", Context.MODE_PRIVATE, null)
        db.execSQL(
            TRIGGER_TABLE
        )
        val cursor = db.query(
            "tasks", null, null, null,
            null, null, null
        )
        if (cursor.moveToFirst()) {
            var ifContinue = true
            while (ifContinue) {
                val oneUserTriggerScheduledTaskJSONObject = JSONObject()
                oneUserTriggerScheduledTaskJSONObject.put(
                    "tgextra",
                    cursor.getString(cursor.getColumnIndexOrThrow("tgextra"))
                )
                oneUserTriggerScheduledTaskJSONObject.put(
                    "enabled",
                    cursor.getInt(cursor.getColumnIndexOrThrow("enabled"))
                )
                oneUserTriggerScheduledTaskJSONObject.put(
                    "label",
                    cursor.getString(cursor.getColumnIndexOrThrow("label"))
                )
                oneUserTriggerScheduledTaskJSONObject.put(
                    "task",
                    cursor.getString(cursor.getColumnIndexOrThrow("task"))
                )
                oneUserTriggerScheduledTaskJSONObject.put(
                    "tg",
                    cursor.getString(cursor.getColumnIndexOrThrow("tg"))
                )
                userTriggerScheduledTasksJSONArray.put(oneUserTriggerScheduledTaskJSONObject)
                ifContinue = cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return userTriggerScheduledTasksJSONArray
    }

    @Throws(JSONException::class)
    private fun generateUserDefinedCategoriesJSONArray(context: Context): JSONArray {
        val userDefinedCategoriesJSONArray = JSONArray()
        val db = context.openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null)
        db.execSQL(
            CATEGORY_TABLE
        )
        val cursor = db.query(
            "categories", arrayOf("label", "packages"),
            null, null, null,
            null, null
        )
        if (cursor.moveToFirst()) {
            var ifContinue = true
            while (ifContinue) {
                val oneUserDefinedCategoriesJSONObject = JSONObject()
                oneUserDefinedCategoriesJSONObject.put(
                    "label",
                    cursor.getString(cursor.getColumnIndexOrThrow("label"))
                )
                oneUserDefinedCategoriesJSONObject.put(
                    "packages",
                    cursor.getString(cursor.getColumnIndexOrThrow("packages"))
                )
                userDefinedCategoriesJSONArray.put(oneUserDefinedCategoriesJSONObject)
                ifContinue = cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return userDefinedCategoriesJSONArray
    }

    @Throws(JSONException::class)
    private fun generateUriAutoAllowPkgsJSONArray(context: Context): JSONArray {
        val userDefinedCategoriesJSONArray = JSONArray()
        val oneUserDefinedCategoriesJSONObject = JSONObject()
        val ap = AppPreferences(context)
        oneUserDefinedCategoriesJSONObject.put(
            "lists",
            convertSharedPreference(ap, "uriAutoAllowPkgs_allows", "")
        )
        userDefinedCategoriesJSONArray.put(oneUserDefinedCategoriesJSONObject)
        return userDefinedCategoriesJSONArray
    }

    @Throws(JSONException::class)
    private fun generateInstallPkgsAutoAllowPkgsJSONArray(context: Context): JSONArray {
        val userDefinedCategoriesJSONArray = JSONArray()
        val oneUserDefinedCategoriesJSONObject = JSONObject()
        val ap = AppPreferences(context)
        oneUserDefinedCategoriesJSONObject.put(
            "lists",
            convertSharedPreference(ap, "installPkgs_autoAllowPkgs_allows", "")
        )
        userDefinedCategoriesJSONArray.put(oneUserDefinedCategoriesJSONObject)
        return userDefinedCategoriesJSONArray
    }
}
