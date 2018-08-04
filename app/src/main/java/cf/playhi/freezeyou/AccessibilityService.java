package cf.playhi.freezeyou;

import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                CharSequence pkgName = accessibilityEvent.getPackageName();
                if (pkgName != null){
                    boolean isScreenOn = true;
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (pm!=null){
                        isScreenOn = pm.isScreenOn();
                    }
                    if (isScreenOn){// && !"android".equals(pkgName) && !"com.android.systemui".equals(pkgName)
                        MainApplication.setCurrentPackage(pkgName.toString());
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
