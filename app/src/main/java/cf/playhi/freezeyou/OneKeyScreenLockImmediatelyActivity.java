package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;

public class OneKeyScreenLockImmediatelyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, OneKeyScreenLockImmediatelyActivity.class));
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.oneKeyLockScreen));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.screenlock));
            setResult(RESULT_OK, intent);
        } else {
            DevicePolicyManagerUtils.doLockScreen(this);
        }
        finish();
    }
}
