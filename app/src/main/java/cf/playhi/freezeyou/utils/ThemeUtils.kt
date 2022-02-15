package cf.playhi.freezeyou.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBar
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.R

internal object ThemeUtils {

    @JvmStatic
    fun getThemeDot(@NonNull context: Context): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return R.drawable.shapedot_coloraccent
        }

        val string = getUiTheme(context)
        return if (string != null) {
            when (string) {
                "blue" -> R.drawable.shapedotblue
                "orange" -> R.drawable.shapedotorange
                "green" -> R.drawable.shapedotgreen
                "pink" -> R.drawable.shapedotpink
                "yellow" -> R.drawable.shapedotyellow
                "red" -> R.drawable.shapedotred
                "black", "deepBlack" -> R.drawable.shapedotwhite
                "white" -> R.drawable.shapedotblack
                else -> R.drawable.shapedotblack
            }
        } else {
            R.drawable.shapedotblack
        }
    }

    /**
     * 主要用于各点的 getThemeDot 的另一（相对/相反）状态
     *
     * @param context Context
     * @return 资源 Id
     */
    @JvmStatic
    fun getThemeSecondDot(@NonNull context: Context): Int {
        val string = getUiTheme(context)
        return if (string != null) {
            when (string) {
                "black", "deepBlack" -> R.drawable.shapedotblack
                "blue", "orange", "green", "pink", "yellow", "white", "red" -> R.drawable.shapedotwhite
                else -> R.drawable.shapedotwhite
            }
        } else {
            R.drawable.shapedotwhite
        }
    }

    @JvmStatic
    fun getThemeFabDotBackground(@NonNull context: Context): Int {
        val string = getUiTheme(context)
        return if (string != null) {
            when (string) {
                "pink" -> R.drawable.shapedotpink
                "blue" -> R.drawable.shapedotblue
                "orange" -> R.drawable.shapedotorange
                "green" -> R.drawable.shapedotgreen
                "yellow" -> R.drawable.shapedotyellow
                "red" -> R.drawable.shapedotred
                "black", "white" -> R.drawable.shapedotblack
                else -> R.drawable.shapedotblack
            }
        } else {
            R.drawable.shapedotblack
        }
    }

    @JvmStatic
    fun getUiTheme(@NonNull context: Context): String? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return if (sp.getBoolean("allowFollowSystemAutoSwitchDarkMode", true)) {
            if (isSystemDarkModeEnabled(context))
                if ("dark" == sp.getString("themeOfAutoSwitchDarkMode", "dark"))
                    "black"
                else
                    "deepBlack"
            else sp.getString("uiStyleSelection", "default")
        } else {
            sp.getString("uiStyleSelection", "default")
        }
    }

    private fun isSystemDarkModeEnabled(@NonNull context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    @JvmStatic
    fun processAddTranslucent(@NonNull activity: Activity) {
        val window = activity.window
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawableResource(R.color.realTranslucent)
            when {
                Build.VERSION.SDK_INT >= 21 -> {
                    window.navigationBarColor = Color.TRANSPARENT
                    window.statusBarColor = Color.TRANSPARENT
                }
                Build.VERSION.SDK_INT >= 19 -> {
                    @Suppress("DEPRECATION")
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    @Suppress("DEPRECATION")
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        }
    }

    @JvmStatic
    fun processActionBar(actionBar: ActionBar?) {
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun processSetTheme(@NonNull context: Context, isDialog: Boolean = false) {
        try {
            val string = getUiTheme(context)
            if (string != null) {
                when (string) {
                    "blue" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Blue else R.style.AppTheme_Light_Blue)
                    "orange" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Orange else R.style.AppTheme_Light_Orange)
                    "green" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Green else R.style.AppTheme_Light_Green)
                    "pink" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Pink else R.style.AppTheme_Light_Pink)
                    "yellow" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Yellow else R.style.AppTheme_Light_Yellow)
                    "black" -> context.setTheme(if (isDialog) R.style.AppTheme_Dark_Dialog_Default else R.style.AppTheme_Dark_Default)
                    "red" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_Red else R.style.AppTheme_Light_Red)
                    "deepBlack" -> context.setTheme(if (isDialog) R.style.AppTheme_Dark_Dialog_Black else R.style.AppTheme_Dark_Black)
                    "white" -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_White else R.style.AppTheme_Light_White)
                    else -> context.setTheme(if (isDialog) R.style.AppTheme_Light_Dialog_White else R.style.AppTheme_Light_White)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}