package cf.playhi.freezeyou.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class IsSelectedAlwaysTrueTextView extends TextView {
    public IsSelectedAlwaysTrueTextView(Context context) {
        super(context);
    }

    public IsSelectedAlwaysTrueTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IsSelectedAlwaysTrueTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IsSelectedAlwaysTrueTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isSelected() {
        return true;
    }
}
