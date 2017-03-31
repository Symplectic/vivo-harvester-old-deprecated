/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class FileTranslationSource implements TranslationSource {
    private File inputFile;
    private InputStream inputStream = null;

    public FileTranslationSource(File inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public Source source() {
        try {
            inputStream = new BufferedInputStream(new FileInputStream(inputFile));
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
