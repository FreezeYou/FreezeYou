package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;

public class OneKeyScreenLockImmediatelyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Support.doLockScreen(this);
        finish();
    }
}
