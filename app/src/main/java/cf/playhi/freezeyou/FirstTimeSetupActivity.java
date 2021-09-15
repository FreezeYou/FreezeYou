package cf.playhi.freezeyou;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class FirstTimeSetupActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time_setup_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        init();
    }

    private void init() {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.first_time_setup_main_frameLayout, new FirstTimeSetupFragment())
                .commit();

        Button first_time_setup_main_next_button = findViewById(R.id.first_time_setup_main_next_button);
        first_time_setup_main_next_button.setOnClickListener(v -> finish());

    }
}
