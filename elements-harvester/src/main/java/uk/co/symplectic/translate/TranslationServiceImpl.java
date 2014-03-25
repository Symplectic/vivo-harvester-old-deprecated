/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.utils.ExecutorServiceUtils;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Static implementation of an Executor based translation service.
 *
 * Package private, as this is not part of the public API.
 *
 * Users should access via the TranslationService() object.
 */
final class TranslationServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static final ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("TranslationService");

    private TranslationServiceImpl() {}

    static Templates compileSource(Source source) {
        try {
            return TranslationServiceImpl.getFactory().newTemplates(source);
        } catch (TransformerConfigurationException e) {
            log.error("Unable to compile ", e);
            throw new IllegalStateException("");
        }
    }

    static TransformerFactory getFactory() {
        TransformerFactory factory = null;
        try {
            factory =  TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        } catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
            log.warn("Unable to obtain Saxon XSLT factory. Attempting fallback to default.", transformerFactoryConfigurationError);
        }

        if (null == factory) {
            factory = TransformerFactory.newInstance();
        }

        if (null == factory) {
            throw new IllegalStateException("Unable to obtain a TransformerFactory instance");
        }

        return factory;
    }

    static void translate(TranslationServiceConfig config, File input, File output, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
        Future<Boolean> result = wrapper.service().submit(new TranslateTask(config, input, output, translationTemplates, callback));
    }

    static void translate(TranslationServiceConfig config, InputStream inputStream, OutputStream outputStream, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
        Future<Boolean> result = wrapper.service().submit(new TranslateTask(config, inputStream, outputStream, translationTemplates, callback));
    }

    static class TranslateTask implements Callable<Boolean> {
        private File inputFile = null;
        private File outputFile = null;

        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        private TemplatesHolder templates;

        private PostTranslateCallback postTranslateCallback;

        private TranslationServiceConfig config;

        TranslateTask(TranslationServiceConfig config, InputStream inputStream, OutputStream outputStream, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
            this.config = config == null ? new TranslationServiceConfig() : config;
            this.inputFile = null;
            this.outputFile = null;
            this.inputStream  = inputStream;
            this.outputStream = outputStream;
            this.templates = translationTemplates;
            this.postTranslateCallback = callback;
        }

        TranslateTask(TranslationServiceConfig config, File input, File output, TemplatesHolder translationTemplates, PostTranslateCallback callback) {
            this.config = config == null ? new TranslationServiceConfig() : config;
            this.inputFile = input;
            this.outputFile = output;
            this.templates = translationTemplates;
            this.postTranslateCallback = callback;
        }

        @Override
        public Boolean call() throws Exception {
            Boolean retCode = Boolean.TRUE;
            Exception caughtException = null;

            Source xmlSource = new StreamSource(getInputStream());
            Result outputResult = new StreamResult(getOutputStream());

            try {
                Transformer transformer = templates.getTemplates().newTransformer();
                transformer.setErrorListener(new TranslateTaskErrorListener(config));
                transformer.transform(xmlSource, outputResult);

                if (outputStream != null) {
                    outputStream.flush();
                }
            } catch (IOException e) {
                log.error("Unable to write to output stream", e);
                caughtException = e;
                retCode = Boolean.FALSE;
            } catch (TransformerException e) {
                log.error("Unable to perform translation", e);
                caughtException = e;
                retCode = Boolean.FALSE;
            } finally {
                IOException caughtIOException = null;
                try { releaseInputStream(); } catch (IOException e) { caughtIOException = e; }
                try { releaseOutputStream(); } catch (IOException e) { caughtIOException = e; }

                if (postTranslateCallback != null) {
                    if (retCode) {
                        postTranslateCallback.translationSuccess();
                    } else {
                        postTranslateCallback.translationFailure(caughtException);
                    }
                }

                if (caughtIOException != null) {
                    throw caughtIOException;
                }
            }

            return retCode;
        }

        private InputStream getInputStream() {
            if (inputStream != null) {
                return inputStream;
            } else if (inputFile != null) {
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                    return inputStream;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to open input stream", e);
                }
            }

            return null;
        }

        private OutputStream getOutputStream() {
            if (outputStream != null) {
                return outputStream;
            } else if (outputFile != null) {
                try {
                    outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    return outputStream;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to open output stream", e);
                }
            }

            return null;
        }

        private void releaseInputStream() throws IOException {
            if (inputFile != null && inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
        }

        private void releaseOutputStream() throws IOException {
            if (outputFile != null && outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        }
    }

    static void shutdown() {
        wrapper.shutdown();
    }

    private static class TranslateTaskErrorListener implements ErrorListener {
        TranslationServiceConfig config;

        TranslateTaskErrorListener(TranslationServiceConfig config) {
            this.config = config == null ? new TranslationServiceConfig() : config;
        }

        @Override
        public void warning(TransformerException exception) throws TransformerException {
            throw exception;
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
            Throwable cause = exception.getCause();
            if (config.getIgnoreFileNotFound() && cause instanceof FileNotFoundException) {
                log.trace("Ignoring file not found in transform");
            } else {
                log.error("Transformer Exception", exception);
                throw exception;
            }
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
            throw exception;
        }
    };
}
