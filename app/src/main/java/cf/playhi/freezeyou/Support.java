package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import java.io.DataOutputStream;

class Support {
    private static Process process = null;
    private static DataOutputStream outputStream = null;
    static void makeDialog(String title, String message, final Activity activity, final Boolean SelfCloseWhenDestroyProcess,final String backData,final String pkgName){
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                process = Runtime.getRuntime().exec("su");
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm disable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                int exitValue = process.waitFor();
                                if (exitValue == 0) {
                                    Toast.makeText(activity,R.string.executed,Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(activity,R.string.mayUnrootedOrOtherEx,Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,activity.getString(R.string.exception)+e.getMessage(),Toast.LENGTH_LONG).show();
                                if (e.getMessage().contains("Permission denied")){
                                    Toast.makeText(activity,R.string.mayUnrooted,Toast.LENGTH_SHORT).show();
                                }
                                destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                            }
                            destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                        }
                    }
                })
                .setPositiveButton(R.string.unfreeze, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                process = Runtime.getRuntime().exec("su");
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm enable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                int exitValue = process.waitFor();
                                if (exitValue == 0) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            .setTitle(R.string.notice)
                                            .setMessage(R.string.unfreezedAndAskLaunch)
                                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                                }
                                            })
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int ii) {
                                                    if (activity.getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                                                        Intent intent = new Intent(
                                                                activity.getPackageManager().getLaunchIntentForPackage(pkgName));
                                                        activity.startActivity(intent);
                                                        destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                                    } else {
                                                        Toast.makeText(activity,
                                                                R.string.unrootedOrCannotFindTheLaunchIntent,
                                                                Toast.LENGTH_LONG).show();
                                                    }
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
                                } else {
                                    Toast.makeText(activity,R.string.mayUnrootedOrOtherEx,Toast.LENGTH_LONG).show();
                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,activity.getString(R.string.exception)+e.getMessage(),Toast.LENGTH_LONG).show();
                                if (e.getMessage().contains("Permission denied")){
                                    Toast.makeText(activity,R.string.mayUnrooted,Toast.LENGTH_SHORT).show();
                                }
                                destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
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
                            try {
                                process = Runtime.getRuntime().exec("su");
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm disable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                int exitValue = process.waitFor();
                                if (exitValue == 0) {
                                    Toast.makeText(activity,R.string.executed,Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(activity,R.string.mayUnrootedOrOtherEx,Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,activity.getString(R.string.exception)+e.getMessage(),Toast.LENGTH_LONG).show();
                                if (e.getMessage().contains("Permission denied")){
                                    Toast.makeText(activity,R.string.mayUnrooted,Toast.LENGTH_SHORT).show();
                                }
                                destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
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

    static void makeToast(Context context,int id){
        Toast.makeText(context,id,Toast.LENGTH_LONG).show();
    }

    static void makeToast(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_LONG).show();
    }

    static AlertDialog.Builder buildAlertDialog(Activity activity,int icon,int message,int title){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    static AlertDialog.Builder buildAlertDialog(Activity activity, Drawable icon, CharSequence message,CharSequence title){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

}
