package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
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
                .setNegativeButton("冻结", new DialogInterface.OnClickListener() {
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
                                    Toast.makeText(activity,"执行完成",Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(activity,"似乎没有获得ROOT权限，或发生了其他异常",Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                            }
                            destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                        }
                    }
                })
                .setPositiveButton("解冻", new DialogInterface.OnClickListener() {
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
                                            .setTitle("提示")
                                            .setMessage("已要求解冻，立即尝试启动？")
                                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                                }
                                            })
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int ii) {
                                                    if (activity.getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                                                        Intent intent = new Intent(
                                                                activity.getPackageManager().getLaunchIntentForPackage(pkgName));
                                                        activity.startActivity(intent);
                                                        destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                                    } else {
                                                        Toast.makeText(activity,
                                                                "未找到程序入口或由于未获得ROOT权限导致解冻失败",
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
                                    Toast.makeText(activity,"似乎没有获得ROOT权限，或发生了其他异常",Toast.LENGTH_LONG).show();
                                    destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(SelfCloseWhenDestroyProcess,outputStream,process,activity);
                            }
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
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
                .setNegativeButton("冻结", new DialogInterface.OnClickListener() {
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
                                    Toast.makeText(activity,"执行完成",Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(activity,"似乎没有获得ROOT权限，或发生了其他异常",Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(activity,"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                            }
                            destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                        }
                    }
                })
                .setPositiveButton("启动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity.getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                            Intent intent = new Intent(
                                    activity.getPackageManager().getLaunchIntentForPackage(pkgName));
                            activity.startActivity(intent);
                            destroyProcess(selfCloseWhenDestroyProcess,outputStream,process,activity);
                        } else {
                            Toast.makeText(activity,
                                    "未找到程序入口",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
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

    private static void destroyProcess(Boolean finish, DataOutputStream dataOutputStream, Process process1, Activity activity){
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
}
