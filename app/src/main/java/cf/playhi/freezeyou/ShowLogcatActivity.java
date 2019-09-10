package cf.playhi.freezeyou;

import android.os.Bundle;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class ShowLogcatActivity extends FreezeYouBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showlogcat_activity);
        processActionBar(getActionBar());

        EditText editText = findViewById(R.id.sla_log_editText);

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String log_line;
            String rn = System.getProperty("line.separator");
            while ((log_line = bufferedReader.readLine()) != null) {
                editText.setText(editText.getText().append(log_line).append(rn));
            }
            bufferedReader.close();
            process.destroy();
            editText.setSelection(editText.getText().length());

        } catch (IOException e) {
            e.printStackTrace();
            editText.setText(editText.getText().append(e.getLocalizedMessage()));
        }
    }

}
