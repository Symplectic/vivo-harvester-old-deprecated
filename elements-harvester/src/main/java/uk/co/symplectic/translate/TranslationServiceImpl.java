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

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.concurrent.*;

/**
 * Static implementation of an Executor based translation service.
 *
 * Package private, as this is not part of the public API.
 *
 * Users should access via the TranslationService() object.
 */
final class TranslationServiceImpl {
    private static Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("TranslationService");

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
        } catch (TransformerFactoryConfigurationError tfce) {
            log.warn("Unable to obtain Saxon XSLT factory. Attempting fallback to default.", tfce);
        }

        if (null == factory) {
            factory = TransformerFactory.newInstance();
        }

        if (null == factory) {
            throw new IllegalStateException("Unable to obtain a TransformerFactory instance");
        }

        return factory;
    }

    static void translate(File input, File output, Templates translationTemplates, PostTranslateCallback callback) {
        Future<Boolean> result = wrapper.service().submit(new TranslateTask(input, output, translationTemplates, callback));
    }

    static void translate(InputStream inputStream, OutputStream outputStream, Templates translationTemplates, PostTranslateCallback callback) {
        Future<Boolean> result = wrapper.service().submit(new TranslateTask(inputStream, outputStream, translationTemplates, callback));
    }

    static class TranslateTask implements Callable<Boolean> {
        private File inputFile = null;
        private File outputFile = null;

        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        private Templates templates;

        private PostTranslateCallback postTranslateCallback;

        TranslateTask(InputStream inputStream, OutputStream outputStream, Templates translationTemplates, PostTranslateCallback callback) {
            this.inputFile = null;
            this.outputFile = null;
            this.inputStream  = inputStream;
            this.outputStream = outputStream;
            this.templates = translationTemplates;
            this.postTranslateCallback = callback;
        }

        TranslateTask(File input, File output, Templates translationTemplates, PostTranslateCallback callback) {
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
                templates.newTransformer().transform(xmlSource, outputResult);
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
}
