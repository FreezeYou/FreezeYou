package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class OneKeyScreenLockQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        Support.doLockScreen(getApplicationContext());
    }
}
