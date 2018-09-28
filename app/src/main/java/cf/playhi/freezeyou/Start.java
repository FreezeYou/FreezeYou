package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.showToast;

public class Start extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showInRecents", true)) {
            showToast(this,"1");
            startActivity(new Intent(this, Main.class));
        } else {
            showToast(this,"2");
            startActivity(new Intent(this, Main.class)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
        }

        Start.this.finish();
    }
}
