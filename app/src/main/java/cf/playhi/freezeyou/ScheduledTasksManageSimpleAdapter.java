package cf.playhi.freezeyou;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

class ScheduledTasksManageSimpleAdapter extends SimpleAdapter {

    private ArrayList<Map<String, Object>> mTasksList;

    ScheduledTasksManageSimpleAdapter(Context context, ArrayList<Map<String, Object>> list, int resource, String[] from, int[] to) {
        super(context, list, resource, from, to);
        mTasksList = list;
    }

    boolean replaceAllInFormerArrayList(ArrayList<Map<String, Object>> list) {
        mTasksList.clear();
        boolean b = mTasksList.addAll(list);
        notifyDataSetChanged();
        return b;
    }
}
