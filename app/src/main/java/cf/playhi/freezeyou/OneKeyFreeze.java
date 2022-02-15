package cf.playhi.freezeyou;

import android.content.Intent;
import android.os.Bundle;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.service.OneKeyFreezeService;
import cf.playhi.freezeyou.utils.ServiceUtils;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
public class OneKeyFreeze extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, OneKeyFreeze.class));
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.oneKeyFreeze));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_new_round));
            setResult(RESULT_OK, intent);
        } else {
            ServiceUtils.startService(
                    this,
                    new Intent(getApplicationContext(), OneKeyFreezeService.class)
                            .putExtra("autoCheckAndLockScreen", getIntent().getBooleanExtra("autoCheckAndLockScreen", true)));
        }
        finish();
    }
}
