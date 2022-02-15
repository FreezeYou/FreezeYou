package cf.playhi.freezeyou.app;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.CallSuper;

import com.google.android.play.core.splitcompat.SplitCompat;

import java.util.Locale;

import static cf.playhi.freezeyou.utils.Support.checkLanguage;
import static cf.playhi.freezeyou.utils.Support.getLocalString;
import static cf.playhi.freezeyou.utils.VersionUtils.isGooglePlayVersion;

public abstract class FreezeYouBaseService extends Service {

    @Override
    @CallSuper
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String locale = getLocalString(newBase);
            Configuration configuration = new Configuration();
            configuration.setLocale(
                    "Default".equals(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale)
            );
            Context context = newBase.createConfigurationContext(configuration);
            super.attachBaseContext(context);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            checkLanguage(this);
        } else {
            if (isGooglePlayVersion(this)) {
                SplitCompat.install(this);
            }
        }
        super.onCreate();
    }
}
