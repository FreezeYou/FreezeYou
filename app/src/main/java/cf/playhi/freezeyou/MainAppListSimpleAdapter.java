package cf.playhi.freezeyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

class MainAppListSimpleAdapter extends SimpleAdapter {

    private final ArrayList<Map<String, Object>> mAppList;
    private final ArrayList<String> mIsCheckedPackageList;

    MainAppListSimpleAdapter(Context context, ArrayList<Map<String, Object>> list, ArrayList<String> isCheckedPackageList, int resource, String[] from, int[] to) {
        super(context, list, resource, from, to);
        mAppList = list;
        mIsCheckedPackageList = isCheckedPackageList;

        setViewBinder(new MainAppListSimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView) {
                    if (data instanceof Drawable) {
                        ((ImageView) view).setImageDrawable((Drawable) data);
                        return true;
                    } else if (data instanceof Bitmap) {
                        ((ImageView) view).setImageBitmap((Bitmap) data);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        if (mIsCheckedPackageList.contains((String) mAppList.get(position).get("PackageName"))) {
            view.setBackgroundResource(R.color.translucentGreyBackground);
        } else {
            view.setBackgroundResource(0);
        }

        return view;
    }

}
