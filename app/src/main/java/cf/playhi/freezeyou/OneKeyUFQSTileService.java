package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import cf.playhi.freezeyou.service.OneKeyUFService;
import cf.playhi.freezeyou.utils.ServiceUtils;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
@TargetApi(Build.VERSION_CODES.N)
public class OneKeyUFQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        ServiceUtils.startService(
                this,
                new Intent(getApplicationContext(), OneKeyUFService.class));
    }
}
