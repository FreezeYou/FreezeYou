package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.TriggerTasksService
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.fragment.STAAFragment
import cf.playhi.freezeyou.ui.fragment.STAATriggerFragment
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.*
import cf.playhi.freezeyou.utils.AccessibilityUtils.isAccessibilitySettingsOn
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings
import cf.playhi.freezeyou.utils.TasksUtils.publishTask
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ScheduledTasksAddActivity : FreezeYouBaseActivity(), OnSharedPreferenceChangeListener {
    private var isTimeTask = false
    private var isEdited = false
    private var id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        id = intent.getIntExtra("id", -5)
        isTimeTask = intent.getBooleanExtra("time", true)
        val actionBar = supportActionBar
        processActionBar(actionBar)
        if (actionBar != null) {
            actionBar.title = intent.getStringExtra("label")
        }
        init()
        setContent {
            FreezeYouTheme {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            FragmentContainerView(context).apply {
                                this.id = R.id.scheduled_task_preferences_container
                                if (supportFragmentManager.findFragmentById(this.id) == null) {
                                    supportFragmentManager.beginTransaction()
                                        .replace(
                                            this.id,
                                            if (isTimeTask) STAAFragment() else STAATriggerFragment()
                                        )
                                        .commitNow()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    FloatingActionButton(
                        onClick = ::saveTask,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(25.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_save), stringResource(R.string.save))
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.staa_menu, menu)
        val cTheme = ThemeUtils.getUiTheme(this@ScheduledTasksAddActivity)
        if ("white" == cTheme || "default" == cTheme) {
            menu.findItem(R.id.menu_staa_delete).setIcon(R.drawable.ic_action_delete_light)
            menu.findItem(R.id.menu_staa_share).setIcon(R.drawable.ic_action_share_light)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                checkAndDecideIfFinish()
                true
            }
            R.id.menu_staa_delete -> {
                AlertDialogUtils
                    .buildAlertDialog(
                        this, R.drawable.ic_warning, R.string.askIfDel, R.string.notice
                    )
                    .setPositiveButton(R.string.yes) { _, _ ->
                        setResult(RESULT_OK)
                        if (id != -5) {
                            val db = openOrCreateDatabase(
                                if (isTimeTask) "scheduledTasks" else "scheduledTriggerTasks",
                                MODE_PRIVATE,
                                null
                            )
                            if (isTimeTask) {
                                TasksUtils.cancelTheTask(this@ScheduledTasksAddActivity, id)
                            }
                            db.execSQL("DELETE FROM tasks WHERE _id = $id")
                            db.close()
                        }
                        finish()
                    }
                    .setNegativeButton(R.string.no, null)
                    .create().show()
                true
            }
            R.id.menu_staa_share -> {
                val defSP = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val finalOutputJsonObject = JSONObject()
                val userScheduledTasksJSONArray = JSONArray()
                val oneUserScheduledTaskJSONObject = JSONObject()
                try {
                    if (isTimeTask) {
                        val hm: MutableMap<String, Any?> = HashMap()
                        if (!prepareSaveTimeTaskData(defSP, hm)) {
                            return true
                        }
                        oneUserScheduledTaskJSONObject.put("hour", hm["hour"] as Int)
                        oneUserScheduledTaskJSONObject.put("minutes", hm["minutes"] as Int)
                        oneUserScheduledTaskJSONObject.put("enabled", hm["enabled"] as Int)
                        oneUserScheduledTaskJSONObject.put("label", hm["label"] as String?)
                        oneUserScheduledTaskJSONObject.put("task", hm["task"] as String?)
                        oneUserScheduledTaskJSONObject.put("repeat", hm["repeat"] as String?)
                    } else {
                        oneUserScheduledTaskJSONObject.put(
                            "tgextra",
                            defSP.getString("stma_add_trigger_extra_parameters", "")
                        )
                        oneUserScheduledTaskJSONObject.put(
                            "enabled",
                            if (defSP.getBoolean("stma_add_enable", true)) 1 else 0
                        )
                        oneUserScheduledTaskJSONObject.put(
                            "label",
                            defSP.getString("stma_add_label", getString(R.string.label))
                        )
                        oneUserScheduledTaskJSONObject.put(
                            "task",
                            defSP.getString("stma_add_task", "okuf")
                        )
                        oneUserScheduledTaskJSONObject.put(
                            "tg",
                            defSP.getString("stma_add_trigger", "")
                        )
                    }
                    userScheduledTasksJSONArray.put(oneUserScheduledTaskJSONObject)
                    finalOutputJsonObject.put(
                        if (isTimeTask) "userTimeScheduledTasks" else "userTriggerScheduledTasks",
                        userScheduledTasksJSONArray
                    )
                    var shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        GZipUtils.gzipCompress(finalOutputJsonObject.toString())
                    )
                    shareIntent = Intent.createChooser(shareIntent, getString(R.string.share))
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast(this, R.string.failed)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkAndDecideIfFinish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun init() {
        prepareData(id)
    }

    private fun prepareData(id: Int) {
        if (id != -5) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@ScheduledTasksAddActivity)
            val editor = sharedPreferences.edit()
            if (isTimeTask) {
                val db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null)
                db.execSQL(
                    "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
                )
                val cursor = db.query(
                    "tasks",
                    null,
                    "_id=?",
                    arrayOf(id.toString()),
                    null,
                    null,
                    null
                )
                if (cursor.moveToFirst()) {
                    editor.putString(
                        "stma_add_time",
                        cursor.getInt(cursor.getColumnIndexOrThrow("hour"))
                            .toString() + ":" + cursor.getInt(cursor.getColumnIndexOrThrow("minutes"))
                    )
                    editor.putBoolean(
                        "stma_add_enable",
                        cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1
                    )
                    editor.putString(
                        "stma_add_label",
                        cursor.getString(cursor.getColumnIndexOrThrow("label"))
                    )
                    editor.putString(
                        "stma_add_task",
                        cursor.getString(cursor.getColumnIndexOrThrow("task"))
                    )
                    val hashSet = HashSet<String>()
                    val repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat"))
                    for (i in 0 until repeat.length) {
                        hashSet.add(repeat.substring(i, i + 1))
                    }
                    editor.putStringSet("stma_add_repeat", hashSet)
                }
                cursor.close()
                db.close()
            } else {
                val db = this@ScheduledTasksAddActivity.openOrCreateDatabase(
                    "scheduledTriggerTasks",
                    MODE_PRIVATE,
                    null
                )
                db.execSQL(
                    "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
                )
                val cursor = db.query(
                    "tasks",
                    null,
                    "_id=?",
                    arrayOf(id.toString()),
                    null,
                    null,
                    null
                )
                if (cursor.moveToFirst()) {
                    editor.putString(
                        "stma_add_trigger_extra_parameters",
                        cursor.getString(cursor.getColumnIndexOrThrow("tgextra"))
                    )
                    editor.putBoolean(
                        "stma_add_enable",
                        cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1
                    )
                    editor.putString(
                        "stma_add_label",
                        cursor.getString(cursor.getColumnIndexOrThrow("label"))
                    )
                    editor.putString(
                        "stma_add_task",
                        cursor.getString(cursor.getColumnIndexOrThrow("task"))
                    )
                    editor.putString(
                        "stma_add_trigger",
                        cursor.getString(cursor.getColumnIndexOrThrow("tg"))
                    )
                }
                cursor.close()
                db.close()
            }
            editor.apply()
        }
    }

    private fun saveTask() {
        val defaultSharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (isTimeTask) {
            if (saveTimeTaskData(defaultSharedPreferences, id)) {
                finish()
            }
        } else if (saveTriggerTaskData(defaultSharedPreferences, id)) {
            finish()
        }
    }

    private fun prepareSaveTimeTaskData(
        defaultSharedPreferences: SharedPreferences,
        returnPreparedData: MutableMap<String, Any?>
    ): Boolean {
        var time = defaultSharedPreferences.getString("stma_add_time", "09:09")
        if (time == null) {
            time = "09:09"
        }
        val indexOfColon = time.indexOf(":")
        if (indexOfColon == -1) {
            showToast(this, R.string.mustContainColon)
            return false
        }
        val hour: Int
        val minutes: Int
        try {
            hour = time.substring(0, indexOfColon).toInt()
            minutes = time.substring(indexOfColon + 1).toInt()
        } catch (e: Exception) {
            showToast(
                this,
                getString(R.string.minutesShouldBetween)
                        + System.getProperty("line.separator")
                        + getString(R.string.hourShouldBetween)
            )
            return false
        }
        val enabled = if (defaultSharedPreferences.getBoolean("stma_add_enable", true)) 1 else 0
        val repeatStringBuilder = StringBuilder()
        val stringSet = defaultSharedPreferences.getStringSet("stma_add_repeat", null)
        if (stringSet != null) {
            for (str in stringSet) {
                when (str) {
                    "1", "2", "3", "4", "5", "6", "7" -> repeatStringBuilder.append(str)
                    else -> {}
                }
            }
        }
        val repeat =
            if (repeatStringBuilder.toString() == "") "0" else repeatStringBuilder.toString()
        val label = defaultSharedPreferences.getString("stma_add_label", getString(R.string.label))
        val task = defaultSharedPreferences.getString("stma_add_task", "okuf")
        returnPreparedData["hour"] = hour
        returnPreparedData["minutes"] = minutes
        returnPreparedData["enabled"] = enabled
        returnPreparedData["repeat"] = repeat
        returnPreparedData["label"] = label ?: ""
        returnPreparedData["task"] = task ?: ""
        return true
    }

    private fun saveTimeTaskData(defaultSharedPreferences: SharedPreferences, id: Int): Boolean {
        val returnData: MutableMap<String, Any?> = HashMap()
        if (!prepareSaveTimeTaskData(defaultSharedPreferences, returnData)) {
            return false
        }
        val hour = returnData["hour"] as Int
        val minutes = returnData["minutes"] as Int
        val enabled = returnData["enabled"] as Int
        val repeat = returnData["repeat"] as String
        val label = returnData["label"] as String
        val task = returnData["task"] as String
        val db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null)
        db.execSQL(
            "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        ) //column1\2 留作备用
        if (id == -5) {
            db.execSQL(
                "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                        + hour + ","
                        + minutes + ","
                        + "'" + repeat + "'" + ","
                        + enabled + ","
                        + "'" + label + "'" + ","
                        + "'" + task + "'" + ",'','')"
            )
        } else {
            db.execSQL(
                "UPDATE tasks SET hour = "
                        + hour + ", minutes = "
                        + minutes + ", repeat = "
                        + "'" + repeat + "'" + ", enabled = "
                        + enabled + ", label = '"
                        + label + "', task = '"
                        + task + "' WHERE _id = " + id + ";"
            )
        }
        db.close()
        TasksUtils.cancelTheTask(this@ScheduledTasksAddActivity, id)
        if (enabled == 1) {
            publishTask(this@ScheduledTasksAddActivity, id, hour, minutes, repeat, task)
        }
        setResult(RESULT_OK)
        return true
    }

    private fun saveTriggerTaskData(defaultSharedPreferences: SharedPreferences, id: Int): Boolean {
        val triggerExtraParameters =
            defaultSharedPreferences.getString("stma_add_trigger_extra_parameters", "")
        val enabled = if (defaultSharedPreferences.getBoolean("stma_add_enable", true)) 1 else 0
        val label = defaultSharedPreferences.getString("stma_add_label", getString(R.string.label))
        val task = defaultSharedPreferences.getString("stma_add_task", "okuf")
        val trigger = defaultSharedPreferences.getString("stma_add_trigger", "")
        if ("" == trigger) { //未指定触发器，直接return，抛failed
            showToast(this, R.string.failed)
            return false
        }
        val db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null)
        db.execSQL(
            "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        )
        db.execSQL(
            "replace into tasks(_id,tg,tgextra,enabled,label,task,column1,column2) VALUES ( "
                    + (if (id == -5) null else id) + ",'"
                    + trigger + "','" + triggerExtraParameters + "'," + enabled + ",'" + label + "','" + task + "','','')"
        )
        db.close()
        if (enabled == 1 && trigger != null) {
            when (trigger) {
                "onScreenOn" -> ServiceUtils.startService(
                    this,
                    Intent(this, TriggerTasksService::class.java)
                        .putExtra("OnScreenOn", true)
                )
                "onScreenOff" -> ServiceUtils.startService(
                    this,
                    Intent(this, TriggerTasksService::class.java)
                        .putExtra("OnScreenOff", true)
                )
                "onApplicationsForeground", "onLeaveApplications" -> AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(
                    this
                )
                else -> {}
            }
        }
        setResult(RESULT_OK)
        return true
    }

    private fun checkAndDecideIfFinish() {
        if (!isEdited) {
            finish()
            return
        }
        AlertDialogUtils.buildAlertDialog(
            this,
            R.mipmap.ic_launcher_new_round,
            R.string.askIfSave,
            R.string.notice
        )
            .setPositiveButton(R.string.yes) { _, _ ->
                if (isTimeTask) {
                    if (saveTimeTaskData(
                            PreferenceManager.getDefaultSharedPreferences(
                                applicationContext
                            ), id
                        )
                    ) {
                        finish()
                    }
                } else {
                    if (saveTriggerTaskData(
                            PreferenceManager.getDefaultSharedPreferences(
                                applicationContext
                            ), id
                        )
                    ) {
                        finish()
                    }
                }
            }
            .setNegativeButton(R.string.no) { _, _ -> finish() }
            .setNeutralButton(R.string.cancel, null)
            .create().show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        isEdited = true
        when (key) {
            "stma_add_time" -> {
                val time = sharedPreferences.getString("stma_add_time", "09:09")
                if (time != null) {
                    if (time.contains(":")) {
                        var sHour = time.substring(0, time.indexOf(":"))
                        var sMin = time.substring(time.indexOf(":") + 1)
                        if ("" == sHour) sHour = "0"
                        if ("" == sMin) sMin = "0"
                        val hour: Int
                        val minutes: Int
                        try {
                            hour = sHour.toInt()
                            minutes = sMin.toInt()
                        } catch (e: Exception) {
                            showToast(
                                this,
                                getString(R.string.minutesShouldBetween)
                                        + System.getProperty("line.separator")
                                        + getString(R.string.hourShouldBetween)
                            )
                            return
                        }
                        if (hour < 0 || hour >= 24) {
                            showToast(this, R.string.hourShouldBetween)
                        }
                        if (minutes < 0 || minutes > 59) {
                            showToast(this, R.string.minutesShouldBetween)
                        }
                    } else {
                        showToast(this, R.string.mustContainColon)
                    }
                }
            }
            "stma_add_trigger" -> {
                val stmaAddTrigger = sharedPreferences.getString("stma_add_trigger", "")
                if (("onApplicationsForeground" == stmaAddTrigger
                            || "onLeaveApplications" == stmaAddTrigger)
                    && !isAccessibilitySettingsOn(this)
                ) {
                    showToast(this, R.string.needActiveAccessibilityService)
                    openAccessibilitySettings(this)
                }
            }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}
