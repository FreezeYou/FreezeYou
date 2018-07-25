package cf.playhi.freezeyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cf.playhi.freezeyou.listener.ScreenLockListener;

public class ScreenLockOneKeyFreezeService extends Service {

    private ScreenLockListener screenLockListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (screenLockListener==null) {
            screenLockListener = new ScreenLockListener(getApplicationContext());
            screenLockListener.registerListener();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (screenLockListener!=null) {
            screenLockListener.unregisterListener();
            screenLockListener = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
