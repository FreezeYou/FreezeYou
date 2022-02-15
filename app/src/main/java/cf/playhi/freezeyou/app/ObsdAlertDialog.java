package cf.playhi.freezeyou.app;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;

public class ObsdAlertDialog extends AlertDialog {
    private boolean isObsd;

    public ObsdAlertDialog(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        isObsd = (ev.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0;
        return super.dispatchTouchEvent(ev);
    }

    public boolean isObsd() {
        return isObsd;
    }
}
