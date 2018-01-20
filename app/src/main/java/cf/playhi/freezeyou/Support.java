package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
}
