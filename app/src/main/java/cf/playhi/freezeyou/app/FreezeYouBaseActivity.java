package cf.playhi.freezeyou.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import cf.playhi.freezeyou.utils.Support;

@SuppressLint("Registered")
public class FreezeYouBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Support.checkLanguage(this);
        super.onCreate(savedInstanceState);
    }

}
