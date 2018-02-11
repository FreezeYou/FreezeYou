package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.checkFrozen;
import static cf.playhi.freezeyou.Support.getBitmapFromLocalFile;
import static cf.playhi.freezeyou.Support.getVersionCode;
import static cf.playhi.freezeyou.Support.showToast;

public class Main extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Thread initThread;
        initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mode = getIntent().getStringExtra("pkgName");
                if (mode == null){
                    mode = "all";
                }
                switch (mode){
                    case "OF":
                        generateList("OF");
                        break;
                    case "UF":
                        generateList("UF");
                        break;
                    case "OO":
                        generateList("OO");
                        break;
                    default:
                        generateList("all");
                        break;
                }
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
                .setNeutralButton(R.string.hMRoot, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri webPage = Uri.parse("https://github.com/Playhi/FreezeYou/wiki/%E5%85%8DROOT%E4%BD%BF%E7%94%A8");
                        Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                        if (about.resolveActivity(getPackageManager()) != null) {
                            startActivity(about);
                        } else {
                            showToast(getApplicationContext(),R.string.plsVisitPXXXX);
                        }
                    }
                })
                .create();
        alertDialog.show();
    }


    private void createShortCut(String title, String pkgName, Drawable icon,Class<?> cls,String id){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            createShortCut_OldApi(title,pkgName,icon,cls);
        } else {
            ShortcutManager mShortcutManager =
                    this.getSystemService(ShortcutManager.class);
            if (mShortcutManager!=null){
                if (mShortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo.Builder shortcutInfoBuilder =
                            new ShortcutInfo.Builder(this, id);
                    shortcutInfoBuilder.setIcon(Icon.createWithBitmap(getBitmapFromDrawable(icon)));
                    shortcutInfoBuilder.setIntent(
                            new Intent(getApplicationContext(), cls)
                                    .setAction(Intent.ACTION_MAIN)
                                    .putExtra("pkgName",pkgName)
                    );
                    shortcutInfoBuilder.setShortLabel(title);
                    shortcutInfoBuilder.setLongLabel(title);
                    // Assumes there's already a shortcut with the ID "my-shortcut".
                    // The shortcut must be enabled.
                    ShortcutInfo pinShortcutInfo = shortcutInfoBuilder.build();
                    // Create the PendingIntent object only if your app needs to be notified
                    // that the user allowed the shortcut to be pinned. Note that, if the
                    // pinning operation fails, your app isn't notified. We assume here that the
                    // app has implemented a method called createShortcutResultIntent() that
                    // returns a broadcast intent.
                    Intent pinnedShortcutCallbackIntent =
                            mShortcutManager.createShortcutResultIntent(pinShortcutInfo);

                    // Configure the intent so that your app's broadcast receiver gets
                    // the callback successfully.
                    PendingIntent successCallback = PendingIntent.getBroadcast(this, 0,
                            pinnedShortcutCallbackIntent, 0);

                    mShortcutManager.requestPinShortcut(pinShortcutInfo,
                            successCallback.getIntentSender());
                }else {
                    createShortCut_OldApi(title,pkgName,icon,cls);
                }
            } else {
                createShortCut_OldApi(title,pkgName,icon,cls);
            }
        }
    }

    private void createShortCut_OldApi(String title, String pkgName, Drawable icon,Class<?> cls){
        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.putExtra("pkgName",pkgName);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
//        BitmapDrawable bd = (BitmapDrawable) icon;bd.getBitmap()
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(getBitmapFromDrawable(icon),192,192,true));
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        try{
            sendBroadcast(addShortCut);
            showToast(Main.this,R.string.requested);
        } catch (Exception e){
            showToast(Main.this,getString(R.string.requestFailed)+e.getMessage());
        }
    }

    //https://stackoverflow.com/questions/44447056/convert-adaptiveicondrawable-to-bitmap-in-android-o-preview
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
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
                createShortCut(
                        getString(R.string.oneKeyFreeze),
                        "",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),OneKeyFreeze.class,
                        "OneKeyFreeze"
                );
                return true;
            case R.id.menu_createOnlyFrozenShortCut:
                createShortCut(
                        getString(R.string.onlyFrozen),
                        "OF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "OF"
                );
                return true;
            case R.id.menu_createOnlyUFShortCut:
                createShortCut(
                        getString(R.string.onlyUF),
                        "UF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "UF"
                );
                return true;
            case R.id.menu_createOnlyOnekeyShortCut:
                createShortCut(
                        getString(R.string.onlyOnekey),
                        "OO",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "OO"
                );
                return true;
            case R.id.menu_about:
                Support.buildAlertDialog(this,R.mipmap.ic_launcher_round,R.string.about_message,R.string.about)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNeutralButton(R.string.visitWebsite, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou");
                                Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                                if (about.resolveActivity(getPackageManager()) != null) {
                                    startActivity(about);
                                } else {
                                    showToast(getApplicationContext(),R.string.plsVisitPXXXX);
                                }
                            }
                        })
                        .setPositiveButton(R.string.addQQGroup, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Support.joinQQGroup(Main.this);
                            }
                        }).create().show();
                return true;
            case R.id.menu_oneKeyFreezeImmediately:
                startActivity(new Intent(this,OneKeyFreeze.class));
                return true;
            case R.id.menu_vM_onlyFrozen:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("OF");
                    }
                }).start();
                return true;
            case R.id.menu_vM_onlyUF:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("UF");
                    }
                }).start();
                return true;
            case R.id.menu_vM_all:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("all");
                    }
                }).start();
                return true;
            case R.id.menu_vM_onlyOnekey:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("OO");
                    }
                }).start();
                return true;
            case R.id.menu_vM_onlySA:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("OS");
                    }
                }).start();
                return true;
            case R.id.menu_vM_onlyUA:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateList("OU");
                    }
                }).start();
                return true;
            case R.id.menu_update:
                Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou/checkupdate.php?v=" + getVersionCode(this));
                Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                if (about.resolveActivity(getPackageManager()) != null) {
                    startActivity(about);
                } else {
                    showToast(this,"请访问 https://app.playhi.cf/freezeyou/checkupdate.php?v=" + getVersionCode(this));
                }
                return true;
            case R.id.menu_exit:
                System.exit(0);
                return true;
            case R.id.menu_moreSettings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateList(String filter){
        final ListView app_listView = findViewById(R.id.app_list);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView textView = findViewById(R.id.textView);
        final LinearLayout linearLayout = findViewById(R.id.layout2);
        final List<Map<String, Object>> AppList = new ArrayList<>();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                app_listView.setVisibility(View.GONE);
            }
        });
        Drawable icon;
        List<ApplicationInfo> applicationInfo = getApplicationContext().getPackageManager().getInstalledApplications(0);
        int size = applicationInfo.size();
        switch (filter) {
            case "all":
                addMRootApplications(getApplicationContext(),AppList);
                for (int i = 0; i < size; i++) {
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android") || packageName.equals("cf.playhi.freezeyou"))) {
                        Map<String, Object> keyValuePair = new HashMap<>();
                        icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                        if (icon != null) {
                            keyValuePair.put("Img", icon);
                        } else {
                            keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                        }
                        int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                        if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                            keyValuePair.put("Name", name + "(" + getString(R.string.frozen) + ")");
                        } else {
                            keyValuePair.put("Name", name);
                        }
                        keyValuePair.put("PackageName", packageName);
                        AppList.add(keyValuePair);
                    } else if ((i+1==size)&&(AppList.size()==0)){
                        addNotAvailablePair(getApplicationContext(),AppList);
                    }
                }
                break;
            case "OF":
                addMRootApplications(getApplicationContext(),AppList);
                for (int i = 0; i < size; i++) {
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android") || packageName.equals("cf.playhi.freezeyou"))) {
                        int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                        if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                                tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                            Map<String, Object> keyValuePair = new HashMap<>();
                            icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                            if (icon != null) {
                                keyValuePair.put("Img", icon);
                            } else {
                                keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                            }
                            keyValuePair.put("Name", name + "(" + getString(R.string.frozen) + ")");
                            keyValuePair.put("PackageName", packageName);
                            AppList.add(keyValuePair);
                        } else if ((i+1==size)&&(AppList.size()==0)){
                            Map<String, Object> keyValuePair = new HashMap<>();
                            keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                            keyValuePair.put("Name", getString(R.string.notAvailable));
                            keyValuePair.put("PackageName", getString(R.string.notAvailable));
                            AppList.add(keyValuePair);
                        }
                    }
                }
                break;
            case "UF":
                for (int i = 0; i < size; i++) {
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android") || packageName.equals("cf.playhi.freezeyou"))) {
                        int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                        if (tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER &&
                                tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                            Map<String, Object> keyValuePair = new HashMap<>();
                            icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                            if (icon != null) {
                                keyValuePair.put("Img", icon);
                            } else {
                                keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                            }
                            keyValuePair.put("Name", name);
                            keyValuePair.put("PackageName", packageName);
                            AppList.add(keyValuePair);
                        }
                    } else if ((i+1==size)&&(AppList.size()==0)){
                        addNotAvailablePair(getApplicationContext(),AppList);
                    }
                }
                break;
            case "OO":
                String[] pkgNameList = getApplicationContext().getSharedPreferences(
                        "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
                for (String aPkgNameList : pkgNameList) {
                    aPkgNameList = aPkgNameList.replaceAll("\\|","");
                    String name;
                    try{
                        name = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(aPkgNameList,0)).toString();
                    } catch (Exception e){
                        if (checkFrozen(getApplicationContext(),aPkgNameList)){
                            final SharedPreferences sharedPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(
                                    "pkgName2Name", Context.MODE_PRIVATE);
                            name = sharedPreferences.getString(aPkgNameList,getResources().getString(R.string.notice));
                        } else {
                            name = getResources().getString(R.string.uninstalled);
                        }
                    }
                    if (!(aPkgNameList.equals("android") || aPkgNameList.equals("cf.playhi.freezeyou") || aPkgNameList.equals(""))) {
                        Map<String, Object> keyValuePair = new HashMap<>();
                        try{
                            icon = getPackageManager().getApplicationIcon(getPackageManager().getApplicationInfo(aPkgNameList,0));
                        } catch (Exception e){
                            if (checkFrozen(getApplicationContext(),aPkgNameList)){
                                Bitmap bitmap = getBitmapFromLocalFile(getApplicationContext().getFilesDir()+"/icon/"+aPkgNameList+".png");
                                if (bitmap!=null){
                                    icon = new BitmapDrawable(bitmap);
                                } else {
                                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                                }
                            } else {
                                icon = getResources().getDrawable(android.R.drawable.ic_menu_delete);//ic_delete
                            }
                        }
                        keyValuePair.put("Img", icon);
                        int tmp;
                        try{
                            tmp = getPackageManager().getApplicationEnabledSetting(aPkgNameList);
                        } catch (Exception e){
                            tmp = -10086;
                        }
                        if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || checkFrozen(getApplicationContext(),aPkgNameList)) {
                            keyValuePair.put("Name", name + "(" + getString(R.string.frozen) + ")");
                        } else {
                            keyValuePair.put("Name", name);
                        }
                        keyValuePair.put("PackageName", aPkgNameList);
                        AppList.add(keyValuePair);
                    } else if (pkgNameList.length==1||pkgNameList.length==0){
                        addNotAvailablePair(getApplicationContext(),AppList);
                    }
                }
                break;
            case "OS":
                for (int i = 0; i < size; i++) {
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android") || packageName.equals("cf.playhi.freezeyou"))) {
                        Map<String, Object> keyValuePair = new HashMap<>();
                        icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                        if (icon != null) {
                            keyValuePair.put("Img", icon);
                        } else {
                            keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                        }
                        if ((applicationInfo.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
                            int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                            if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                                keyValuePair.put("Name", name + "(" + getString(R.string.frozen) + ")");
                            } else {
                                keyValuePair.put("Name", name);
                            }
                            keyValuePair.put("PackageName", packageName);
                            AppList.add(keyValuePair);
                        }
                    } else if ((i+1==size)&&(AppList.size()==0)){
                        addNotAvailablePair(getApplicationContext(),AppList);
                    }
                }
                break;
            case "OU":
                for (int i = 0; i < size; i++) {
                    String name = getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString();
                    String packageName = applicationInfo.get(i).packageName;
                    if (!(packageName.equals("android") || packageName.equals("cf.playhi.freezeyou"))) {
                        Map<String, Object> keyValuePair = new HashMap<>();
                        icon = getPackageManager().getApplicationIcon(applicationInfo.get(i));
                        if (icon != null) {
                            keyValuePair.put("Img", icon);
                        } else {
                            keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
                        }
                        if ((applicationInfo.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM){
                            int tmp = getPackageManager().getApplicationEnabledSetting(packageName);
                            if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                                keyValuePair.put("Name", name + "(" + getString(R.string.frozen) + ")");
                            } else {
                                keyValuePair.put("Name", name);
                            }
                            keyValuePair.put("PackageName", packageName);
                            AppList.add(keyValuePair);
                        }
                    } else if ((i+1==size)&&(AppList.size()==0)){
                        addNotAvailablePair(getApplicationContext(),AppList);
                    }
                }
                break;
            default:
                break;
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
                app_listView.setVisibility(View.VISIBLE);
            }
        });

        app_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                final String name=map.get("Name");
                final String pkgName=map.get("PackageName");
                if (!(name.equals(getString(R.string.notAvailable))||name.equals(getString(R.string.uninstalled)))){
                    int tmp = getPackageManager().getApplicationEnabledSetting(pkgName);
                    if (tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER||tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED||Support.checkFrozen(Main.this,pkgName)){
                        Support.makeDialog(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                    } else {
                        Support.makeDialog2(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                    }
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
                if (!name.equals(getString(R.string.notAvailable))) {
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
                                    try {
                                        createShortCut(
                                                name.replace("(" + getString(R.string.frozen) + ")", ""),
                                                pkgName,
                                                getPackageManager().getApplicationIcon(pkgName),
                                                Freeze.class,
                                                "FreezeYou! "+pkgName
                                        );
                                    } catch (PackageManager.NameNotFoundException e) {
                                        showToast(getApplicationContext(), R.string.cannotFindApp);
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
                                                    Uri uri = Uri.fromParts("package", pkgName, null);
                                                    intent.setData(uri);
                                                    try {
                                                        startActivity(intent);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        showToast(getApplicationContext(), e.getLocalizedMessage());
                                                    }
                                                }
                                            });
                                    final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                                            "AutoFreezeApplicationList", Context.MODE_PRIVATE);
                                    final String pkgNameList = sharedPreferences.getString("pkgName", "");
                                    if (pkgNameList.contains("|" + pkgName + "|")) {
                                        dialog.setPositiveButton(R.string.removeFromOneKeyList, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int ii) {
                                                if (sharedPreferences.edit()
                                                        .putString(
                                                                "pkgName",
                                                                pkgNameList.replace("|" + pkgName + "|", ""))
                                                        .commit()) {
                                                    showToast(getApplicationContext(), R.string.removed);
                                                } else {
                                                    showToast(getApplicationContext(), R.string.removeFailed);
                                                }
                                            }
                                        });
                                    } else {
                                        dialog.setPositiveButton(R.string.addToOneKeyList, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int ii) {
                                                if (sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit()) {
                                                    showToast(getApplicationContext(), R.string.added);
                                                } else {
                                                    showToast(getApplicationContext(), R.string.addFailed);
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
            }
        });
    }

    private static void addMRootApplications(Context context,List<Map<String, Object>> AppList){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "FrozenList", Context.MODE_PRIVATE);
        final String pkgNameList = sharedPreferences.getString("pkgName", "");
        String[] pkgNameListKeyValuePair = pkgNameList.split("\\|");
        final SharedPreferences pkgName2NameSharedPreferences = context.getApplicationContext().getSharedPreferences(
                "pkgName2Name", Context.MODE_PRIVATE);
        for (String aPkgNameListKeyValuePair : pkgNameListKeyValuePair) {
            if (!aPkgNameListKeyValuePair.equals("")){
                Map<String, Object> keyValuePair = new HashMap<>();
                Bitmap bitmap = getBitmapFromLocalFile(context.getFilesDir()+"/icon/"+aPkgNameListKeyValuePair+".png");
                if (bitmap!=null){
                    keyValuePair.put("Img", new BitmapDrawable(bitmap));
                } else {
                    keyValuePair.put("Img", R.mipmap.ic_launcher_round);
                }
                keyValuePair.put("Name",
                        pkgName2NameSharedPreferences.getString(
                                aPkgNameListKeyValuePair,
                                context.getString(R.string.notAvailable)) + "(" + context.getString(R.string.frozen)+")");
                keyValuePair.put("PackageName", aPkgNameListKeyValuePair);
                AppList.add(keyValuePair);
            }
        }
    }

    private static void addNotAvailablePair(Context context,List<Map<String,Object>> AppList){
        Map<String, Object> keyValuePair = new HashMap<>();
        keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
        keyValuePair.put("Name", context.getString(R.string.notAvailable));
        keyValuePair.put("PackageName", context.getString(R.string.notAvailable));
        AppList.add(keyValuePair);
    }
}
