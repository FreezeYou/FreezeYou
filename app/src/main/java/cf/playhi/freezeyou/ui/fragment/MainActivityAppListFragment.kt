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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import cf.playhi.freezeyou.R
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            FreezeYouTheme {
                AndroidView(
                    factory = { context ->
                        if (mUseGridMode) {
                            GridView(context).apply {
                                mAppListGridView = this
                                onItemClickListener = mOnItemClickListener
                                onItemLongClickListener = mOnItemLongClickListener
                                mMultiChoiceModeListener?.let(::setMultiChoiceModeListener)
                                adapter = mAppListAdapter
                                choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
                                columnWidth =
                                    (resources.getDimension(android.R.dimen.app_icon_size) * 1.6).toInt()
                                numColumns = GridView.AUTO_FIT
                                stretchMode = GridView.STRETCH_SPACING_UNIFORM
                                verticalSpacing = (6 * resources.displayMetrics.density).toInt()
                                isFastScrollEnabled = true
                                gravity = android.view.Gravity.CENTER
                            }
                        } else {
                            ListView(context).apply {
                                mAppListListView = this
                                onItemClickListener = mOnItemClickListener
                                onItemLongClickListener = mOnItemLongClickListener
                                mMultiChoiceModeListener?.let(::setMultiChoiceModeListener)
                                adapter = mAppListAdapter
                                choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
                                divider = null
                                dividerHeight = 0
                                isFastScrollEnabled = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    fun setUseGridMode(b: Boolean) {
        mUseGridMode = b
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
}
