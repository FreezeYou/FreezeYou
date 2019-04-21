package cf.playhi.freezeyou;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

class MainAppListSimpleAdapter extends SimpleAdapter {

    private ArrayList<Map<String, Object>> mAppList;

    MainAppListSimpleAdapter(Context context, ArrayList<Map<String, Object>> list, int resource, String[] from, int[] to) {
        super(context, list, resource, from, to);
        mAppList = list;
    }
//
//    public void clearArrayListData() {
//        mAppList.clear();
//        notifyDataSetChanged();
//    }
//
//    public boolean addToArrayList(Map<String, Object> map) {
//        boolean b = mAppList.add(map);
//        notifyDataSetChanged();
//        return b;
//    }
//
//    public void addToArrayList(int index, Map<String, Object> map) {
//        mAppList.add(index, map);
//        notifyDataSetChanged();
//    }
//
//    public boolean removeFromArrayList(Map<String, Object> map) {
//        boolean b = mAppList.remove(map);
//        notifyDataSetChanged();
//        return b;
//    }
//
//    public Map<String, Object> removeFromArrayList(int index) {
//        Map<String, Object> m = mAppList.remove(index);
//        notifyDataSetChanged();
//        return m;
//    }

    boolean replaceAllInFormerArrayList(ArrayList<Map<String, Object>> list) {
        mAppList.clear();
        boolean b = mAppList.addAll(list);
        notifyDataSetChanged();
        return b;
    }

    ArrayList<Map<String, Object>> getStoredArrayList() {
        return mAppList;
    }

}
