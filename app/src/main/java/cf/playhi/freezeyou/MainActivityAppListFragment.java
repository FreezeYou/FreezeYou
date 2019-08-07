package cf.playhi.freezeyou;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

public class MainActivityAppListFragment extends Fragment {

    private boolean mUseGridMode = false;

    private AdapterView.OnItemClickListener mOnItemClickListener = null;
    private AbsListView.OnItemLongClickListener mOnItemLongClickListener = null;
    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = null;

    private ListAdapter mAppListAdapter = null;

    private GridView mAppListGridView = null;
    private ListView mAppListListView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (mUseGridMode) {
            view = inflater.inflate(R.layout.main_app_grid_fragment, container, false);
            mAppListGridView = view.findViewById(R.id.main_appList_gridView);

            if (mOnItemClickListener != null)
                mAppListGridView.setOnItemClickListener(mOnItemClickListener);
            if (mOnItemLongClickListener != null)
                mAppListGridView.setOnItemLongClickListener(mOnItemLongClickListener);
            if (mMultiChoiceModeListener != null)
                mAppListGridView.setMultiChoiceModeListener(mMultiChoiceModeListener);
            if (mAppListAdapter != null)
                mAppListGridView.setAdapter(mAppListAdapter);

            mAppListGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            mAppListGridView.setColumnWidth((int) (getResources().getDimension(android.R.dimen.app_icon_size) * 1.6));

        } else {
            view = inflater.inflate(R.layout.main_app_list_fragment, container, false);
            mAppListListView = view.findViewById(R.id.main_appList_listView);

            if (mOnItemClickListener != null)
                mAppListListView.setOnItemClickListener(mOnItemClickListener);
            if (mOnItemLongClickListener != null)
                mAppListListView.setOnItemLongClickListener(mOnItemLongClickListener);
            if (mMultiChoiceModeListener != null)
                mAppListListView.setMultiChoiceModeListener(mMultiChoiceModeListener);
            if (mAppListAdapter != null)
                mAppListListView.setAdapter(mAppListAdapter);

        }
        return view;
    }

    public void setUseGridMode(boolean b) {
        mUseGridMode = b;
    }

    public void setOnAppListItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView.setOnItemClickListener(mOnItemClickListener);
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView.setOnItemClickListener(mOnItemClickListener);
            }
        }
    }

    public void setOnAppListItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView.setOnItemLongClickListener(mOnItemLongClickListener);
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView.setOnItemLongClickListener(mOnItemLongClickListener);
            }
        }
    }

    public void setMultiChoiceModeListener(AbsListView.MultiChoiceModeListener listener) {
        mMultiChoiceModeListener = listener;
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView.setMultiChoiceModeListener(mMultiChoiceModeListener);
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView.setMultiChoiceModeListener(mMultiChoiceModeListener);
            }
        }
    }

    public MainAppListSimpleAdapter setAppListAdapter(Context context, ArrayList<Map<String, Object>> appList, ArrayList<String> selectedPackages) {
        if (mAppListAdapter instanceof MainAppListSimpleAdapter) {
            ((MainAppListSimpleAdapter) mAppListAdapter).replaceAllInFormerArrayList(appList);
        } else {
            mAppListAdapter = new MainAppListSimpleAdapter(
                    context,
                    appList,
                    selectedPackages,
                    mUseGridMode ?
                            R.layout.shortcut_launcher_folder_item :
                            R.layout.app_list_1,
                    new String[]{"Img", "Name", "PackageName", "isFrozen"},
                    mUseGridMode ?
                            new int[]{R.id.slfi_imageView, R.id.slfi_textView} :
                            new int[]{R.id.img, R.id.name, R.id.pkgName, R.id.isFrozen});
        }

        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView.setAdapter(mAppListAdapter);
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView.setAdapter(mAppListAdapter);
            }
        }

        return (MainAppListSimpleAdapter) mAppListAdapter;
    }

    public ListAdapter getAppListAdapter() {
        return mAppListAdapter;
    }

    public void setItemChecked(int position, boolean value) {
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView.setItemChecked(position, value);
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView.setItemChecked(position, value);
            }
        }
    }

}
