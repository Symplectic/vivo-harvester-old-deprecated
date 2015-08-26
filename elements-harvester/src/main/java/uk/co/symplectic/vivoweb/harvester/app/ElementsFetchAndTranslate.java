/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.app;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.UsageException;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIHttpClient;
import uk.co.symplectic.elements.api.IgnoreSSLErrorsProtocolSocketFactory;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.utils.ExecutorServiceUtils;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsExcludedUsersFetch;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsFetch;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsFetchObserver;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsUserPhotoRetrievalObserver;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.ResourceFetchService;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoreFactory;
import uk.co.symplectic.vivoweb.harvester.store.ElementsTransferredRdfStore;
import uk.co.symplectic.vivoweb.harvester.transfer.TransferElementsRdfStoreObserver;
import uk.co.symplectic.vivoweb.harvester.transfer.TransferService;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsObjectTranslateObserver;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsRelationshipTranslateObserver;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
                // Store the time we started running
                Date startTime = Calendar.getInstance().getTime();

                // Load the configuration
                Configuration.parse("ElementsFetchAndTranslate", args);
                log.debug("ElementsFetchAndTranslate: Start");

                // Set the background thread pool sizes
                setExecutorServiceMaxThreadsForPool("TranslationService", Configuration.getMaxThreadsXsl());
                setExecutorServiceMaxThreadsForPool("ResourceFetchService", Configuration.getMaxThreadsResource());
                setExecutorServiceMaxThreadsForPool("TransferService", Configuration.getMaxThreadsTransfer());

                // Obtain the stores we are using
                final ElementsObjectStore objectStore = ElementsStoreFactory.getObjectStore();
                final ElementsRdfStore rdfStore = ElementsStoreFactory.getRdfStore();
                final ElementsTransferredRdfStore transferredRdfStore = ElementsStoreFactory.getTransferredRdfStore();

                // Obtain bits of configuration
                final boolean currentStaffOnly = Configuration.getCurrentStaffOnly();
                final boolean visibleLinksOnly = Configuration.getVisibleLinksOnly();

                final File vivoImageDir = ElementsFetchAndTranslate.getVivoImageDir(Configuration.getVivoImageDir());

                final String vivoBaseURI = Configuration.getBaseURI();
                final String xslFilename = Configuration.getXslTemplate();

                boolean useElementsDeltas = Configuration.getUseElementsDeltas();
                Date lastExecuted = null;

                if (useElementsDeltas) {
                    if (
                        currentStaffOnly ||
                        visibleLinksOnly ||
                        !StringUtils.isEmpty(Configuration.getGroupsToExclude()) ) {

                        System.err.println("Error: Unable to do direct transfer if limiting to:");
                        System.err.println("");
                        System.err.println("current staff");
                        System.err.println("visible links");
                        System.err.println("or excluding groups");

                        System.exit(1);
                    }

                    if (!StringUtils.isEmpty(Configuration.getGroupsToHarvest())) {
                        System.err.println("Warning: Restricting to groups may be unpredictable.");
                    }

                    rdfStore.addObserver(
                            TransferElementsRdfStoreObserver.create()
                                    .setTransferredRdfStore(transferredRdfStore)
                    );

                    lastExecuted = loadLastRun();
                }

                ElementsAPI elementsAPI = ElementsFetchAndTranslate.getElementsAPI();

                ElementsFetch fetcher = new ElementsFetch(elementsAPI);

                fetcher.setElementsDeltas(useElementsDeltas);
                fetcher.setModifiedSince(lastExecuted);

                /** Define harvest scope **/
                fetcher.setGroupsToHarvest(Configuration.getGroupsToHarvest());
                fetcher.setObjectsToHarvest(Configuration.getObjectsToHarvest());

                /** Set pagination **/
                fetcher.setObjectsPerPage(Configuration.getApiObjectsPerPage());
                fetcher.setRelationshipsPerPage(Configuration.getApiRelationshipsPerPage());

                /** Add observers for Objects **/
                fetcher.addObjectObserver(
                        ElementsObjectTranslateObserver.create()
                                .setRdfStore(rdfStore)
                                .setKeepEmpty(useElementsDeltas)
                                .setXslTemplate(xslFilename)
                                .setXslParameters(Configuration.getXslParameters())
                                .setCurrentStaffOnly(currentStaffOnly)
                                .setExcludedUsers(getExcludedUsers(elementsAPI))
                                .addObserver(
                                        ElementsUserPhotoRetrievalObserver.create()
                                                .setElementsAPI(elementsAPI)
                                                .setObjectStore(objectStore)
                                                .setRdfStore(rdfStore)
                                                .setImageDir(vivoImageDir)
                                                .setBaseURI(vivoBaseURI)
                                )
                );

                /** Add observers for Relationships **/
                fetcher.addRelationshipObserver(
                        ElementsRelationshipTranslateObserver.create()
                                .setObjectStore(objectStore)
                                .setRdfStore(rdfStore)
                                .setKeepEmpty(useElementsDeltas)
                                .setXslTemplate(xslFilename)
                                .setXslParameters(Configuration.getXslParameters())
                                .setCurrentStaffOnly(currentStaffOnly)
                                .setVisibleLinksOnly(visibleLinksOnly)
                );

                fetcher.addFetchObserver(new ElementsFetchObserver() {
                    @Override
                    public void postFetch() {
                        TranslationService.shutdown();
                        ResourceFetchService.shutdown();
                        if (transferredRdfStore != null) {
                            TransferService.shutdown();
                        }
                    }
                });

                /** Run the harvest **/
                fetcher.execute();

                if (useElementsDeltas) {
                    saveLastRun(startTime);
                }

                long execution = Calendar.getInstance().getTimeInMillis() - startTime.getTime();
                long execHours = TimeUnit.HOURS.convert(execution, TimeUnit.MILLISECONDS);
                long execMin = TimeUnit.MINUTES.convert(execution, TimeUnit.MILLISECONDS) - (execHours * 60);
                long execSec = TimeUnit.SECONDS.convert(execution, TimeUnit.MILLISECONDS) - ((execHours * 60) + execMin) * 60;
                System.out.println("Completed in " + execHours + " hours " + execMin + " minutes and " + execSec + " secs.");
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

    private static Set<String> getExcludedUsers(ElementsAPI elementsAPI) throws IOException {
        if (!StringUtils.isEmpty(Configuration.getGroupsToExclude())) {
            ElementsExcludedUsersFetch excludedUserFetcher = new ElementsExcludedUsersFetch(elementsAPI);
            excludedUserFetcher.setGroupsToExclude(Configuration.getGroupsToExclude());
            excludedUserFetcher.execute();
            return excludedUserFetcher.getExcludedUsers();
        }

        return null;
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

    private static DateFormat lastRunFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
    private static Date loadLastRun() throws IOException {
        File runFile = new File("data/lastrun");
        if (runFile.exists()) {
            BufferedReader r = new BufferedReader(new FileReader(runFile.getAbsoluteFile()));
            String date = r.readLine();
            try {
                return lastRunFormat.parse(date);
            } catch (ParseException e) {
                log.error("Unable to parse date: " + date);
                System.exit(1);
            } finally {
                r.close();
            }
        }
        return null;
    }

    private static void saveLastRun(Date ran) throws IOException {
        File runFile = new File("data/lastrun");
        Writer w = new BufferedWriter(new FileWriter(runFile.getAbsoluteFile()));
        w.write(lastRunFormat.format(ran));
        w.close();
    }
}
