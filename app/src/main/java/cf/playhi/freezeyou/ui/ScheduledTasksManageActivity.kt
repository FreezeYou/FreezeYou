package cf.playhi.freezeyou.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.AlertDialogUtils
import cf.playhi.freezeyou.utils.TasksUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme

open class ScheduledTasksManageActivity : FreezeYouBaseActivity() {
    private data class TaskEntry(
        val id: Int,
        val label: String,
        val description: String,
        val isTimeTask: Boolean,
        val enabled: Boolean
    )

    private var tasks by mutableStateOf<List<TaskEntry>>(emptyList())
    private val selectedTaskIds = mutableStateListOf<String>()
    private var showAddChoices by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        if (Intent.ACTION_CREATE_SHORTCUT == intent.action) {
            createLegacyShortcut()
            return
        }
        updateTasksList()
        setContent { FreezeYouTheme { TasksScreen() } }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showAddChoices = false
        if ((requestCode == TIME_TASK_REQUEST || requestCode == TRIGGER_TASK_REQUEST) &&
            resultCode == RESULT_OK
        ) {
            updateTasksList()
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @androidx.compose.runtime.Composable
    private fun TasksScreen() {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                if (selectedTaskIds.isNotEmpty()) SelectionActions()
                LazyColumn(Modifier.fillMaxSize()) {
                    items(tasks, key = { "${it.isTimeTask}-${it.id}" }) { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth().combinedClickable(
                                onClick = {
                                    if (selectedTaskIds.isEmpty()) editTask(task) else toggleSelection(task)
                                },
                                onLongClick = { toggleSelection(task) }
                            ).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(task.label, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                                Text(task.description)
                            }
                            Switch(
                                checked = task.enabled,
                                onCheckedChange = { setTaskEnabled(task, it) }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(25.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(showAddChoices) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallFloatingActionButton(onClick = { addTask(true) }) {
                            Icon(painterResource(R.drawable.ic_add_alarm), stringResource(R.string.time))
                        }
                        SmallFloatingActionButton(onClick = { addTask(false) }) {
                            Icon(painterResource(R.drawable.ic_explore), stringResource(R.string.add))
                        }
                    }
                }
                FloatingActionButton(onClick = { showAddChoices = !showAddChoices }) {
                    Icon(painterResource(R.drawable.ic_action_add), stringResource(R.string.add))
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SelectionActions() {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedTaskIds.size.toString(), Modifier.weight(1f))
            Button(onClick = { selectedTaskIds.clear(); selectedTaskIds.addAll(tasks.map(::taskKey)) }) {
                Text(stringResource(R.string.selectAll))
            }
            Button(onClick = {
                val previous = selectedTaskIds.toSet()
                selectedTaskIds.clear()
                selectedTaskIds.addAll(tasks.map(::taskKey).filterNot(previous::contains))
            }) {
                Text(stringResource(R.string.selectUnselected))
            }
            Button(onClick = ::confirmDeleteSelected) {
                Text(stringResource(R.string.delete))
            }
        }
    }

    private fun createLegacyShortcut() {
        val result = Intent().apply {
            putExtra(
                Intent.EXTRA_SHORTCUT_INTENT,
                Intent(this@ScheduledTasksManageActivity, ScheduledTasksManageActivity::class.java)
            )
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.scheduledTasks))
            @Suppress("DEPRECATION")
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this@ScheduledTasksManageActivity, R.mipmap.ic_launcher_new_round)
            )
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun toggleSelection(task: TaskEntry) {
        val key = taskKey(task)
        if (!selectedTaskIds.remove(key)) selectedTaskIds.add(key)
    }

    private fun confirmDeleteSelected() {
        AlertDialogUtils.buildAlertDialog(this, R.drawable.ic_warning, R.string.askIfDel, R.string.notice)
            .setPositiveButton(R.string.yes) { _, _ ->
                tasks.filter { taskKey(it) in selectedTaskIds }.forEach { task ->
                    val database = openOrCreateDatabase(
                        if (task.isTimeTask) "scheduledTasks" else "scheduledTriggerTasks",
                        MODE_PRIVATE,
                        null
                    )
                    if (task.isTimeTask) TasksUtils.cancelTheTask(this, task.id)
                    database.delete("tasks", "_id = ?", arrayOf(task.id.toString()))
                    database.close()
                }
                selectedTaskIds.clear()
                updateTasksList()
            }
            .setNegativeButton(R.string.no, null)
            .create().show()
    }

    private fun editTask(task: TaskEntry) {
        @Suppress("DEPRECATION")
        startActivityForResult(
            Intent(this, ScheduledTasksAddActivity::class.java)
                .putExtra("label", task.label)
                .putExtra("time", task.isTimeTask)
                .putExtra("id", task.id),
            if (task.isTimeTask) TIME_TASK_REQUEST else TRIGGER_TASK_REQUEST
        )
    }

    private fun addTask(isTimeTask: Boolean) {
        showAddChoices = false
        @Suppress("DEPRECATION")
        startActivityForResult(
            Intent(this, ScheduledTasksAddActivity::class.java)
                .putExtra("label", getString(R.string.add))
                .putExtra("time", isTimeTask),
            if (isTimeTask) TIME_TASK_REQUEST else TRIGGER_TASK_REQUEST
        )
    }

    private fun setTaskEnabled(task: TaskEntry, enabled: Boolean) {
        val database = openOrCreateDatabase(
            if (task.isTimeTask) "scheduledTasks" else "scheduledTriggerTasks",
            MODE_PRIVATE,
            null
        )
        database.execSQL(
            if (task.isTimeTask) TIME_TASK_TABLE else TRIGGER_TASK_TABLE
        )
        database.update(
            "tasks",
            android.content.ContentValues().apply { put("enabled", if (enabled) 1 else 0) },
            "_id = ?",
            arrayOf(task.id.toString())
        )
        database.close()
        if (task.isTimeTask) TasksUtils.checkTimeTasks(this)
        tasks = tasks.map { if (it == task) it.copy(enabled = enabled) else it }
    }

    private fun taskKey(task: TaskEntry): String =
        "${if (task.isTimeTask) 't' else 'g'}:${task.id}"

    private fun updateTasksList() {
        tasks = loadTimeTasks() + loadTriggerTasks()
    }

    private fun loadTimeTasks(): List<TaskEntry> {
        val database = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null)
        database.execSQL(TIME_TASK_TABLE)
        val cursor = database.query("tasks", null, null, null, null, null, null)
        val result = mutableListOf<TaskEntry>()
        while (cursor.moveToNext()) {
            val hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"))
            val minutes = cursor.getInt(cursor.getColumnIndexOrThrow("minutes"))
            result += TaskEntry(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                label = cursor.getString(cursor.getColumnIndexOrThrow("label")),
                description = "%02d:%02d".format(hour, minutes),
                isTimeTask = true,
                enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1
            )
        }
        cursor.close()
        database.close()
        return result
    }

    private fun loadTriggerTasks(): List<TaskEntry> {
        val database = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null)
        database.execSQL(TRIGGER_TASK_TABLE)
        val cursor = database.query("tasks", null, null, null, null, null, null)
        val triggerValues = resources.getStringArray(R.array.triggersValues)
        val triggerLabels = resources.getStringArray(R.array.triggers)
        val result = mutableListOf<TaskEntry>()
        while (cursor.moveToNext()) {
            val trigger = cursor.getString(cursor.getColumnIndexOrThrow("tg"))
            val triggerIndex = triggerValues.indexOf(trigger).let { if (it < 0) 0 else it }
            result += TaskEntry(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                label = cursor.getString(cursor.getColumnIndexOrThrow("label")),
                description = triggerLabels[triggerIndex],
                isTimeTask = false,
                enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1
            )
        }
        cursor.close()
        database.close()
        return result
    }

    private companion object {
        const val TIME_TASK_REQUEST = 1
        const val TRIGGER_TASK_REQUEST = 2
        const val TIME_TASK_TABLE =
            "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        const val TRIGGER_TASK_TABLE =
            "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
    }
}
