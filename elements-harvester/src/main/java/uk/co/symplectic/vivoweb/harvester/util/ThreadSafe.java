package uk.co.symplectic.vivoweb.harvester.util;

import java.io.File;

public final class ThreadSafe {
    public static boolean mkdirs(File file) {
        if (file == null) {
            return false;
        }

        // Test whether the file exists
        if (!file.exists()) {
            // Attempt to create the directory
            if (!file.mkdirs()) {
                // Check the directory exists (it may have failed as another thread has created it)
                if (!file.exists()) {
                    return false;
                }
            }
        }

        return file.isDirectory();
    }
}
