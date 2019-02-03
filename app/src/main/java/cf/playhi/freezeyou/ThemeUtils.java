package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
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
                default:
                    resId = R.drawable.shapedotblack;//resId = R.drawable.shapedotblue;
                    break;
            }
        } else {
            resId = R.drawable.shapedotblack;//resId = R.drawable.shapedotblue;
        }
        return resId;
    }

    static String getUiTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("uiStyleSelection", "default");
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
        try {
            String string = getUiTheme(context);
            if (string != null) {
                switch (string) {
                    case "blue":
                        context.setTheme(R.style.AppTheme_Default_Blue);
                        break;
                    case "orange":
                        context.setTheme(R.style.AppTheme_Default_Orange);
                        break;
                    case "green":
                        context.setTheme(R.style.AppTheme_Default_Green);
                        break;
                    case "pink":
                        context.setTheme(R.style.AppTheme_Default_Pink);
                        break;
                    case "yellow":
                        context.setTheme(R.style.AppTheme_Default_Yellow);
                        break;
                    case "black":
                        context.setTheme(R.style.AppTheme_Default);
                        break;
                    case "white":
                        context.setTheme(R.style.AppTheme_Default_White);
                        break;
                    case "red":
                        context.setTheme(R.style.AppTheme_Default_Red);
                        break;
                    default:
                        context.setTheme(R.style.AppTheme_Default_White);
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
