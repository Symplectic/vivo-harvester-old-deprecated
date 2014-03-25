/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Public interface to the Translation service.
 *
 * Wraps the static implementation in an object, so that it can be mocked / substituted.
 */
public final class TranslationService {
    private static Logger log = LoggerFactory.getLogger(TranslationService.class);

    private TranslationServiceConfig config = new TranslationServiceConfig();

    public TranslationService() {}

    public void setIgnoreFileNotFound(boolean ignoreFlag) {
        config.setIgnoreFileNotFound(ignoreFlag);
    }

    public Templates compileSource(Node domNode) {
        return TranslationServiceImpl.compileSource(new DOMSource(domNode));
    }

    public Templates compileSource(InputStream stream) {
        return TranslationServiceImpl.compileSource(new StreamSource(stream));
    }

    public Templates compileSource(File file) throws FileNotFoundException {
        return TranslationServiceImpl.compileSource(new StreamSource(new BufferedInputStream(new FileInputStream(file))));
    }

    public void translate(File input, File output, TemplatesHolder translationTemplates) {
        TranslationServiceImpl.translate(config, input, output, translationTemplates, null);
    }

    public void translate(File input, File output, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
        TranslationServiceImpl.translate(config, input, output, translationTemplates, callback);
    }

    public void translate(InputStream inputStream, OutputStream outputStream, TemplatesHolder translationTemplates) {
        TranslationServiceImpl.translate(config, inputStream, outputStream, translationTemplates, null);
    }

    public void translate(InputStream inputStream, OutputStream outputStream, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
        TranslationServiceImpl.translate(config, inputStream, outputStream, translationTemplates, callback);
    }

    public static void shutdown() {
        TranslationServiceImpl.shutdown();
    }
}
