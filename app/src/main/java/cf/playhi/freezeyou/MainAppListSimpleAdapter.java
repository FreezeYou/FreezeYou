package cf.playhi.freezeyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class MainAppListSimpleAdapter extends SimpleAdapter {

    private final Context mContext;
    private final ArrayList<Map<String, Object>> mAppList;
    private final ArrayList<String> mIsCheckedPackageList;

    MainAppListSimpleAdapter(Context context, ArrayList<Map<String, Object>> list, ArrayList<String> isCheckedPackageList, int resource, String[] from, int[] to) {
        super(context, list, resource, from, to);
        mAppList = list;
        mContext = context;
        mIsCheckedPackageList = isCheckedPackageList;
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
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.app_list_1, null);
        } else {
            view = convertView;
        }

        ImageView imgImageView = view.findViewById(R.id.img);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView pkgNameTextView = view.findViewById(R.id.pkgName);

        Object imgObj = mAppList.get(position).get("Img");
        if (imgObj instanceof Drawable) {
            imgImageView.setImageDrawable((Drawable) imgObj);
        } else if (imgObj instanceof Bitmap) {
            imgImageView.setImageBitmap((Bitmap) imgObj);
        } else if (imgObj instanceof Integer) {
            imgImageView.setImageResource((Integer) imgObj);
        }

        nameTextView.setText((String) mAppList.get(position).get("Name"));
        pkgNameTextView.setText((String) mAppList.get(position).get("PackageName"));

        if (mIsCheckedPackageList.contains((String) mAppList.get(position).get("PackageName"))) {
            view.setBackgroundResource(R.color.translucentGreyBackground);
        } else {
            view.setBackgroundResource(0);
        }

        return view;
    }
}
