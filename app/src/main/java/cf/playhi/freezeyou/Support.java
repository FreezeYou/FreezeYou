package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.DataOutputStream;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

class Support {
    private static Process process = null;
    private static DataOutputStream outputStream = null;
    static Drawable drawable;
    private static void makeDialog(final String title, String message, final Activity activity, final Boolean SelfCloseWhenDestroyProcess, final ApplicationInfo applicationInfo, final String pkgName){
        buildAlertDialog(activity,getApplicationIcon(activity,pkgName,applicationInfo,true),message,title)
                .setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processFreezeAction(activity,activity,pkgName,applicationInfo,SelfCloseWhenDestroyProcess);
                    }
                })
                .setPositiveButton(R.string.unfreeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processUnfreezeAction(activity,activity,pkgName,applicationInfo,SelfCloseWhenDestroyProcess);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                    }
                })
                .create().show();
    }

    private static void makeDialog2(String title, String message, final Activity activity, final Boolean selfCloseWhenDestroyProcess,final ApplicationInfo applicationInfo,final String pkgName){
        buildAlertDialog(activity,getApplicationIcon(activity,pkgName,applicationInfo,true),message,title)
                .setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processFreezeAction(activity,activity,pkgName,applicationInfo,selfCloseWhenDestroyProcess);
                    }
                })
                .setPositiveButton(R.string.launch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkAndStartApp(activity,activity,pkgName,selfCloseWhenDestroyProcess);
//                        if (activity.getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
//                            Intent intent = new Intent(
//                                    activity.getPackageManager().getLaunchIntentForPackage(pkgName));
//                            activity.startActivity(intent);
//                            destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
//                        } else {
//                            showToast(activity,
//                                    R.string.cannotFindTheLaunchIntent);
//                        }
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                    }
                })
                .create().show();
    }

    private static void destroyProcess(Boolean finish, DataOutputStream dataOutputStream, Process process1, Activity activity){
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (process1 != null){
                process1.destroy();
            }
            if (finish){
                activity.finish();
            }
        } catch (Exception e) {
            if (finish){
                activity.finish();
            }
        }
    }

    static void showToast(Context context,int id){
        Toast.makeText(context,id,Toast.LENGTH_LONG).show();
    }

    static void showToast(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_LONG).show();
    }

    static AlertDialog.Builder buildAlertDialog(Context context,int icon,int message,int title){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    static AlertDialog.Builder buildAlertDialog(Context context, Drawable icon, CharSequence message,CharSequence title){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    /****************
     *
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    static boolean joinQQGroup(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D92NGzlhmCK_UFrL_oEAV7Fe6QrvFR5y_"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Uri webPage = Uri.parse("https://shang.qq.com/wpa/qunwpa?idkey=cbc8ae71402e8a1bc9bb4c39384bcfe5b9f7d18ff1548ea9bdd842f036832f3d");
            Intent intent1 = new Intent(Intent.ACTION_VIEW, webPage);
            if (intent1.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent1);
                return true;
            } else {
                return false;
            }
//            // 未安装手Q或安装的版本不支持
//            return false;
        }
    }

    static boolean isDeviceOwner(Context context) {
        return Build.VERSION.SDK_INT >= 18 && getDevicePolicyManager(context).isDeviceOwnerApp(context.getPackageName());
    }

    static DevicePolicyManager getDevicePolicyManager(Context context){
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

//    static void addFrozen(Context context,String pkgName){
//        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
//                "FrozenList", Context.MODE_PRIVATE);
//        final String pkgNameList = sharedPreferences.getString("pkgName", "");
//        if (!sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit()) {
//            if (!sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit()) {
//                showToast(context.getApplicationContext(), "数据异常");
//            }
//        }
//    }

//    static void removeFrozen(Context context,String pkgName){
//        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
//                "FrozenList", Context.MODE_PRIVATE);
//        final String pkgNameList = sharedPreferences.getString("pkgName", "");
//        if (!sharedPreferences.edit().putString("pkgName", pkgNameList.replace("|" + pkgName + "|", "")).commit()) {
//            if (!sharedPreferences.edit().putString("pkgName", pkgNameList.replace("|" + pkgName + "|", "")).commit()) {
//                showToast(context.getApplicationContext(), "数据异常");
//            }
//        }
//    }

    static boolean checkMRootFrozen(Context context,String pkgName) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isDeviceOwner(context) && getDevicePolicyManager(context).isApplicationHidden(DeviceAdminReceiver.getComponentName(context), pkgName);
    }

    static boolean checkRootFrozen(Context context,String pkgName) {
        int tmp;
        try {
            tmp = context.getPackageManager().getApplicationEnabledSetting(pkgName);
        } catch (Exception e){
            tmp = -1;
        }
        return ((tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER )|| (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED));
    }

    private static void askRun(final Activity activity, final Boolean SelfCloseWhenDestroyProcess, final String pkgName,final ApplicationInfo applicationInfo){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if ((sharedPref.getBoolean("openImmediately",false))||(sharedPref.getBoolean("openAndUFImmediately",false))){
            checkAndStartApp(activity,activity,pkgName,SelfCloseWhenDestroyProcess);
        } else {
            buildAlertDialog(activity,getApplicationIcon(activity,pkgName,applicationInfo,true),activity.getResources().getString(R.string.unfreezedAndAskLaunch),activity.getResources().getString(R.string.notice))
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                        }
                    })
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int ii) {
                            checkAndStartApp(activity,activity,pkgName,SelfCloseWhenDestroyProcess);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                        }
                    })
                    .create().show();
        }
    }

    @TargetApi(21)
    static void shortcutMakeDialog(String title, String message, final Activity activity, final Boolean selfCloseWhenDestroyProcess, final ApplicationInfo applicationInfo, final String pkgName, int ot, boolean auto){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getBoolean("openAndUFImmediately",false) && auto){
            if (ot==2){
                checkAndStartApp(activity,activity,pkgName,selfCloseWhenDestroyProcess);
            } else {
                processUnfreezeAction(activity,activity,pkgName,applicationInfo,selfCloseWhenDestroyProcess);
            }
        } else {
            if (ot==2){
                makeDialog2(title,message,activity,selfCloseWhenDestroyProcess,applicationInfo,pkgName);
            } else {
                makeDialog(title,message,activity,selfCloseWhenDestroyProcess,applicationInfo,pkgName);
            }
        }
    }

//    /**
//     * 图片保存文件：
//     * 从Browser项目搬来的代码
//     * @param filePath filePath
//     * @param b bitmap
//     * @param quality quality
//     */
//    private static void writeBitmapToFile(String filePath, Bitmap b, int quality) {
//        try {
//            File desFile = new File(filePath);
//            FileOutputStream fos = new FileOutputStream(desFile);
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//            b.compress(Bitmap.CompressFormat.PNG, quality, bos);
//            bos.flush();
//            bos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //最初参考 http://www.cnblogs.com/zhou2016/p/6281678.html
    /**
     * Drawable转Bitmap
     *
     * @param drawable drawable
     * @return Bitmap
     */
    private static Bitmap getBitmapFromDrawable(Drawable drawable) {
        try {
            return ((BitmapDrawable)drawable).getBitmap();
        }catch (Exception e) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    static Drawable getApplicationIcon(Context context, String pkgName, ApplicationInfo applicationInfo,boolean resize){
        drawable = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        if (applicationInfo!=null){
            drawable = context.getPackageManager().getApplicationIcon(applicationInfo);
        } else if (!"".equals(pkgName)){
            try {
                drawable = context.getPackageManager().getApplicationIcon(pkgName);
            } catch (Exception e){
                drawable = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
            }
        }
        if ((drawable == null)||(drawable.getIntrinsicWidth()<=0)||(drawable.getIntrinsicHeight()<=0)){
            drawable = context.getResources().getDrawable(R.mipmap.ic_launcher_round);
        }
        if (resize){
            int width = drawable.getIntrinsicWidth();
            int height= drawable.getIntrinsicHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float)72 / width);
            float scaleHeight = ((float)72 / height);
            matrix.postScale(scaleWidth, scaleHeight);
            return new BitmapDrawable(Bitmap.createBitmap(getBitmapFromDrawable(drawable), 0, 0, width, height, matrix, true));
        } else {
            return new BitmapDrawable(Bitmap.createBitmap(getBitmapFromDrawable(drawable)));
        }
    }

    private static int fAURoot(String pkgName, Boolean enable) throws Exception{
        process = Runtime.getRuntime().exec("su");
        outputStream = new DataOutputStream(process.getOutputStream());
        if (enable){
            outputStream.writeBytes("pm enable " + pkgName + "\n");
        } else {
            outputStream.writeBytes("pm disable " + pkgName + "\n");
        }
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        return process.waitFor();
    }

    static void createShortCut(String title, String pkgName, Drawable icon,Class<?> cls,String id,Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            createShortCutOldApi(title,pkgName,icon,cls,context);
        } else {
            ShortcutManager mShortcutManager =
                    context.getSystemService(ShortcutManager.class);
            if (mShortcutManager!=null){
                if (mShortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo.Builder shortcutInfoBuilder =
                            new ShortcutInfo.Builder(context, id);
                    shortcutInfoBuilder.setIcon(Icon.createWithBitmap(getBitmapFromDrawable(icon)));
                    shortcutInfoBuilder.setIntent(
                            new Intent(context, cls)
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
                    PendingIntent successCallback = PendingIntent.getBroadcast(context, 0,
                            pinnedShortcutCallbackIntent, 0);

                    mShortcutManager.requestPinShortcut(pinShortcutInfo,
                            successCallback.getIntentSender());
                } else {
                    createShortCutOldApi(title,pkgName,icon,cls,context);
                }
            } else {
                createShortCutOldApi(title,pkgName,icon,cls,context);
            }
        }
    }

    private static void createShortCutOldApi(String title, String pkgName, Drawable icon,Class<?> cls,Context context){
        try {
            Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//            Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
            Intent intent = new Intent(context, cls);
            intent.putExtra("pkgName", pkgName);
            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            BitmapDrawable bd = (BitmapDrawable) icon;
            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            context.sendBroadcast(addShortCut);
            showToast(context, R.string.requested);
        } catch (Exception e){
            Intent addShortCut2 = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Intent intent2 = new Intent(context, cls);
            intent2.putExtra("pkgName",pkgName);
            addShortCut2.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            addShortCut2.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(getBitmapFromDrawable(icon),192,192,true));
            addShortCut2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2);
            try {
                context.sendBroadcast(addShortCut2);
                showToast(context, R.string.requested);
            }catch (Exception ee){
                showToast(context,context.getString(R.string.requestFailed)+ee.getMessage());
            }
        }
    }

    private static void createNotification(Context context,String pkgName,int iconResId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            Notification.Builder mBuilder = new Notification.Builder(context);
            int mId = pkgName.hashCode();
            mBuilder.setSmallIcon(iconResId);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_notification));
            try {
                mBuilder.setContentTitle(context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkgName,GET_UNINSTALLED_PACKAGES)).toString());
            } catch (Exception e){
                mBuilder.setContentTitle(context.getString(R.string.notice));
            }
            mBuilder.setContentText(context.getString(R.string.disableAEnable));
            mBuilder.setAutoCancel(true);
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String CHANNEL_ID = "FAUf";
                CharSequence name = context.getString(R.string.disableAEnable);
                String description = context.getString(R.string.disableAEnable);
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager!=null)
                    notificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId(CHANNEL_ID);
            }
            // Create an Intent for the activity you want to start
            Intent resultIntent = new Intent(context, Freeze.class).putExtra("pkgName",pkgName).putExtra("auto",false);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(mId, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                    "notificationId", Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(pkgName, mId).apply();
            if (mNotificationManager!=null){
                // mId allows you to update the notification later on.
                mNotificationManager.notify(mId, mBuilder.build());
            }
        }
    }

    private static void deleteNotification(Context context,String pkgName){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "notificationId", Context.MODE_PRIVATE);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager!=null){
            mNotificationManager.cancel(sharedPreferences.getInt(pkgName,0));
        }
    }
//
//    static String getVersionName(Context context) {
//        PackageManager packageManager = context.getPackageManager();
//        String packageName = context.getPackageName();
//        int flags = 0;
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = packageManager.getPackageInfo(packageName, flags);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (packageInfo != null) {
//            return packageInfo.versionName;
//        }
//        return "";
//    }

    static void processRootAction(final String pkgName,final Context context,final Activity activity,final boolean enable,final boolean SelfCloseWhenDestroyProcess,final ApplicationInfo applicationInfo){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int exitValue = fAURoot(pkgName,enable);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (exitValue == 0) {
                                if (enable){
                                    askRun(activity, SelfCloseWhenDestroyProcess, pkgName,applicationInfo);
                                    createNotification(context, pkgName, R.drawable.ic_notification);
                                } else {
                                    showToast(context, R.string.executed);
                                    deleteNotification(context,pkgName);
                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                }
                            } else {
                                showToast(context, R.string.mayUnrootedOrOtherEx);
                                destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                            }

                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            showToast(context, context.getString(R.string.exception) + e.getMessage());
                            if (e.getMessage().toLowerCase().contains("permission denied")||e.getMessage().toLowerCase().contains("not found")) {
                                showToast(context, R.string.mayUnrooted);
                            }
                            destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);

                        }
                    });
                }
            }
        }).start();
    }

    @TargetApi(21)
    private static void processMRootAction(Context context,Activity activity,String pkgName,boolean hidden,ApplicationInfo applicationInfo,boolean finish){
        if (getDevicePolicyManager(context).setApplicationHidden(
                DeviceAdminReceiver.getComponentName(context), pkgName, hidden)) {
            if (hidden){
                deleteNotification(context,pkgName);
                activity.finish();
            } else {
                askRun(activity, finish, pkgName,applicationInfo);
                createNotification(activity,pkgName,R.drawable.ic_notification);
            }
        } else {
            showToast(context, "Failed!");
            if (finish){
                activity.finish();
            }
        }
    }

    private static void checkAndStartApp(Context context,Activity activity,String pkgName,boolean finish){
        if (context.getPackageManager().getLaunchIntentForPackage(pkgName) != null) {
            Intent intent = new Intent(
                    context.getPackageManager().getLaunchIntentForPackage(pkgName));
            activity.startActivity(intent);
            destroyProcess(finish, outputStream, process, activity);
        } else {
            showToast(context,
                    R.string.unrootedOrCannotFindTheLaunchIntent);
            destroyProcess(finish, outputStream, process, activity);
        }
    }

    private static void processUnfreezeAction(Context context,Activity activity,String pkgName,ApplicationInfo applicationInfo,boolean finish){
        if (checkMRootFrozen(context,pkgName)) {
            processMRootAction(context,activity,pkgName,false,applicationInfo,finish);
        } else {
            processRootAction(pkgName,context,activity,true,finish,applicationInfo);
        }
    }

    private static void processFreezeAction(Context context,Activity activity,String pkgName,ApplicationInfo applicationInfo,boolean finish){
        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(activity)) {
            processMRootAction(context,activity,pkgName,true,applicationInfo,finish);
        } else {
            processRootAction(pkgName,context,activity,false,finish,applicationInfo);
        }
    }

    static ApplicationInfo getApplicationInfoFromPkgName(String pkgName,Context context){
        ApplicationInfo applicationInfo = null;
        try{
            applicationInfo = context.getPackageManager().getApplicationInfo(pkgName,GET_UNINSTALLED_PACKAGES);
        }catch (Exception e){
            e.printStackTrace();
        }
        return applicationInfo;
    }

    static void oneKeyActionRoot(Context context,Activity activity,boolean freeze,String[] pkgNameList){
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(process.getOutputStream());
            if (freeze){
                for (String aPkgNameList : pkgNameList) {
                    int tmp = context.getPackageManager().getApplicationEnabledSetting(aPkgNameList.replaceAll("\\|", ""));
                    if (tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER && tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                        outputStream.writeBytes(
                                "pm disable " + aPkgNameList.replaceAll("\\|", "") + "\n");
                    }
                }
            } else {
                for (String aPkgNameList : pkgNameList) {
                    int tmp = context.getPackageManager().getApplicationEnabledSetting(aPkgNameList.replaceAll("\\|", ""));
                    if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                        outputStream.writeBytes(
                                "pm enable " + aPkgNameList.replaceAll("\\|", "") + "\n");
                    }
                }
            }
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                if (freeze){
                    for (String aPkgNameList : pkgNameList) {
                        deleteNotification(activity,aPkgNameList.replaceAll("\\|", ""));
                    }
                } else {
                    for (String aPkgNameList : pkgNameList) {
                        createNotification(context,aPkgNameList.replaceAll("\\|", ""),R.drawable.ic_notification);
                    }
                }
                showToast(context,R.string.executed);
            } else {
                showToast(context,R.string.mayUnrootedOrOtherEx);
            }
            destroyProcess(true,outputStream,process,activity);
        } catch (Exception e){
            e.printStackTrace();
            showToast(activity,context.getString(R.string.exception) + e.getMessage());
            if (e.getMessage().toLowerCase().contains("permission denied")||e.getMessage().toLowerCase().contains("not found")){
                showToast(activity,R.string.mayUnrooted);
            }
            destroyProcess(true,outputStream,process,activity);
        }
    }

    @TargetApi(21)
    static void oneKeyActionMRoot(Context context,Activity activity,boolean freeze,String[] pkgNameList){
        for (String aPkgNameList : pkgNameList) {
            String tmp = aPkgNameList.replaceAll("\\|", "");
            try {
                if (freeze){
                    if (!checkMRootFrozen(activity, tmp)) {
                        if (!getDevicePolicyManager(activity).setApplicationHidden(
                                DeviceAdminReceiver.getComponentName(activity), tmp, true)) {
                            showToast(activity, tmp + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                        } else {
                            deleteNotification(activity,tmp);
                        }
                    }
                } else {
                    if (checkMRootFrozen(activity, tmp)) {
                        if (!getDevicePolicyManager(activity).setApplicationHidden(
                                DeviceAdminReceiver.getComponentName(activity), tmp, false)) {
                            showToast(activity, tmp + " " + context.getString(R.string.failed) + " " + context.getString(R.string.mayUnrootedOrOtherEx));
                        } else {
                            createNotification(activity,tmp,R.drawable.ic_notification);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(activity, "发生了点异常，操作仍将继续:" + e.getLocalizedMessage());
            }
        }
        showToast(activity,R.string.executed);
    }
}
