package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static cf.playhi.freezeyou.Support.checkUpdate;
import static cf.playhi.freezeyou.Support.getVersionCode;
import static cf.playhi.freezeyou.Support.getVersionName;
import static cf.playhi.freezeyou.Support.joinQQGroup;
import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;
import static cf.playhi.freezeyou.Support.requestOpenWebSite;
import static cf.playhi.freezeyou.Support.showToast;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        processActionBar(getActionBar());

        final Activity activity = AboutActivity.this;

        TextView aboutSlogan = findViewById(R.id.about_slogan);

        final String[] aboutData = new String[]{
                getResources().getString(R.string.hToUse),
                getResources().getString(R.string.helpTranslate),
                getResources().getString(R.string.thanksList),
                getResources().getString(R.string.visitWebsite),
                getResources().getString(R.string.addQQGroup),
                getResources().getString(R.string.update),
                "V" + getVersionName(getApplicationContext()) + "(" + getVersionCode(getApplicationContext()) + ")"
        };

        ListView aboutListView = findViewById(R.id.about_listView);

        aboutListView.setAdapter(new ArrayAdapter<>(AboutActivity.this, android.R.layout.simple_list_item_1, aboutData));

        aboutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        requestOpenWebSite(activity, "https://freezeyou.playhi.cf/");
                        break;
                    case 1:
                        requestOpenWebSite(activity, "https://crwd.in/freezeyou");
                        break;
                    case 2:
                        requestOpenWebSite(activity, "https://freezeyou.playhi.cf/thanks.html");
                        break;
                    case 3:
                        requestOpenWebSite(activity, "https://freezeyou.playhi.cf");
                        break;
                    case 4:
                        joinQQGroup(activity);
                        break;
                    case 5:
                        checkUpdate(activity);
                        break;
                    case 6:
                        showToast(activity, "V" + getVersionName(activity) + "(" + getVersionCode(activity) + ")");
                        break;
                    default:
                        break;
                }
            }
        });

        aboutSlogan.setText(String.format("V %s", getVersionCode(activity)));
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
}
