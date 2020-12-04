package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class LaunchFreezeYouQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        startActivityAndCollapse(
                new Intent(this, Main.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
