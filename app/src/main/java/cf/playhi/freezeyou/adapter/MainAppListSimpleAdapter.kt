package cf.playhi.freezeyou.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cf.playhi.freezeyou.R

class MainAppListSimpleAdapter(
    private val context: Context,
    private val appList: MutableList<MutableMap<String, Any?>>,
    private val checkedPackages: List<String>,
    private val gridMode: Boolean
) : BaseAdapter() {
    private val density = context.resources.displayMetrics.density

    override fun getCount(): Int = appList.size
    override fun getItem(position: Int): MutableMap<String, Any?> = appList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun replaceAllInFormerArrayList(list: List<MutableMap<String, Any?>>): Boolean {
        appList.clear()
        val changed = appList.addAll(list)
        notifyDataSetChanged()
        return changed
    }

    fun getStoredArrayList(): MutableList<MutableMap<String, Any?>> = appList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val root = convertView as? LinearLayout ?: if (gridMode) createGridItem() else createListItem()
        val holder = root.tag as Holder
        val item = appList[position]

        when (val icon = item["Img"]) {
            is Bitmap -> holder.icon.setImageBitmap(icon)
            is Drawable -> holder.icon.setImageDrawable(icon)
            else -> holder.icon.setImageDrawable(null)
        }
        holder.name.text = item["Name"].toString()
        holder.packageName?.text = item["PackageName"].toString()
        holder.status?.setImageResource(item["isFrozen"] as? Int ?: 0)
        root.setBackgroundResource(
            if (item["PackageName"] in checkedPackages) {
                R.color.translucentGreyBackground
            } else {
                0
            }
        )
        return root
    }

    private fun createListItem(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(5), dp(8), dp(5))
            layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val icon = ImageView(context).apply {
            contentDescription = "Icon"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(icon, LinearLayout.LayoutParams(dp(40), dp(40)).apply {
            marginEnd = dp(5)
        })

        val labels = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val name = marqueeTextView(18f).apply { setTypeface(typeface, Typeface.BOLD) }
        val packageName = marqueeTextView(12f)
        labels.addView(name, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        labels.addView(packageName, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.addView(labels, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val status = ImageView(context).apply {
            contentDescription = context.getString(R.string.status)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(dp(10), dp(10)).apply {
            marginStart = dp(5)
        })
        root.tag = Holder(icon, name, packageName, status)
        return root
    }

    private fun createGridItem(): LinearLayout {
        val iconSize = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(5), dp(5), dp(5), dp(5))
            layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val icon = ImageView(context).apply {
            contentDescription = context.getString(R.string.appDetail)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val name = marqueeTextView(12f).apply { gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(icon, LinearLayout.LayoutParams(iconSize, iconSize))
        root.addView(name, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.tag = Holder(icon, name, null, null)
        return root
    }

    private fun marqueeTextView(textSizeSp: Float) = TextView(context).apply {
        textSize = textSizeSp
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        isSelected = true
    }

    private fun dp(value: Int): Int = (value * density + 0.5f).toInt()

    private data class Holder(
        val icon: ImageView,
        val name: TextView,
        val packageName: TextView?,
        val status: ImageView?
    )
}
