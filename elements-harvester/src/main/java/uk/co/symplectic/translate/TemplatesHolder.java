/*******************************************************************************
 * Copyright (c) 2014 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import javax.xml.transform.Templates;
import java.io.File;

public class TemplatesHolder {
    private final TranslationService translationService = new TranslationService();
    private final ThreadLocal<Templates> myTemplates;

    public TemplatesHolder(final String xslFilename) {
        myTemplates = new ThreadLocal<Templates>() {
            @Override
            protected Templates initialValue() {
                File xslFile = new File(xslFilename);
                if (xslFile.exists()) {
                    return translationService.compileSource(xslFile);
                } else {
                    throw new IllegalStateException("XSL Translation file not found: " + xslFilename);
                }
            }
        };
    }

    public Templates getTemplates() {
        return myTemplates.get();
    }
}
