package cf.playhi.freezeyou.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Map;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.utils.TasksUtils;

public class ScheduledTasksManageSimpleAdapter extends SimpleAdapter {

    private final Context mContext;
    private final ArrayList<Map<String, Object>> mTasksList;
    private final ArrayList<Integer> mIdIndexArrayList;

    public ScheduledTasksManageSimpleAdapter(
            Context context, ArrayList<Map<String, Object>> list,
            ArrayList<Integer> idIndexArrayList, int resource,
            String[] from, int[] to) {
        super(context, list, resource, from, to);
        mTasksList = list;
        mContext = context;
        mIdIndexArrayList = idIndexArrayList;
    }

    public boolean replaceAllInFormerArrayList(ArrayList<Map<String, Object>> list) {
        mTasksList.clear();
        boolean b = mTasksList.addAll(list);
        notifyDataSetChanged();
        return b;
    }

    public ArrayList<Map<String, Object>> getStoredArrayList() {
        return mTasksList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        SwitchMaterial sc = view.findViewById(R.id.stma_switch);
        sc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final String timeS = (String) mTasksList.get(position).get("time");
            mTasksList.get(position).put("enabled", isChecked);
            SQLiteDatabase db =
                    mContext.openOrCreateDatabase(
                            (timeS != null && timeS.contains(":")) ?
                                    "scheduledTasks" : "scheduledTriggerTasks",
                            Context.MODE_PRIVATE, null);
            db.execSQL(
                    "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
            );
            db.execSQL(
                    "UPDATE tasks SET enabled = " + (isChecked ? 1 : 0) +
                            " WHERE _id = " + mIdIndexArrayList.get(position) + ";"
            );
            db.close();
            if (timeS != null && timeS.contains(":")) {
                TasksUtils.checkTimeTasks(mContext);
            }
            notifyDataSetChanged();
        });

        return view;
    }
}
