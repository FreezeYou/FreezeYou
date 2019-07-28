package cf.playhi.freezeyou;

import java.io.File;
import java.io.IOException;

final class FileUtils {

    static void deleteAllFiles(File file, boolean deleteSelfFolder) throws IOException {
        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                if (!file.delete())
                    throw new IOException(file.getAbsolutePath() + " delete failed");
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (deleteSelfFolder && files == null) {
                    if (!file.delete())
                        throw new IOException(file.getAbsolutePath() + " delete failed");
                } else {
                    for (File f : files) {
                        deleteAllFiles(f, true);
                    }
                    if (deleteSelfFolder && !file.delete())
                        throw new IOException(file.getAbsolutePath() + " delete failed");
                }
            }
        }
    }

}
