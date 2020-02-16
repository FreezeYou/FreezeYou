package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

final class ThemeUtils {

    static int getThemeDot(Context context) {
        int resId;
        String string = getUiTheme(context);
        if (string != null) {
            switch (string) {
                case "blue":
                    resId = R.drawable.shapedotblue;
                    break;
                case "orange":
                    resId = R.drawable.shapedotorange;
                    break;
                case "green":
                    resId = R.drawable.shapedotgreen;
                    break;
                case "pink":
                    resId = R.drawable.shapedotpink;
                    break;
                case "yellow":
                    resId = R.drawable.shapedotyellow;
                    break;
                case "white":
                    resId = R.drawable.shapedotblack;
                    break;
                case "red":
                    resId = R.drawable.shapedotred;
                    break;
                case "black":
                    resId = R.drawable.shapedotwhite;
                    break;
                default:
                    resId = R.drawable.shapedotblack;//resId = R.drawable.shapedotblue;
                    break;
            }
        } else {
            resId = R.drawable.shapedotblack;//resId = R.drawable.shapedotblue;
        }
        return resId;
    }

    /**
     * 主要用于各点的 getThemeDot 的另一（相对/相反）状态
     *
     * @param context Context
     * @return 资源 Id
     */
    static int getThemeSecondDot(Context context) {
        int resId;
        String string = getUiTheme(context);
        if (string != null) {
            switch (string) {
                case "black":
                    resId = R.drawable.shapedotblack;
                    break;
                case "blue":
                case "orange":
                case "green":
                case "pink":
                case "yellow":
                case "white":
                case "red":
                default:
                    resId = R.drawable.shapedotwhite;
                    break;
            }
        } else {
            resId = R.drawable.shapedotwhite;
        }
        return resId;
    }

    static int getThemeFabDotBackground(Context context) {
        int resId;
        String string = getUiTheme(context);
        if (string != null) {
            switch (string) {
                case "pink":
                    resId = R.drawable.shapedotpink;
                    break;
                case "blue":
                    resId = R.drawable.shapedotblue;
                    break;
                case "orange":
                    resId = R.drawable.shapedotorange;
                    break;
                case "green":
                    resId = R.drawable.shapedotgreen;
                    break;
                case "yellow":
                    resId = R.drawable.shapedotyellow;
                    break;
                case "red":
                    resId = R.drawable.shapedotred;
                    break;
                case "black":
                case "white":
                default:
                    resId = R.drawable.shapedotblack;
                    break;
            }
        } else {
            resId = R.drawable.shapedotblack;
        }
        return resId;
    }

    static String getUiTheme(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("allowFollowSystemAutoSwitchDarkMode", true)) {
            return isSystemDarkModeEnabled(context) ? "black" :
                    sp.getString("uiStyleSelection", "default");
        } else {
            return sp.getString("uiStyleSelection", "default");
        }
    }

    private static boolean isSystemDarkModeEnabled(Context context) {
        return (context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    static void processAddTranslucent(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawableResource(R.color.realTranslucent);
            if (Build.VERSION.SDK_INT >= 19) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    static void processActionBar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    static void processSetTheme(Context context) {
        processSetTheme(context, false);
    }

    static void processSetTheme(Context context, boolean isDialog) {
        try {
            String string = getUiTheme(context);
            if (string != null) {
                switch (string) {
                    case "blue":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Blue : R.style.AppTheme_Default_Blue);
                        break;
                    case "orange":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Orange : R.style.AppTheme_Default_Orange);
                        break;
                    case "green":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Green : R.style.AppTheme_Default_Green);
                        break;
                    case "pink":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Pink : R.style.AppTheme_Default_Pink);
                        break;
                    case "yellow":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Yellow : R.style.AppTheme_Default_Yellow);
                        break;
                    case "black":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog : R.style.AppTheme_Default);
                        break;
                    case "white":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_White : R.style.AppTheme_Default_White);
                        break;
                    case "red":
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_Red : R.style.AppTheme_Default_Red);
                        break;
                    default:
                        context.setTheme(isDialog ? R.style.AppTheme_Default_Dialog_White : R.style.AppTheme_Default_White);
//                        if (Build.VERSION.SDK_INT >= 21) {
//                            context.setTheme(R.style.AppTheme_Default_Blue);
//                        } else {
//                            context.setTheme(R.style.AppTheme_Default);
//                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
