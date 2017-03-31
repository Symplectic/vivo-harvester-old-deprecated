/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.translate.TranslationSource;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileTempCache {
    private static boolean isEnabled = true;

    private static final Map<String, byte[]> fileCache = new HashMap<String, byte[]>();
    private static long cacheSize = 0;
    private static long maxCacheSize = 0;

    static {
        long maxBytes      = 1024 * 1024 * 1024; // 1 Gig
        long reservedBytes = 200  * 1024 * 1024; // 200 Meg

        // Ensure that the runtime thinks we have more than 200 Meg available
        if (Runtime.getRuntime().maxMemory() > reservedBytes) {
            maxCacheSize = Math.min(maxBytes, Runtime.getRuntime().maxMemory() - reservedBytes);
        } else {
            isEnabled = false;
        }
    }

    public static void setEnabled(boolean isEnabled) {
        FileTempCache.isEnabled = isEnabled;
    }

    public synchronized void put(File file, byte[] xml) {
        if (isEnabled && cacheSize + xml.length < maxCacheSize) {
            fileCache.put(file.getAbsolutePath(), xml);
            cacheSize += xml.length;
        }
    }

    public synchronized byte[] remove(File file) {
        if (isEnabled) {
            byte[] bytes = fileCache.remove(file.getAbsolutePath());
            if (bytes != null) {
                cacheSize -= bytes.length;
            }

            return bytes;
        }

        return null;
    }

    public TranslationSource translationSource(File inputFile) {
        return new CacheAwareFileTranslationSource(this, inputFile);
    }

    private static class CacheAwareFileTranslationSource implements TranslationSource {
        private FileTempCache fileMemStore;
        private File inputFile;
        private InputStream inputStream = null;

        CacheAwareFileTranslationSource(FileTempCache fileMemStore, File inputFile) {
            this.fileMemStore = fileMemStore;
            this.inputFile = inputFile;
        }

        @Override
        public Source source() {
            try {
                byte[] xml = fileMemStore.remove(inputFile);
                if (xml != null) {
                    inputStream = new ByteArrayInputStream(xml);
                } else {
                    inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to open input stream", e);
            }

            return new StreamSource(inputStream);
        }

        @Override
        public void release() throws IOException {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        @Override
        public String description() {
            return inputFile.getAbsolutePath();
        }
    }
}
