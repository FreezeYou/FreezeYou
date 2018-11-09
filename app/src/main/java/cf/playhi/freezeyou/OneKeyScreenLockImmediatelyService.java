package cf.playhi.freezeyou;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OneKeyScreenLockImmediatelyService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DevicePolicyManagerUtils.doLockScreen(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
