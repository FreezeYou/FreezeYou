package cf.playhi.freezeyou;

import android.support.annotation.Nullable;

import java.io.DataOutputStream;

final class ProcessUtils {

    static void destroyProcess(@Nullable DataOutputStream dataOutputStream, @Nullable Process process1) {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (process1 != null) {
                process1.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static int fAURoot(String pkgName, Boolean enable) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        if (enable) {
            outputStream.writeBytes("pm enable " + pkgName + "\n");
        } else {
            outputStream.writeBytes("pm disable " + pkgName + "\n");
        }
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        int i = process.waitFor();
        destroyProcess(outputStream, process);
        return i;
    }

}
