package cf.playhi.freezeyou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class ScheduledTasksManageSimpleAdapter extends SimpleAdapter {

    private final Context mContext;
    private final ArrayList<Map<String, Object>> mTasksList;
    private final ArrayList<Integer> mIdIndexArrayList;

    ScheduledTasksManageSimpleAdapter(
            Context context, ArrayList<Map<String, Object>> list,
            ArrayList<Integer> idIndexArrayList, int resource,
            String[] from, int[] to) {
        super(context, list, resource, from, to);
        mTasksList = list;
        mContext = context;
        mIdIndexArrayList = idIndexArrayList;
    }

    boolean replaceAllInFormerArrayList(ArrayList<Map<String, Object>> list) {
        mTasksList.clear();
        boolean b = mTasksList.addAll(list);
        notifyDataSetChanged();
        return b;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.stma_item, null);
        } else {
            view = convertView;
        }

        TextView label = view.findViewById(R.id.stma_label);
        TextView time = view.findViewById(R.id.stma_time);
        Switch sc = view.findViewById(R.id.stma_switch);

        label.setText((String) mTasksList.get(position).get("label"));
        final String timeS = (String) mTasksList.get(position).get("time");
        time.setText(timeS);
        sc.setChecked((boolean) mTasksList.get(position).get("enabled"));

        sc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                if (timeS != null && timeS.contains(":") ) {
                    TasksUtils.checkTimeTasks(mContext);
                }
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
