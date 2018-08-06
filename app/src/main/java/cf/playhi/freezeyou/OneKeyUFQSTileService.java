package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class OneKeyUFQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(
                    new Intent(getApplicationContext(), OneKeyUFService.class));
        } else {
            this.startService(
                    new Intent(getApplicationContext(), OneKeyUFService.class));
        }
    }
}
