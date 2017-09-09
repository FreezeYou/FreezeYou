package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView app_listView = findViewById(R.id.app_list);
        final List<Map<String, String>> AppList = new ArrayList<Map<String, String>>();
        List<ApplicationInfo> applicationInfo = getApplicationContext().getPackageManager().getInstalledApplications(0);
        int size = applicationInfo.size();
        for(int i=0;i<size;i++){
            String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
            String packageName = applicationInfo.get(i).packageName;
            Map<String, String> keyValuePair = new HashMap<String, String>();
            keyValuePair.put("Name", name);
            keyValuePair.put("PackageName", packageName);
            AppList.add(keyValuePair);
        }

        ListAdapter adapter = new SimpleAdapter(this, AppList,
                android.R.layout.simple_list_item_2, new String[] { "Name",
                "PackageName" }, new int[] { android.R.id.text1,android.R.id.text2 });
        app_listView.setAdapter(adapter);

        final List<ApplicationInfo> applicationInfoF = applicationInfo;
        app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, final long l) {
                AlertDialog alertDialog = new AlertDialog.Builder(Main.this)
                        .setTitle("提示")
                        .setMessage("是否在桌面创建冻结/解冻 "+ getPackageManager().getApplicationLabel(applicationInfoF.get(i)).toString() +" 的快捷方式？")
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {
                                createShortCut(getPackageManager().getApplicationLabel(applicationInfoF.get(i)).toString(),
                                        applicationInfoF.get(i).packageName,getPackageManager().getApplicationIcon(applicationInfoF.get(i)));
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
    }

    public void createShortCut(String title, String pkgName, Drawable icon){
        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
        Intent intent = new Intent(getApplicationContext(), Freeze.class);
        intent.putExtra("pkgName",pkgName);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        BitmapDrawable bd = (BitmapDrawable) icon;
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON,bd.getBitmap());
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        sendBroadcast(addShortCut);
    }
}
