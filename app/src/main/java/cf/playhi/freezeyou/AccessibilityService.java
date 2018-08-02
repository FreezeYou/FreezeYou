package cf.playhi.freezeyou;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (accessibilityEvent.getPackageName() != null && !"android".equals(accessibilityEvent.getPackageName().toString())){
                    MainApplication.setCurrentPackage(accessibilityEvent.getPackageName().toString());
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
