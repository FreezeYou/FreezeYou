package cf.playhi.freezeyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cf.playhi.freezeyou.Main;

public class SecretCodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startActivity(new Intent(context, Main.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
