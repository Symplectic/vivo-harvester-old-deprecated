/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.translate.TranslationSource;
import uk.co.symplectic.vivoweb.harvester.cache.CachingServiceImpl;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.ByteBuffer;

public class FileTempCache {
    private static boolean isEnabled = true;

    public static void setEnabled(boolean isEnabled) {
        FileTempCache.isEnabled = isEnabled;
    }

    public synchronized void put(File file, byte[] xml) {
        if (isEnabled) {
            CachingServiceImpl.put(file.getAbsolutePath(), ByteBuffer.wrap(xml));
        }
    }

    public synchronized byte[] remove(File file) {
        if (isEnabled) {
            Object content = CachingServiceImpl.get(file.getAbsolutePath());
            if (content instanceof ByteBuffer) {
                CachingServiceImpl.remove(file.getAbsolutePath());
                return ((ByteBuffer)content).array();
            }
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
