package cf.playhi.freezeyou;

import android.content.Context;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;

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
                    if (isScreenOn && !"com.android.systemui".equals(pkgNameString)) {// && !"android".equals(pkgName)
                        String previousPkg = MainApplication.getCurrentPackage();
                        MainApplication.setCurrentPackage(pkgNameString);
                        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("freezeOnceQuit", false)) {
                            if (getApplicationContext().getSharedPreferences("FreezeOnceQuit", Context.MODE_PRIVATE)
                                    .getString("pkgName", "").contains("|" + previousPkg + "|") && !pkgNameString.equals(previousPkg)) {
                                Support.processFreezeAction(getApplicationContext(), null, previousPkg, null, false, false);
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
