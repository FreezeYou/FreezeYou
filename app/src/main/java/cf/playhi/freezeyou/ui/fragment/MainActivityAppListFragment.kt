package cf.playhi.freezeyou.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import cf.playhi.freezeyou.adapter.MainAppListSimpleAdapter
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme

class MainActivityAppListFragment : Fragment() {
    private var mUseGridMode = false
    private var mOnItemClickListener: AdapterView.OnItemClickListener? = null
    private var mOnItemLongClickListener: AdapterView.OnItemLongClickListener? = null
    private var mMultiChoiceModeListener: AbsListView.MultiChoiceModeListener? = null
    private var mAppListAdapter: ListAdapter? = null
    private var mAppListGridView: GridView? = null
    private var mAppListListView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUseGridMode = arguments?.getBoolean(ARG_GRID_MODE) ?: mUseGridMode
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = if (mUseGridMode) {
        GridView(requireContext()).apply {
            mAppListGridView = this
            bindListBehavior(this)
            columnWidth = (resources.getDimension(android.R.dimen.app_icon_size) * 1.6).toInt()
            numColumns = GridView.AUTO_FIT
            stretchMode = GridView.STRETCH_SPACING_UNIFORM
            verticalSpacing = (6 * resources.displayMetrics.density).toInt()
            gravity = android.view.Gravity.CENTER
        }
    } else {
        ListView(requireContext()).apply {
            mAppListListView = this
            bindListBehavior(this)
            divider = null
            dividerHeight = 0
        }
    }

    private fun bindListBehavior(view: AbsListView) {
        view.onItemClickListener = mOnItemClickListener
        view.onItemLongClickListener = mOnItemLongClickListener
        mMultiChoiceModeListener?.let(view::setMultiChoiceModeListener)
        view.adapter = mAppListAdapter
        view.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
        view.isFastScrollEnabled = true
    }

    override fun onDestroyView() {
        mAppListGridView = null
        mAppListListView = null
        super.onDestroyView()
    }

    fun setUseGridMode(b: Boolean) {
        mUseGridMode = b
        arguments = (arguments ?: Bundle()).apply { putBoolean(ARG_GRID_MODE, b) }
    }

    fun setOnAppListItemClickListener(listener: AdapterView.OnItemClickListener?) {
        mOnItemClickListener = listener
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView!!.onItemClickListener = mOnItemClickListener
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView!!.onItemClickListener = mOnItemClickListener
            }
        }
    }

    fun setOnAppListItemLongClickListener(listener: AdapterView.OnItemLongClickListener?) {
        mOnItemLongClickListener = listener
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView!!.onItemLongClickListener = mOnItemLongClickListener
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView!!.onItemLongClickListener = mOnItemLongClickListener
            }
        }
    }

    fun setMultiChoiceModeListener(listener: AbsListView.MultiChoiceModeListener?) {
        mMultiChoiceModeListener = listener
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView!!.setMultiChoiceModeListener(mMultiChoiceModeListener)
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView!!.setMultiChoiceModeListener(mMultiChoiceModeListener)
            }
        }
    }

    fun setAppListAdapter(
        context: Context, appList: ArrayList<MutableMap<String, Any?>>,
        selectedPackages: ArrayList<String>
    ): MainAppListSimpleAdapter? {
        if (mAppListAdapter is MainAppListSimpleAdapter) {
            (mAppListAdapter as MainAppListSimpleAdapter).replaceAllInFormerArrayList(appList)
        } else {
            mAppListAdapter = MainAppListSimpleAdapter(
                context,
                appList,
                selectedPackages,
                mUseGridMode
            )
        }
        val activity = activity
        if (activity != null) {
            if (mUseGridMode) {
                if (mAppListGridView != null) {
                    activity.runOnUiThread { mAppListGridView!!.adapter = mAppListAdapter }
                }
            } else {
                if (mAppListListView != null) {
                    activity.runOnUiThread { mAppListListView!!.adapter = mAppListAdapter }
                }
            }
        }
        return mAppListAdapter as MainAppListSimpleAdapter?
    }

    fun getAppListAdapter(): ListAdapter? {
        return mAppListAdapter
    }

    fun setItemChecked(position: Int, value: Boolean) {
        if (mUseGridMode) {
            if (mAppListGridView != null) {
                mAppListGridView!!.setItemChecked(position, value)
            }
        } else {
            if (mAppListListView != null) {
                mAppListListView!!.setItemChecked(position, value)
            }
        }
    }

    private companion object {
        const val ARG_GRID_MODE = "grid-mode"
    }
}
