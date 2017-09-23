package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.DataOutputStream;

public class Freeze extends Activity{

    DataOutputStream outputStream = null;
    java.lang.Process process = null;
    String backData = "backData";
    String pkgName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName.equals("unknown")){
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            Freeze.this.finish();
        }
        if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                makeDialog2(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),"请选择具体操作");
            }catch (Exception e){
                e.printStackTrace();
                makeDialog2("提示","请选择具体操作");
            }
        } else {
            try{
                makeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),"请选择具体操作");
            }catch (Exception e){
                e.printStackTrace();
                makeDialog("提示","请选择具体操作");
            }
        }
    }

    private void makeDialog(String title,String message){
        AlertDialog alertDialog = new AlertDialog.Builder(Freeze.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("冻结", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                process = Runtime.getRuntime().exec("su");
//                                BufferedReader in = new BufferedReader(
//                                        new InputStreamReader(process.getInputStream()));
//                                String line = in.readLine();
//                                while (line!=null) {
//                                    while(line.contains("Permission denied")){
//                                        Toast.makeText(getApplicationContext(),"该功能需要ROOT权限",Toast.LENGTH_LONG).show();
//                                        destroyProcess(false);
//                                        makeDialog("提示","请选择具体操作");
//                                    }
//                                }
//                                in.close();
//
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm disable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                process.waitFor();
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(true);
                            }
                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
                            destroyProcess(true);
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
                                process.waitFor();
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(true);
                            }
                            AlertDialog alertDialog = new AlertDialog.Builder(Freeze.this)
                                    .setTitle("提示")
                                    .setMessage("已要求解冻，立即尝试启动？")
                                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            destroyProcess(true);
                                        }
                                    })
                                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int ii) {
                                            if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                                                Intent intent = new Intent(
                                                        getPackageManager().getLaunchIntentForPackage(pkgName));
                                                startActivity(intent);
                                                destroyProcess(true);
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "未找到程序入口或由于未获得ROOT权限导致解冻失败",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialogInterface) {
                                            destroyProcess(true);
                                        }
                                    })
                                    .create();
                            alertDialog.show();
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(true);
                    }
                })
                .create();
        alertDialog.show();
    }

    private void makeDialog2(String title,String message){
        AlertDialog alertDialog = new AlertDialog.Builder(Freeze.this)
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
                                process.waitFor();
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(true);
                            }
                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
                            destroyProcess(true);
                        }
                    }
                })
                .setPositiveButton("启动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
                            Intent intent = new Intent(
                                    getPackageManager().getLaunchIntentForPackage(pkgName));
                            startActivity(intent);
                            destroyProcess(true);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "未找到程序入口",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(true);
                    }
                })
                .create();
        alertDialog.show();
    }

    private void destroyProcess(Boolean finish){
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            process.destroy();
            if (finish){
                Freeze.this.finish();
            }
        } catch (Exception e) {
            if (finish){
                Freeze.this.finish();
            }
        }
    }
}
