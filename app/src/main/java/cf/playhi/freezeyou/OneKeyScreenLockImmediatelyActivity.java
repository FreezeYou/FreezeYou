package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class OneKeyScreenLockImmediatelyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(
                    new Intent(getApplicationContext(), OneKeyScreenLockImmediatelyService.class));
        } else {
            this.startService(
                    new Intent(getApplicationContext(), OneKeyScreenLockImmediatelyService.class));
        }
        finish();
    }
}
