package cf.playhi.freezeyou;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

public class CommandExecutorActivity extends FreezeYouBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cea_main);
        ThemeUtils.processActionBar(getSupportActionBar());

        init();
    }

    private void init() {
        final EditText printEditText = findViewById(R.id.cea_main_print_editText);
        final EditText inputEditText = findViewById(R.id.cea_main_input_editText);
        final Button submitButton = findViewById(R.id.cea_main_finish_button);


    }

}
