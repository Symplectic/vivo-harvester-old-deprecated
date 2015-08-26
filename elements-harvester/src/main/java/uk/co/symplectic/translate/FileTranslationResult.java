/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class FileTranslationResult implements TranslationResult {
    private static final Logger log = LoggerFactory.getLogger(FileTranslationResult.class);
    private File output;

    private boolean keepEmpty = false;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public FileTranslationResult(File output) {
        this.output = output;
    }

    public FileTranslationResult setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
        return this;
    }

    @Override
    public Result result() {
        return new StreamResult(baos);
    }

    @Override
    public void release() throws IOException {
        byte[] xml = baos.toByteArray();

        if (keepEmpty || xml.length > 0) {
            OutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(output));
                outputStream.write(xml);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }
}
