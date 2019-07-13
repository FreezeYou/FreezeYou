package cf.playhi.freezeyou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class BackupImportChooserActivitySwitchSimpleAdapter extends SimpleAdapter {

    private final Context mContext;
    private final ArrayList<HashMap<String, String>> mData;
    private final ArrayList<HashMap<String, String>> needExcludeData = new ArrayList<>();
    private final ArrayList<Integer> isDisabledList = new ArrayList<>();
    private JSONObject mJsonObject = null;

    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    BackupImportChooserActivitySwitchSimpleAdapter(Context context, JSONObject jsonObject, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        mData = data;
        if (jsonObject != null) {
            try {
                mJsonObject = new JSONObject(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bica_list_item, null);
        Switch s = view.findViewById(R.id.bica_list_item_switch);
        s.setText(mData.get(position).get("title"));
        String category = mData.get(position).get("category");
        s.setChecked(!isDisabledList.contains(position));
        if ("Failed!".equals(category)) {
            s.setChecked(true);
            s.setEnabled(false);
        }
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isDisabledList.remove((Integer) position);
                    needExcludeData.remove(mData.get(position));
                } else {
                    if (!needExcludeData.contains(mData.get(position))) {
                        needExcludeData.add(mData.get(position));
                    }
                    if (!isDisabledList.contains(position)) {
                        isDisabledList.add(position);
                    }
                }
            }
        });

        return view;
    }

    JSONObject getFinalList() {
        if (mJsonObject == null) {
            return new JSONObject();
        }

        String spKey, category;
        for (int i = 0; i < needExcludeData.size(); i++) {
            HashMap<String, String> hm = needExcludeData.get(i);
            category = hm.get("category");
            spKey = hm.get("spKey");
            if (category == null) continue;
            if (spKey == null) continue;
            JSONArray array = mJsonObject.optJSONArray(category);
            if (array == null) continue;
            switch (category) {
                case "generalSettings_boolean":
                case "generalSettings_string":
                case "generalSettings_int":
                case "oneKeyList":
                    JSONObject jsonObj = array.optJSONObject(0);
                    if (jsonObj == null) continue;
                    jsonObj.remove(spKey);
                    break;
                case "userTimeScheduledTasks":
                case "userTriggerScheduledTasks":
                    for (int j = 0; j < array.length(); ++j) {
                        JSONObject jsonObject = array.optJSONObject(j);
                        if (jsonObject == null || !spKey.equals(jsonObject.optString("i", "-1")))
                            continue;
                        try {
                            jsonObject.put("doNotImport", true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return mJsonObject;

    }
}
