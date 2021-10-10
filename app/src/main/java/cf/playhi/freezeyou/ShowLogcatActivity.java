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
        processActionBar(getSupportActionBar());

        EditText editText = findViewById(R.id.sla_log_editText);

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String logLine;
            StringBuilder sb = new StringBuilder();
            String rn = System.getProperty("line.separator");
            while ((logLine = bufferedReader.readLine()) != null) {
                sb.append(logLine).append(rn);
            }
            bufferedReader.close();
            process.destroy();
            editText.setText(sb.toString());
            editText.setSelection(editText.getText().length());

        } catch (IOException e) {
            e.printStackTrace();
            editText.setText(editText.getText().append(e.getLocalizedMessage()));
        }
    }

}
