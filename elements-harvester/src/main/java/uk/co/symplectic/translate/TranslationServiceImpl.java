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
import uk.co.symplectic.utils.ExecutorServiceUtils;

import javax.xml.transform.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

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

    static TranslationTask translate(TranslationServiceConfig config, TranslationSource input, TranslationResult output, TemplatesHolder translationTemplates, Map<String, String> params) {
         return new TranslationTask(wrapper.submit(new TranslateHandler(config, input, output, translationTemplates, params)));
    }

    static class TranslateHandler implements Callable<Boolean> {
        private TranslationSource input;
        private TranslationResult output;

        private TemplatesHolder templates;

        private TranslationServiceConfig config;

        private Map<String, String> params;

        TranslateHandler(TranslationServiceConfig config, TranslationSource input, TranslationResult output, TemplatesHolder translationTemplates, Map<String, String> params) {
            this.config = config == null ? new TranslationServiceConfig() : config;
            this.input = input;
            this.output = output;
            this.templates = translationTemplates;
            this.params = params;
        }

        @Override
        public Boolean call() throws Exception {
            Boolean retCode = Boolean.TRUE;

            try {
                Transformer transformer = templates.getTemplates().newTransformer();
                transformer.setErrorListener(new TranslateTaskErrorListener(config));

                if (params != null) {
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        if (!StringUtils.isEmpty(param.getKey()) && !StringUtils.isEmpty(param.getValue())) {
                            try { transformer.setParameter(param.getKey(), param.getValue()); } catch (RuntimeException re) { }
                        }
                    }
                }

                transformer.transform(input.source(), output.result());
            } catch (TransformerException e) {
                log.error("Unable to perform translation on " + input.description(), e);
                retCode = Boolean.FALSE;
            } finally {
                try { output.release(); } catch (IOException ie) { }
                try { input.release(); } catch (IOException ie) { }
            }

            return retCode;
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
