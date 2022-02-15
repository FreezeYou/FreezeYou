package cf.playhi.freezeyou.adapter;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

public final class ReplaceableSimpleAdapter extends SimpleAdapter {

    private final ArrayList<Map<String, Object>> mAppList;

    public ReplaceableSimpleAdapter(Context context, ArrayList<Map<String, Object>> list, int resource, String[] from, int[] to) {
        super(context, list, resource, from, to);
        mAppList = list;
    }

    public boolean replaceAllInFormerArrayList(ArrayList<Map<String, Object>> list) {
        mAppList.clear();
        boolean b = mAppList.addAll(list);
        notifyDataSetChanged();
        return b;
    }

    public ArrayList<Map<String, Object>> getStoredArrayList() {
        return mAppList;
    }

}
