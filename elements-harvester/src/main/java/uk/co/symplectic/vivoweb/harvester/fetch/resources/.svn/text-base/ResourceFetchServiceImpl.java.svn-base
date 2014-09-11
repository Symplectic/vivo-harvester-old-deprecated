/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.utils.ExecutorServiceUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class ResourceFetchServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(ResourceFetchServiceImpl.class);

    private static final ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("ResourceFetchService");

    private ResourceFetchServiceImpl() {}

    static void fetchElements(ElementsAPI api, String url, File outputFile, PostFetchCallback callback) throws MalformedURLException {
        // Attempt to validate the URL by creating a URL object
        // (This is just to throw a MalformedURLException - we don't need the url object)
        URL validateUrl = new URL(url);

        Future<Boolean> result = wrapper.service().submit(new ElementsFetchTask(api, url, outputFile, callback));
    }

    static void fetchExternal(String url, File outputFile, PostFetchCallback callback) throws MalformedURLException {
        Future<Boolean> result = wrapper.service().submit(new ExternalFetchTask(new URL(url), outputFile, callback));
    }

    static class ElementsFetchTask implements Callable<Boolean> {
        private ElementsAPI api;
        private String url;

        private File outputFile;

        private PostFetchCallback postFetchCallback;

        ElementsFetchTask(ElementsAPI api, String url, File outputFile, PostFetchCallback callback) {
            this.api = api;
            this.url = url;
            this.outputFile = outputFile;
            this.postFetchCallback = callback;
        }

        @Override
        public Boolean call() throws Exception {
            Boolean retCode = Boolean.TRUE;

            OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
            retCode = api.fetchResource(url, os);
            os.close();

            if (postFetchCallback != null) {
                postFetchCallback.fetchSuccess(outputFile);
            }

            return retCode;
        }
    }

    static class ExternalFetchTask implements Callable<Boolean> {
        private URL url;

        private File outputFile;

        private PostFetchCallback postFetchCallback;

        ExternalFetchTask(URL url, File outputFile, PostFetchCallback callback) {
            this.url = url;
            this.outputFile = outputFile;
            this.postFetchCallback = callback;
        }

        @Override
        public Boolean call() throws Exception {
            // Not implemented yet
            return Boolean.TRUE;
        }
    }

    static void shutdown() {
        wrapper.shutdown();
    }
}
