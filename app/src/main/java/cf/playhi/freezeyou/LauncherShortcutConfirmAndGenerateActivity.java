package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class LauncherShortcutConfirmAndGenerateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());
        setContentView(R.layout.lscaga_main);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        Button lscaga_generate_button = findViewById(R.id.lscaga_generate_button);
        ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);
        TextView lscaga_icon_textView = findViewById(R.id.lscaga_icon_textView);
        TextView lscaga_displayName_textView = findViewById(R.id.lscaga_displayName_textView);
        EditText lscaga_displayName_editText = findViewById(R.id.lscaga_displayName_editText);


    }
}
