package cf.playhi.freezeyou;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class OneKeyScreenLockImmediatelyService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Support.doLockScreen(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
