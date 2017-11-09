package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Thread initThread;
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
                        int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                        if (tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER||tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                            keyValuePair.put("Name", name+"("+getString(R.string.frozen)+")");
                        } else {
                            keyValuePair.put("Name", name);
                        }
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

                app_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                        final String name=map.get("Name");
                        final String pkgName=map.get("PackageName");
                        int tmp = getPackageManager().getApplicationEnabledSetting(pkgName);
                        if (tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER||tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                            Support.makeDialog(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                        } else {
                            Support.makeDialog2(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                        }
                        return true;
                    }
                });

                app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, final long l) {
                        HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                        final String name=map.get("Name");
                        final String pkgName=map.get("PackageName");
                        AlertDialog alertDialog = new AlertDialog.Builder(Main.this)
                                .setTitle(name)
                                .setMessage(R.string.createFreezeShortcutNotice)
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int ii) {
                                        try{
                                            createShortCut(name.replace("("+getString(R.string.frozen)+")",""),pkgName,getPackageManager().getApplicationIcon(pkgName));
                                        }catch (PackageManager.NameNotFoundException e){
                                            Toast.makeText(getApplicationContext(),R.string.cannotFindApp,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.more, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this)
                                                .setTitle(R.string.more)
                                                .setMessage(getString(R.string.chooseDetailAction))
                                                .setNeutralButton(R.string.appDetail, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        Uri uri = Uri.fromParts("package", pkgName,null);
                                                        intent.setData(uri);
                                                        startActivity(intent);
                                                    }
                                                });
                                        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                                                "AutoFreezeApplicationList", Context.MODE_PRIVATE);
                                        final String pkgNameList = sharedPreferences.getString("pkgName","");
                                        if (pkgNameList.contains("|"+pkgName+"|")){
                                            dialog.setPositiveButton(R.string.removeFromOneKeyList, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int ii) {
                                                    if (sharedPreferences.edit()
                                                            .putString(
                                                                    "pkgName",
                                                                    pkgNameList.replace("|"+pkgName+"|",""))
                                                            .commit()) {
                                                        Toast.makeText(getApplicationContext(), R.string.removed, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), R.string.removeFailed, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            dialog.setPositiveButton(R.string.addToOneKeyList, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int ii) {
                                                    if (sharedPreferences.edit().putString("pkgName", pkgNameList+"|" + pkgName + "|").commit()) {
                                                        Toast.makeText(getApplicationContext(), R.string.added, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), R.string.addFailed, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                        dialog.create().show();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                });
            }
        });
        initThread.start();
        AlertDialog alertDialog = new AlertDialog.Builder(Main.this)
                .setTitle(R.string.caution)
                .setMessage(R.string.cautionContent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int ii) {
                    }
                })
                .create();
        alertDialog.show();
    }

    private void createShortCut(String title, String pkgName, Drawable icon){
        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
        Intent intent = new Intent(getApplicationContext(), Freeze.class);
        intent.putExtra("pkgName",pkgName);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        BitmapDrawable bd = (BitmapDrawable) icon;
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        try{
            sendBroadcast(addShortCut);
            Toast.makeText(getApplicationContext(),R.string.requested,Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(),getString(R.string.requestFailed)+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_createOneKeyFreezeShortCut:
                Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
                Intent intent = new Intent(getApplicationContext(), OneKeyFreeze.class);
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.oneKeyFreeze));
                BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_round);
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                try{
                    sendBroadcast(addShortCut);
                    Toast.makeText(getApplicationContext(),R.string.requested,Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(),getString(R.string.requestFailed)+e.getMessage(),Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.menu_about:
                Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou");
                Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                if (about.resolveActivity(getPackageManager()) != null) {
                    startActivity(about);
                } else {
                    Toast.makeText(getApplicationContext(),R.string.plsVisitPXXXX,Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.menu_oneKeyFreezeImmediately:
                startActivity(new Intent(this,OneKeyFreeze.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
