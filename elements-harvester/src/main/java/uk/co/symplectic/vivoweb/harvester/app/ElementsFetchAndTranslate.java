/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.app;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.UsageException;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIHttpClient;
import uk.co.symplectic.elements.api.IgnoreSSLErrorsProtocolSocketFactory;
import uk.co.symplectic.utils.ExecutorServiceUtils;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsExcludedUsersFetch;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsFetch;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsUserPhotoRetrievalObserver;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoreFactory;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsObjectTranslateObserver;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsRelationshipTranslateObserver;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ElementsFetchAndTranslate {
    /**
     * SLF4J Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ElementsFetchAndTranslate.class);

    /**
     * Main method
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        Throwable caught = null;
        try {
            try {
                Configuration.parse("ElementsFetchAndTranslate", args);

                log.debug("ElementsFetchAndTranslate: Start");

                setExecutorServiceMaxThreadsForPool("TranslationService",   Configuration.getMaxThreadsXsl());
                setExecutorServiceMaxThreadsForPool("ResourceFetchService", Configuration.getMaxThreadsResource());

                ElementsAPI elementsAPI = ElementsFetchAndTranslate.getElementsAPI();

                ElementsExcludedUsersFetch excludedUserFetcher = new ElementsExcludedUsersFetch(elementsAPI);
                excludedUserFetcher.setGroupsToExclude(Configuration.getGroupsToExclude());
                excludedUserFetcher.execute();
                Set<String> excludedUsers = excludedUserFetcher.getExcludedUsers();

                ElementsFetch fetcher = new ElementsFetch(elementsAPI);
                fetcher.setGroupsToHarvest(Configuration.getGroupsToHarvest());
                fetcher.setObjectsToHarvest(Configuration.getObjectsToHarvest());
                fetcher.setObjectsPerPage(Configuration.getApiObjectsPerPage());
                fetcher.setRelationshipsPerPage(Configuration.getApiRelationshipsPerPage());

                ElementsObjectStore objectStore = ElementsStoreFactory.getObjectStore();
                ElementsRdfStore rdfStore = ElementsStoreFactory.getRdfStore();

                boolean currentStaffOnly = Configuration.getCurrentStaffOnly();
                boolean visibleLinksOnly = Configuration.getVisibleLinksOnly();

                String xslFilename = Configuration.getXslTemplate();
                File vivoImageDir = ElementsFetchAndTranslate.getVivoImageDir(Configuration.getVivoImageDir());
                String vivoBaseURI = Configuration.getBaseURI();

                ElementsObjectTranslateObserver objectObserver = new ElementsObjectTranslateObserver(rdfStore, xslFilename);
                objectObserver.setCurrentStaffOnly(currentStaffOnly);
                objectObserver.setExcludedUsers(excludedUsers);
                objectObserver.addObserver(new ElementsUserPhotoRetrievalObserver(elementsAPI, objectStore, rdfStore, vivoImageDir, vivoBaseURI));
                fetcher.addObjectObserver(objectObserver);

                ElementsRelationshipTranslateObserver relationshipObserver = new ElementsRelationshipTranslateObserver(objectStore, rdfStore, xslFilename);
                relationshipObserver.setCurrentStaffOnly(currentStaffOnly);
                relationshipObserver.setVisibleLinksOnly(visibleLinksOnly);
                fetcher.addRelationshipObserver(relationshipObserver);

                fetcher.execute();

            } catch (IOException e) {
                System.err.println("Caught IOExcpetion initialising ElementsFetchAndTranslate");
                e.printStackTrace(System.err);
                caught = e;
            }

        } catch (UsageException e) {
            caught = e;
            if (!Configuration.isConfigured()) {
                log.info("Printing Usage:");
                System.out.println(Configuration.getUsage());
            } else {
                System.err.println("Caught UsageExcpetion initialising ElementsFetchAndTranslate");
                e.printStackTrace(System.err);
            }
        } finally {
            log.debug("ElementsFetch: End");
            if (caught != null) {
                System.exit(1);
            }
        }
    }

    private static ElementsAPI getElementsAPI() {
        if (Configuration.getIgnoreSSLErrors()) {
            Protocol.registerProtocol("https", new Protocol("https", new IgnoreSSLErrorsProtocolSocketFactory(), 443));
        }

        String apiEndpoint = Configuration.getApiEndpoint();
        String apiVersion = Configuration.getApiVersion();

        String apiUsername = Configuration.getApiUsername();
        String apiPassword = Configuration.getApiPassword();

        boolean apiIsSecure;
        if (apiEndpoint != null && apiEndpoint.toLowerCase().startsWith("http://")) {
            apiIsSecure = false;
        } else {
            apiIsSecure = true;
        }

        int soTimeout = Configuration.getApiSoTimeout();
        if (soTimeout > 4999 && soTimeout < (30 * 60 * 1000)) {
            ElementsAPIHttpClient.setSoTimeout(soTimeout);
        }

        int requestDelay = Configuration.getApiRequestDelay();
        if (requestDelay > -1 && requestDelay < (5 * 60 * 1000)) {
            ElementsAPIHttpClient.setRequestDelay(requestDelay);
        }

        ElementsAPI api = ElementsAPI.getAPI(apiVersion, apiEndpoint, apiIsSecure);
        if (apiIsSecure) {
            api.setUsername(apiUsername);
            api.setPassword(apiPassword);
        }

        return api;
    }

    private static File getVivoImageDir(String imageDir) {
        File vivoImageDir = null;
        // TODO: This should be a required configuration parameter that specifies a path accessible by the VIVO web container
        if (!StringUtils.isEmpty(imageDir)) {
            vivoImageDir = new File(imageDir);
            if (vivoImageDir.exists()) {
                if (!vivoImageDir.isDirectory()) {
                    vivoImageDir = null;
                }
            } else {
                vivoImageDir.mkdirs();
            }
        }

        return vivoImageDir;
    }

    private static void setExecutorServiceMaxThreadsForPool(String poolName, int maxThreads) {
        if (maxThreads > 0) {
            ExecutorServiceUtils.setMaxProcessorsForPool(poolName, maxThreads);
        }
    }
}
