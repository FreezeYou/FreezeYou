package cf.playhi.freezeyou;

import android.annotation.SuppressLint;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.Support.existsInOneKeyList;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    @SuppressLint("SwitchIntDef")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (accessibilityEvent.isFullScreen()) {
                    CharSequence pkgName = accessibilityEvent.getPackageName();
                    if (pkgName != null) {
                        boolean isScreenOn = true;
                        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                        if (pm != null) {
                            isScreenOn = pm.isScreenOn();
                        }
                        String pkgNameString = pkgName.toString();
                        if (isScreenOn &&
                                !"".equals(pkgNameString) &&
                                !"com.android.systemui".equals(pkgNameString) &&
                                !"com.android.packageinstaller".equals(pkgNameString) &&
                                !"android".equals(pkgNameString)) {
                            String previousPkg = MainApplication.getCurrentPackage();
                            MainApplication.setCurrentPackage(pkgNameString);
                            if (!pkgNameString.equals(previousPkg)
                                    && new AppPreferences(getApplicationContext()).getBoolean("freezeOnceQuit", false)
                                    && existsInOneKeyList(getApplicationContext(), getString(R.string.sFreezeOnceQuit), previousPkg)) {
                                Support.processFreezeAction(getApplicationContext(), previousPkg, false, null, false);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }
}
