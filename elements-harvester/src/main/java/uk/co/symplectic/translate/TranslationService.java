/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Map;

/**
 * Public interface to the Translation service.
 *
 * Wraps the static implementation in an object, so that it can be mocked / substituted.
 */
public final class TranslationService {
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private TranslationServiceConfig config = new TranslationServiceConfig();

    public TranslationService() {}

    public void setIgnoreFileNotFound(boolean ignoreFlag) {
        config.setIgnoreFileNotFound(ignoreFlag);
    }

    public Templates compileSource(File file) {
        return TranslationServiceImpl.compileSource(new StreamSource(file));
    }

    public TranslationTask translate(TranslationSource input, TranslationResult output, TemplatesHolder translationTemplates, Map<String, String> params) {
        return TranslationServiceImpl.translate(config, input, output, translationTemplates, params);
    }

    public static void shutdown() {
        TranslationServiceImpl.shutdown();
    }
}
