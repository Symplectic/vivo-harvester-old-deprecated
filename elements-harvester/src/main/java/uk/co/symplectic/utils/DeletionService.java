/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DeletionService {
    public DeletionService() {}

    public void delete(File toDelete) {
        DeletionServiceImpl.delete(toDelete);
    }

    public void deleteOnExit(File toDelete) {
        DeletionServiceImpl.deleteOnExit(toDelete);
    }

    public void keep(File toKeep) {
        DeletionServiceImpl.keep(toKeep);
    }

    public void shutdown() {
        DeletionServiceImpl.shutdown();
    }
}

final class DeletionServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(DeletionServiceImpl.class);
    private static Set<File> filesToDelete = new HashSet<File>();
    private static Set<File> filesToKeep = new HashSet<File>();

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    private DeletionServiceImpl() {}

    static private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            log.debug("Failed to get path of file", e);
            return "unknown file";
        }
    }

    static synchronized void delete(File toDelete) {
        if (filesToKeep.contains(toDelete)) {
            log.debug("Attempting delete on kept file - ignoring: " + getCanonicalPath(toDelete) + " " + getCallingMethod());
        } else {
            log.debug("Deleting file: " + getCanonicalPath(toDelete) + " " + getCallingMethod());
            if (toDelete.delete()) {
                log.trace("Deleted " + getCanonicalPath(toDelete) + " " + getCallingMethod());
            } else {
                log.debug("Failed to delete " + getCanonicalPath(toDelete) + " " + getCallingMethod());
            }
        }
    }

    static synchronized void deleteOnExit(File toDelete) {
        if (filesToKeep.contains(toDelete)) {
            log.trace("Requesting delete on kept file - ignoring: " + getCanonicalPath(toDelete) + " " + getCallingMethod());
        } else {
            filesToDelete.add(toDelete);
        }
    }

    static synchronized void keep(File toKeep) {
        if (toKeep.exists()) {
            if (filesToDelete.contains(toKeep)) {
                log.trace("Keeping file previously requested for delete: " + getCanonicalPath(toKeep) + " " + getCallingMethod());
                filesToDelete.remove(toKeep);
                filesToKeep.add(toKeep);
            } else {
                filesToKeep.add(toKeep);
            }
        } else {
            log.error("File to keep not found: " + getCanonicalPath(toKeep) + " " + getCallingMethod());
        }
    }

    private static String getCallingMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Calling method that we're interested in should be the 5th element
        if (stackTrace.length < 5) {
            return "";
        }

        // If the 5th element is one of our classes, ignore it
        if (stackTrace[4].getClassName().equals(ShutdownHook.class.getName()) ||
            stackTrace[4].getClassName().equals(DeletionServiceImpl.class.getName()) ||
            stackTrace[4].getClassName().equals(DeletionService.class.getName())
                ) {
            return "";
        }

        return "[" + stackTrace[4].getClassName() + "." + stackTrace[4].getMethodName() + "()]";
    }

    static synchronized void shutdown() {
        while (filesToDelete.size() > 0) {
            Set<File> myDeleteSet = filesToDelete;
            filesToDelete = new HashSet<File>();

            for (File toDelete : myDeleteSet) {
                delete(toDelete);
            }

            filesToKeep = new HashSet<File>();
        }
    }

    private static class ShutdownHook extends Thread {
        ShutdownHook() {}

        @Override
        public void run() {
            DeletionServiceImpl.shutdown();
        }
    }
}
