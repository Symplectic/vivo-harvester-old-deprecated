/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
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

    public void shutdown() {
        DeletionServiceImpl.shutdown();
    }
}

final class DeletionServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(DeletionServiceImpl.class);
    private static Set<File> filesToDelete = new HashSet<File>();

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    private DeletionServiceImpl() {}

    static synchronized void delete(File toDelete) {
        try {
            log.debug("Deleting file: " + toDelete.getCanonicalPath());
        } catch (IOException e) {
            log.debug("Failed to get path of file to delete", e);
        }
        try {
            if (toDelete.delete()) {
                log.trace("Deleted " + toDelete.getCanonicalPath());
            } else {
                log.trace("Failed to delete " + toDelete.getCanonicalPath());
            }
        } catch (IOException e) {
            log.debug("Exception deleting file", e);
        }
    }

    static synchronized void deleteOnExit(File toDelete) {
        filesToDelete.add(toDelete);
    }

    static synchronized void shutdown() {
        while (filesToDelete.size() > 0) {
            Set<File> myDeleteSet = filesToDelete;
            filesToDelete = new HashSet<File>();

            for (File toDelete : myDeleteSet) {
                delete(toDelete);
            }
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
