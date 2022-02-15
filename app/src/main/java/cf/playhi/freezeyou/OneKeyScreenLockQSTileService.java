package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import cf.playhi.freezeyou.ui.OneKeyScreenLockImmediatelyActivity;

@TargetApi(Build.VERSION_CODES.N)
public class OneKeyScreenLockQSTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        startActivity(
                new Intent(this, OneKeyScreenLockImmediatelyActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
