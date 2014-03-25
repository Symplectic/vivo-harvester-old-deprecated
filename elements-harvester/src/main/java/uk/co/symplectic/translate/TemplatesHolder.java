/*******************************************************************************
 * Copyright (c) 2014 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import javax.xml.transform.Templates;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TemplatesHolder {
    private final TranslationService translationService = new TranslationService();

    private String xslFilename;
    private ThreadLocal<Templates> myTemplates = new ThreadLocal<Templates>();

    public TemplatesHolder(String xslFilename) {
        this.xslFilename = xslFilename;
    }

    public Templates getTemplates() {
        if (myTemplates.get() == null) {
            try {
                Templates template = translationService.compileSource(new BufferedInputStream(new FileInputStream(xslFilename)));
                myTemplates.set(template);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("XSL Translation file not found", e);
            }
        }

        return myTemplates.get();
    }
}
