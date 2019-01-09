package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class OneKeyFreezeQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        ServiceUtils.startService(this,new Intent(getApplicationContext(), OneKeyFreezeService.class));
    }
}
