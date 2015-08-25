/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.cache;

import uk.co.symplectic.translate.TranslationSource;
import uk.co.symplectic.vivoweb.harvester.cache.CachingService;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class CacheAwareFileTranslationSource implements TranslationSource {
    private static CachingService cachingService = new CachingService();

    private File inputFile;
    private InputStream inputStream = null;

    public CacheAwareFileTranslationSource(File inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public Source source() {
        try {
            String xml = cachingService.get(inputFile);
            if (xml != null) {
                inputStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
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
        cachingService.remove(inputFile);
    }

    @Override
    public String description() {
        return inputFile.getAbsolutePath();
    }
}
