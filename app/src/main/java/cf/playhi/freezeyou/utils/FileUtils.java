package cf.playhi.freezeyou.utils;

import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class FileUtils {

    public static void deleteAllFiles(File file, boolean deleteSelfFolder) throws IOException {
        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                if (!file.delete())
                    throw new IOException(file.getAbsolutePath() + " delete failed");
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteAllFiles(f, true);
                    }
                }
                if (deleteSelfFolder) {
                    if (!file.delete()) {
                        throw new IOException(file.getAbsolutePath() + " delete failed");
                    }
                }
            }
        }
    }

    public static void copyFile(InputStream in, String apkFilePath) throws IOException {
        if (in == null) {
            throw new IOException("InputStream is null");
        }
        if (Build.VERSION.SDK_INT < 26) {
            FileOutputStream out = new FileOutputStream(apkFilePath);
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
        } else {
            Files.copy(in, new File(apkFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
