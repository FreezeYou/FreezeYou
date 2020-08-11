package cf.playhi.freezeyou.app;

import android.app.Service;

import static cf.playhi.freezeyou.utils.Support.checkLanguage;

public abstract class FreezeYouBaseService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        checkLanguage(this);
    }
}
