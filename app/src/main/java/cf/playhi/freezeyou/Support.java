package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class Support {
    private static Process process = null;
    private static DataOutputStream outputStream = null;
    static void makeDialog(final String title, String message, final Activity activity, final Boolean SelfCloseWhenDestroyProcess, final String backData, final String pkgName){
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
                                savePkgName2Name(activity,pkgName);
                                if (getDevicePolicyManager(activity).setApplicationHidden(
                                        DeviceAdminReceiver.getComponentName(activity),pkgName,true)){
                                    addFrozen(activity,pkgName);
                                } else {
                                    showToast(activity, "Failed!");
                                }
                            } else {
                                try {
                                    process = Runtime.getRuntime().exec("su");
                                    outputStream = new DataOutputStream(process.getOutputStream());
                                    outputStream.writeBytes("pm disable " + pkgName + "\n");
                                    outputStream.writeBytes("exit\n");
                                    outputStream.flush();
                                    int exitValue = process.waitFor();
                                    if (exitValue == 0) {
                                        showToast(activity, R.string.executed);
                                    } else {
                                        showToast(activity, R.string.mayUnrootedOrOtherEx);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast(activity, activity.getString(R.string.exception) + e.getMessage());
                                    if (e.getMessage().contains("Permission denied")) {
                                        showToast(activity, R.string.mayUnrooted);
                                    }
                                    destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                                }
                            }
                            destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                        }
                    }
                })
                .setPositiveButton(R.string.unfreeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            final SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(
                                    "FrozenList", Context.MODE_PRIVATE);
                            final String pkgNameList = sharedPreferences.getString("pkgName", "");
                            if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity) && (title.equals(activity.getString(R.string.notice))||pkgNameList.contains("|" + pkgName + "|"))){
                                if (getDevicePolicyManager(activity).setApplicationHidden(
                                        DeviceAdminReceiver.getComponentName(activity),pkgName,false)){
                                    removeFrozen(activity,pkgName);
                                    askRun(activity,SelfCloseWhenDestroyProcess,pkgName);
                                } else {
                                    showToast(activity, "Failed!");
                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                }
                            } else {
                                try {
                                    process = Runtime.getRuntime().exec("su");
                                    outputStream = new DataOutputStream(process.getOutputStream());
                                    outputStream.writeBytes("pm enable " + pkgName + "\n");
                                    outputStream.writeBytes("exit\n");
                                    outputStream.flush();
                                    int exitValue = process.waitFor();
                                    if (exitValue == 0) {
                                        askRun(activity,SelfCloseWhenDestroyProcess,pkgName);
                                    } else {
                                        showToast(activity, R.string.mayUnrootedOrOtherEx);
                                        destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast(activity, activity.getString(R.string.exception) + e.getMessage());
                                    if (e.getMessage().contains("Permission denied")) {
                                        showToast(activity, R.string.mayUnrooted);
                                    }
                                    destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                                }
                            }
                        }
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
                .create();
        alertDialog.show();
    }

    static void makeDialog2(String title, String message, final Activity activity, final Boolean selfCloseWhenDestroyProcess,final String backData,final String pkgName){
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
                                savePkgName2Name(activity,pkgName);
                                if (getDevicePolicyManager(activity).setApplicationHidden(
                                        DeviceAdminReceiver.getComponentName(activity),pkgName,true)){
                                    addFrozen(activity,pkgName);
                                } else {
                                    showToast(activity, "Failed!");
                                }
                            } else {
                                try {
                                    process = Runtime.getRuntime().exec("su");
                                    outputStream = new DataOutputStream(process.getOutputStream());
                                    outputStream.writeBytes("pm disable " + pkgName + "\n");
                                    outputStream.writeBytes("exit\n");
                                    outputStream.flush();
                                    int exitValue = process.waitFor();
                                    if (exitValue == 0) {
                                        showToast(activity,R.string.executed);
                                    } else {
                                        showToast(activity,R.string.mayUnrootedOrOtherEx);
                                    }
                                } catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(activity,activity.getString(R.string.exception)+e.getMessage(),Toast.LENGTH_LONG).show();
                                    if (e.getMessage().contains("Permission denied")){
                                        showToast(activity,R.string.mayUnrooted);
                                    }
                                    destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                                }
                            }
                            destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                        }
                    }
                })
                .setPositiveButton(R.string.launch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity.getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                            Intent intent = new Intent(
                                    activity.getPackageManager().getLaunchIntentForPackage(pkgName));
                            activity.startActivity(intent);
                            destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                        } else {
                            Toast.makeText(activity,
                                    R.string.cannotFindTheLaunchIntent,
                                    Toast.LENGTH_LONG).show();
                        }
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
                .create();
        alertDialog.show();
    }

    static void destroyProcess(Boolean finish, DataOutputStream dataOutputStream, Process process1, Activity activity){
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            process1.destroy();
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
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
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

    private static void addFrozen(Context context,String pkgName){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "FrozenList", Context.MODE_PRIVATE);
        final String pkgNameList = sharedPreferences.getString("pkgName", "");
        if (!sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit()) {
            if (!sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit()) {
                showToast(context.getApplicationContext(), "数据异常");
            }
        }
    }

    private static void removeFrozen(Context context,String pkgName){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "FrozenList", Context.MODE_PRIVATE);
        final String pkgNameList = sharedPreferences.getString("pkgName", "");
        if (!sharedPreferences.edit().putString("pkgName", pkgNameList.replace("|" + pkgName + "|", "")).commit()) {
            if (!sharedPreferences.edit().putString("pkgName", pkgNameList.replace("|" + pkgName + "|", "")).commit()) {
                showToast(context.getApplicationContext(), "数据异常");
            }
        }
    }

    static boolean checkFrozen(Context context,String pkgName){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "FrozenList", Context.MODE_PRIVATE);
        final String pkgNameList = sharedPreferences.getString("pkgName", "");
        if (pkgNameList.contains("|"+pkgName+"|")){
            return true;
        }
        return false;
    }

    private static void askRun(final Activity activity, final Boolean SelfCloseWhenDestroyProcess, final String pkgName){
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.notice)
                .setMessage(R.string.unfreezedAndAskLaunch)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int ii) {
                        if (activity.getPackageManager().getLaunchIntentForPackage(pkgName) != null) {
                            Intent intent = new Intent(
                                    activity.getPackageManager().getLaunchIntentForPackage(pkgName));
                            activity.startActivity(intent);
                            destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                        } else {
                            Toast.makeText(activity,
                                    R.string.unrootedOrCannotFindTheLaunchIntent,
                                    Toast.LENGTH_LONG).show();
                            destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(SelfCloseWhenDestroyProcess, outputStream, process, activity);
                    }
                })
                .create();
        alertDialog.show();
    }

    private static void savePkgName2Name(Context context,String pkgName){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
                "pkgName2Name", Context.MODE_PRIVATE);
        String name = context.getString(R.string.notice);
        PackageManager packageManager = context.getPackageManager();
        try{
            name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName,0)).toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        sharedPreferences.edit().putString(pkgName, name).commit();
        try{
            realFolderCheck(context.getFilesDir()+"/icon");
            writeBitmapToFile(context.getFilesDir()+"/icon/"+pkgName+".png",drawableToBitmap(packageManager.getApplicationIcon(pkgName)),100);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 图片保存文件：
     * 从Browser项目搬来的代码
     * @param filePath filePath
     * @param b bitmap
     * @param quality quality
     */
    private static void writeBitmapToFile(String filePath, Bitmap b, int quality) {
        try {
            File desFile = new File(filePath);
            FileOutputStream fos = new FileOutputStream(desFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.PNG, quality, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //http://www.cnblogs.com/zhou2016/p/6281678.html
    /**
     * Drawable转Bitmap
     *
     * @param drawable drawable
     * @return Bitmap
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    static Bitmap getBitmapFromLocalFile(String path){
        return BitmapFactory.decodeFile(path);
    }

    static void realFolderCheck(String path) {
        //检测文件夹是否存在
        File file = new File(path);
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() +
                        " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " +
                        file.getAbsolutePath());
            }
        }
    }
//
//    static int getVersionCode(Context context) {
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
//            return packageInfo.versionCode;
//        }
//        return 0;
//    }
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
}
