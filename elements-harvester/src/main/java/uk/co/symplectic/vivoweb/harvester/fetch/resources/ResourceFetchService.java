/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.elements.api.ElementsAPI;

import java.io.File;
import java.net.MalformedURLException;

public final class ResourceFetchService {
    private static Logger log = LoggerFactory.getLogger(ResourceFetchService.class);
    ResourceFetchServiceImpl impl;

    public ResourceFetchService() {

    }

    public void fetchElements(ElementsAPI api, String url, File outputFile) {
        ResourceFetchServiceImpl.fetchElements(api, url, outputFile, null);
    }

    public void fetchElements(ElementsAPI api, String url, File outputFile, PostFetchCallback callback) {
        ResourceFetchServiceImpl.fetchElements(api, url, outputFile, callback);
    }

    public void fetchExternal(String url, File outputFile, PostFetchCallback callback) throws MalformedURLException {
        ResourceFetchServiceImpl.fetchExternal(url, outputFile, callback);
    }

    public static void shutdown() {
        ResourceFetchServiceImpl.shutdown();
    }
}
