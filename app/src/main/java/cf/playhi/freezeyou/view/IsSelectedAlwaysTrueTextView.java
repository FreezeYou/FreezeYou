package cf.playhi.freezeyou.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class IsSelectedAlwaysTrueTextView extends AppCompatTextView {

    public IsSelectedAlwaysTrueTextView(@NonNull Context context) {
        super(context);
    }

    public IsSelectedAlwaysTrueTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IsSelectedAlwaysTrueTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isSelected() {
        return true;
    }
}
