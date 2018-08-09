package cf.playhi.freezeyou;

import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.Support.existsInOneKeyList;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                CharSequence pkgName = accessibilityEvent.getPackageName();
                if (pkgName != null) {
                    boolean isScreenOn = true;
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (pm != null) {
                        isScreenOn = pm.isScreenOn();
                    }
                    String pkgNameString = pkgName.toString();
                    if (isScreenOn && !"com.android.systemui".equals(pkgNameString) && !"android".equals(pkgNameString)) {
                        String previousPkg = MainApplication.getCurrentPackage();
                        MainApplication.setCurrentPackage(pkgNameString);
                        if (!pkgNameString.equals(previousPkg)
                                && new AppPreferences(getApplicationContext()).getBoolean("freezeOnceQuit", false)
                                && existsInOneKeyList(getApplicationContext(), getString(R.string.sFreezeOnceQuit), previousPkg)) {
                            Support.processFreezeAction(getApplicationContext(), null, previousPkg, null, false, false);
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
