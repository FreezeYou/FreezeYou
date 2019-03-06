package cf.playhi.freezeyou;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;

class ObsdAlertDialog extends AlertDialog {
    private boolean isObsd;

    ObsdAlertDialog(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        isObsd = (ev.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0;
        return super.dispatchTouchEvent(ev);
    }

    boolean isObsd() {
        return isObsd;
    }
}
