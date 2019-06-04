package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class FirstTimeSetupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time_setup_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.hide();

        init();
    }

    private void init() {

        Button first_time_setup_main_next_button = findViewById(R.id.first_time_setup_main_next_button);
        first_time_setup_main_next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Save

                finish();
            }
        });

    }
}
