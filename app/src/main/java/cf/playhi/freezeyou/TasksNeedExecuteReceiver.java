package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class TasksNeedExecuteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -5);
        int hour = intent.getIntExtra("hour", -1);
        int minute = intent.getIntExtra("minute", -1);
        String task = intent.getStringExtra("task");
        String repeat = intent.getStringExtra("repeat");
        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTasks", Context.MODE_PRIVATE, null);
        if (id != -6) {//-6为延时任务
            if ("0".equals(repeat) && id != -5) {
                db.execSQL("UPDATE tasks SET enabled = 0 WHERE _id = " + Integer.toString(id) + ";");
            } else {
                TasksUtils.publishTask(context, id, hour, minute, repeat, task);
            }
        }
        if (task != null && !"".equals(task)) {
            TasksUtils.runTask(task.toLowerCase(), context, null);//全部转小写
        }
    }
}
