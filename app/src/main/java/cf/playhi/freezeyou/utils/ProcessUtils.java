package cf.playhi.freezeyou.utils;


import java.io.DataOutputStream;

public final class ProcessUtils {

    public static void destroyProcess(DataOutputStream dataOutputStream, Process process1) {
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

    public static int fAURoot(String pkgName, Boolean enable, Boolean hideMode) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        if (enable) {
            outputStream.writeBytes("pm " + (hideMode ? "unhide" : "enable ") + pkgName + "\n");
        } else {
            outputStream.writeBytes("pm " + (hideMode ? "hide" : "disable ") + pkgName + "\n");
        }
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        int i = process.waitFor();
        destroyProcess(outputStream, process);
        return i;
    }

}
