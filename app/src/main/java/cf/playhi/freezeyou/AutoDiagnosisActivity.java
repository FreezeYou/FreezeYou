package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.AccessibilityUtils.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class AutoDiagnosisActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autodiagnosis);
        processActionBar(getActionBar());

        new Thread(new Runnable() {
            @Override
            public void run() {
                go();
            }
        }).start();
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

    protected void go() {
        ListView adg_listView = findViewById(R.id.adg_listView);
        ProgressBar adg_progressBar = findViewById(R.id.adg_progressBar);
        List<Map<String, String>> problemsList = new ArrayList<>();

        AppPreferences appPreferences = new AppPreferences(this);
        disableIndeterminate(adg_progressBar);
        setProgress(adg_progressBar, 5);

        checkSystemVersion(problemsList);
        setProgress(adg_progressBar, 15);

        checkAccessibilityService(problemsList, appPreferences);
        setProgress(adg_progressBar, 30);

        checkNotificationListenerPermission(problemsList, appPreferences);
        setProgress(adg_progressBar, 40);

        checkNotifyPermission(problemsList);
        setProgress(adg_progressBar, 50);

        checkIsDeviceOwner(problemsList);
        setProgress(adg_progressBar, 60);

        checkRootPermission(problemsList);
        setProgress(adg_progressBar, 70);

        checkIfNoProblemFound(problemsList);
        setProgress(adg_progressBar, 90);

        SimpleAdapter adapter =
                new SimpleAdapter(
                        this,
                        problemsList,
                        R.layout.adg_list_item,
                        new String[]{"title", "sTitle"},
                        new int[]{R.id.adgli_title_textView, R.id.adgli_subTitle_textView});
        setProgress(adg_progressBar, 100);

        done(adg_progressBar, adg_listView, adapter);
    }

    protected void disableIndeterminate(final ProgressBar progressBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(false);
            }
        });
    }

    protected void done(final ProgressBar progressBar, final ListView listView, final SimpleAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    protected void setProgress(final ProgressBar progressBar, final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 24) {
                    progressBar.setProgress(progress, true);
                } else {
                    progressBar.setProgress(progress);
                }
            }
        });
    }

    protected void checkSystemVersion(List<Map<String, String>> problemsList) {
        if (Build.VERSION.SDK_INT < 21) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", getString(R.string.sysVerLow));
            hashMap.put("sTitle", getString(R.string.someFuncUn));
            problemsList.add(hashMap);
        }
    }

    protected void checkAccessibilityService(List<Map<String, String>> problemsList, AppPreferences appPreferences) {
        if ((getDatabasePath("scheduledTriggerTasks").exists() || appPreferences.getBoolean("freezeOnceQuit", false) || appPreferences.getBoolean("avoidFreezeForegroundApplications", false)) && !isAccessibilitySettingsOn(this)) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", getString(R.string.ACBSNotEnabled));
            hashMap.put("sTitle", getString(R.string.affect) + " " + getString(R.string.avoidFreezeForegroundApplications) + " " + getString(R.string.scheduledTasks) + " " + getString(R.string.etc));
            problemsList.add(hashMap);
        }
    }

    protected void checkNotificationListenerPermission(List<Map<String, String>> problemsList, AppPreferences appPreferences) {
        if (appPreferences.getBoolean("avoidFreezeNotifyingApplications", false)) {
            String s = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (s == null || !s.contains("cf.playhi.freezeyou/cf.playhi.freezeyou.MyNotificationListenerService")) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("title", getString(R.string.noNotificationListenerPermission));
                hashMap.put("sTitle", getString(R.string.affect) + " " + getString(R.string.avoidFreezeNotifyingApplications));
                problemsList.add(hashMap);
            }
        }
    }

    protected void checkNotifyPermission(List<Map<String, String>> problemsList) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null || (Build.VERSION.SDK_INT >= 24 && !notificationManager.areNotificationsEnabled())) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", getString(R.string.noNotifyPermission));
            hashMap.put("sTitle", getString(R.string.mayCannotNotify));
            problemsList.add(hashMap);
        }
    }

    protected void checkIsDeviceOwner(List<Map<String, String>> problemsList) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (!Support.isDeviceOwner(this)) {
            hashMap.put("title", getString(R.string.noMRootPermission));
            hashMap.put("sTitle", getString(R.string.someFuncMayRestrict));
            problemsList.add(hashMap);
        }
    }

    protected void checkRootPermission(List<Map<String, String>> problemsList) {
        boolean hasPermission = true;
        int value = -1;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            value = process.waitFor();
            ProcessUtils.destroyProcess(outputStream, process);
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("permission denied") || e.getMessage().toLowerCase().contains("not found")) {
                hasPermission = false;
            }
        }
        if (!hasPermission || value != 0) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", getString(R.string.noRootPermission));
            hashMap.put("sTitle", getString(R.string.someFuncMayRestrict));
            problemsList.add(hashMap);
        }
    }

    protected void checkIfNoProblemFound(List<Map<String, String>> problemsList) {
        if (problemsList.isEmpty()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", getString(R.string.noProblemsFound));
            hashMap.put("sTitle", getString(R.string.everySeemsAllRight));
            problemsList.add(hashMap);
        }
    }
}
