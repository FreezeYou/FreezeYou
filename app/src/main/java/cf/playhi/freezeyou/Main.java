package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//com.ibm.icu.text.Collator

public class Main extends Activity {
    Thread initThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initThread = new Thread(new Runnable() {
            @Override
            public void run() {

                final ListView app_listView = findViewById(R.id.app_list);
                final ProgressBar progressBar = findViewById(R.id.progressBar);
                final TextView textView = findViewById(R.id.textView);
                final LinearLayout linearLayout = findViewById(R.id.layout2);
                final List<Map<String, Object>> AppList = new ArrayList<>();
                Drawable icon;
                List<ApplicationInfo> applicationInfo = getApplicationContext().getPackageManager().getInstalledApplications(0);
                int size = applicationInfo.size();
                for(int i=0;i<size;i++){
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android")||packageName.equals("cf.playhi.freezeyou"))){
                        Map<String, Object> keyValuePair = new HashMap<>();
                        icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                        if (icon!=null){
                            keyValuePair.put("Img",icon);
                        }else {
                            keyValuePair.put("Img",android.R.drawable.sym_def_app_icon);
                        }
                        keyValuePair.put("Name", name);
                        keyValuePair.put("PackageName", packageName);
                        AppList.add(keyValuePair);
                    }
                }

                if (!AppList.isEmpty()) {
                    Collections.sort(AppList, new Comparator<Map<String,Object>>() {

                        @Override
                        public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                            return ((String) stringObjectMap.get("PackageName")).compareTo((String) t1.get("PackageName"));
                        }
                    });
                }

                final SimpleAdapter adapter = new SimpleAdapter(Main.this, AppList,
                        R.layout.app_list_1, new String[] { "Img","Name",
                        "PackageName" }, new int[] { R.id.img,R.id.name,R.id.pkgName});

                adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                    public boolean setViewValue(View view, Object data,
                                                String textRepresentation) {
                        if (view instanceof ImageView && data instanceof Drawable) {
                            ImageView imageView = (ImageView) view;
                            imageView.setImageDrawable((Drawable) data);
                            return true;
                        } else
                            return false;
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        app_listView.setAdapter(adapter);
                    }
                });


                app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, final long l) {
                        HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                        final String name=map.get("Name");
                        final String pkgName=map.get("PackageName");
                        AlertDialog alertDialog = new AlertDialog.Builder(Main.this)
                                .setTitle("提示")
                                .setMessage("是否在桌面创建冻结/解冻 "+ name +" 的快捷方式？")
                                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int ii) {
                                        try{
                                            createShortCut(name,pkgName,getPackageManager().getApplicationIcon(pkgName));
                                        }catch (PackageManager.NameNotFoundException e){
                                            Toast.makeText(getApplicationContext(),"未找到该应用程序",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                });
            }
        });
        initThread.start();
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
        Toast.makeText(getApplicationContext(),"已发出创建请求",Toast.LENGTH_SHORT).show();
    }

}
