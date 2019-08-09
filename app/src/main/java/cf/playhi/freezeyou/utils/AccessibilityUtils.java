package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import cf.playhi.freezeyou.AccessibilityService;
import cf.playhi.freezeyou.R;

import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public final class AccessibilityUtils {

    public static void openAccessibilitySettings(Context context) {
        try {
            Intent accessibilityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(accessibilityIntent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(context, R.string.failed);
        }
    }

    //https://stackoverflow.com/questions/18094982/detect-if-my-accessibility-service-is-enabled
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + AccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    public static void checkAndRequestIfAccessibilitySettingsOff(Context context){
        if (!AccessibilityUtils.isAccessibilitySettingsOn(context)) {
            showToast(context, R.string.needActiveAccessibilityService);
            AccessibilityUtils.openAccessibilitySettings(context);
        }
    }

}
