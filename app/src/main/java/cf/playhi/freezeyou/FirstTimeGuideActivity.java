package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class FirstTimeGuideActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        processActionBar(getActionBar());
    }
}
