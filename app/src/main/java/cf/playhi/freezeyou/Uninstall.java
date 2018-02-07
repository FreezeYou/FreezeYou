package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import static cf.playhi.freezeyou.Support.showToast;

public class Uninstall extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Support.buildAlertDialog(this,getResources().getDrawable(R.mipmap.ic_launcher_round),"真的要解除免ROOT吗？\n!为避免造成不必要的麻烦，解除前请先解冻所有已冻结的程序！","请确认")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Support.isDeviceOwner(getApplicationContext())){
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Support.getDevicePolicyManager(getApplicationContext()).clearDeviceOwnerApp("cf.playhi.freezeyou");
                                    showToast(getApplicationContext(),"Success");
                                } else {
                                    showToast(getApplicationContext(),"现在还没有成功启用免ROOT呢！");
                                }
                            } catch (Exception e){
                                showToast(getApplicationContext(),"Failed");
                            }
                        } else {
                            showToast(getApplicationContext(),"现在还没有成功启用免ROOT呢！");
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }
}
