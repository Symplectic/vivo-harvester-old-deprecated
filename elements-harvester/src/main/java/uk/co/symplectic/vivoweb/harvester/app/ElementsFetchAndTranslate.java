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
import uk.co.symplectic.elements.api.ElementsAPIThrottle;
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
import uk.co.symplectic.vivoweb.harvester.util.Statistics;

import javax.imageio.ImageIO;
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

                final ElementsAPI elementsAPI = ElementsFetchAndTranslate.getElementsAPI();

                // Max queue sizes for throttling
                final long maxTransferQueueSize = Configuration.getMaxTransferQueueSize();
                final long maxTranslationQueueSize = Configuration.getMaxTranslationQueueSize();

                // Are we using deltas from Elements (e.g. direct upload to triple store)
                boolean useElementsDeltas = Configuration.getUseElementsDeltas();
                Date lastExecuted = null;

                // If we are getting images, disable the temporary cache, as they are going to be small anyway
                if (vivoImageDir != null) {
                    ImageIO.setUseCache(false);
                }

                if (useElementsDeltas) {
                    // Check that the configuration is consistent with deltas
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

                    // Add an observer to the RDF (translated) store, that will transfer data to the triple store
                    rdfStore.addObserver(
                            TransferElementsRdfStoreObserver.create()
                                    .setTransferredRdfStore(transferredRdfStore)
                    );

                    rdfStore.setKeepEmpty(true);

                    // Get the time of last execution
                    lastExecuted = loadLastRun();

                    // Add a throttle to the API, ensuring that neither the translation or transfer service can get overwhelmed
                    elementsAPI.setAPIThrottle(new ServiceLimitAPIThrottle()
                                    .setTranslationQueueSize(maxTranslationQueueSize)
                                    .setTransferQueueSize(maxTransferQueueSize)
                    );
                } else {
                    rdfStore.setKeepEmpty(false);

                    // We are not using the transfer service, so...
                    // Add a throttle to the API, ensuring that neither the translation service can get overwhelmed
                    elementsAPI.setAPIThrottle(new ServiceLimitAPIThrottle()
                                    .setTranslationQueueSize(maxTranslationQueueSize)
                    );
                }

                ElementsFetch fetcher = new ElementsFetch(elementsAPI);

                /** Add Elements deltas options **/
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
                                .setXslTemplate(xslFilename)
                                .setXslParameters(Configuration.getXslParameters())
                                .setOutputFormat(Configuration.getTranslateFormat())
                                .setCurrentStaffOnly(currentStaffOnly)
                                .setExcludedUsers(getExcludedUsers(elementsAPI))
                                .addObserver(
                                        ElementsUserPhotoRetrievalObserver.create(elementsAPI)
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
                                .setXslTemplate(xslFilename)
                                .setXslParameters(Configuration.getXslParameters())
                                .setOutputFormat(Configuration.getTranslateFormat())
                                .setCurrentStaffOnly(currentStaffOnly)
                                .setVisibleLinksOnly(visibleLinksOnly)
                );

                /** Add observer to ensure we wait for the services to finish **/
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

                /** Using deltas, so store the last rn time (which is actually the time we started) **/
                if (useElementsDeltas) {
                    saveLastRun(startTime);
                }

                /** Output our run time **/
                long execution = Calendar.getInstance().getTimeInMillis() - startTime.getTime();
                long execHours = TimeUnit.HOURS.convert(execution, TimeUnit.MILLISECONDS);
                long execMin = TimeUnit.MINUTES.convert(execution, TimeUnit.MILLISECONDS) - (execHours * 60);
                long execSec = TimeUnit.SECONDS.convert(execution, TimeUnit.MILLISECONDS) - ((execHours * 60) + execMin) * 60;
                System.out.println("Completed in " + execHours + " hours " + execMin + " minutes and " + execSec + " secs.");
                Statistics.print(System.out);
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

    /**
     * Helper method to give us a configured ElementsAPI instance to use
     * @return
     */
    private static ElementsAPI getElementsAPI() {
        if (Configuration.getIgnoreSSLErrors()) {
            Protocol.registerProtocol("https", new Protocol("https", new IgnoreSSLErrorsProtocolSocketFactory(), 443));
        }

        int soTimeout = Configuration.getApiSoTimeout();
        if (soTimeout > 4999 && soTimeout < (30 * 60 * 1000)) {
            ElementsAPIHttpClient.setSoTimeout(soTimeout);
        }

        ElementsAPI api = ElementsAPI.getAPI(Configuration.getApiVersion(), Configuration.getApiEndpoint());
        api.setUsername(Configuration.getApiUsername());
        api.setPassword(Configuration.getApiPassword());

        return api;
    }

    /**
     * Helper method to get users that we are excluding from the harvest
     * @param elementsAPI
     * @return
     * @throws IOException
     */
    private static Set<String> getExcludedUsers(ElementsAPI elementsAPI) throws IOException {
        if (!StringUtils.isEmpty(Configuration.getGroupsToExclude())) {
            ElementsExcludedUsersFetch excludedUserFetcher = new ElementsExcludedUsersFetch(elementsAPI);
            excludedUserFetcher.setGroupsToExclude(Configuration.getGroupsToExclude());
            excludedUserFetcher.execute();
            return excludedUserFetcher.getExcludedUsers();
        }

        return null;
    }

    /**
     * Helper method to get the image dir, making sure that it exists
     * @param imageDir
     * @return
     */
    private static File getVivoImageDir(String imageDir) {
        File vivoImageDir = null;
        if (!StringUtils.isEmpty(imageDir)) {
            vivoImageDir = new File(imageDir);
            if (vivoImageDir.exists()) {
                if (!vivoImageDir.isDirectory()) {
                    vivoImageDir = null;
                }
            } else {
                if (!vivoImageDir.mkdirs() && !vivoImageDir.exists()) {
                    vivoImageDir = null;
                }
            }
        }

        return vivoImageDir;
    }

    /**
     * Helper method to set the thread limits for the executor pools (background services)
     * @param poolName
     * @param maxThreads
     */
    private static void setExecutorServiceMaxThreadsForPool(String poolName, int maxThreads) {
        if (maxThreads > 0) {
            ExecutorServiceUtils.setMaxProcessorsForPool(poolName, maxThreads);
        }
    }

    /** Format of the last run time string **/
    private static String lastRunFormatStr = "MMM dd yyyy HH:mm:ss";

    /**
     * Get the last run time
     * @return
     * @throws IOException
     */
    private static Date loadLastRun() throws IOException {
        File runFile = new File("data/lastrun");
        if (runFile.exists()) {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(runFile), "utf-8"));
            String date = r.readLine();
            try {
                if (date != null) {
                    return new SimpleDateFormat(lastRunFormatStr).parse(date);
                } else{
                    throw new ParseException("Last run file was not valid", 0);
                }
            } catch (ParseException e) {
                log.error("Unable to parse date: " + date);
                System.exit(1);
            } finally {
                r.close();
            }
        }
        return null;
    }

    /**
     * Save the last run time
     * @return
     * @throws IOException
     */
    private static void saveLastRun(Date ran) throws IOException {
        File runFile = new File("data/lastrun");
        Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(runFile), "utf-8"));
        w.write(new SimpleDateFormat(lastRunFormatStr).format(ran));
        w.close();
    }

    /**
     * Helper class for throttling the Elements API requests, based on the size of the queue
     */
    private static class ServiceLimitAPIThrottle implements ElementsAPIThrottle {
        private long maxTranslationQueueSize = -1;
        private long maxTransferQueueSize    = -1;

        ServiceLimitAPIThrottle() {
        }

        ServiceLimitAPIThrottle setTranslationQueueSize(long size) {
            maxTranslationQueueSize = size;
            return this;
        }

        ServiceLimitAPIThrottle setTransferQueueSize(long size) {
            maxTransferQueueSize = size;
            return this;
        }


        @Override
        public void requestDelay() {
            int count = 0;
            while (isOverloaded()) {
                if (++count % 10 == 0) {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Waiting on");
                    if (isTranslationOverloaded()) {
                        msg.append(" TranslationService (" + TranslationService.getQueueSize() + ")");
                    }
                    if (isTransferOverloaded()) {
                        msg.append(" TransferService (" + TransferService.getQueueSize() + ")");
                    }
                    System.err.println(msg.toString());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private boolean isOverloaded() {
            return isTranslationOverloaded() || isTransferOverloaded();
        }

        private boolean isTranslationOverloaded() {
            return maxTranslationQueueSize > 0 && TranslationService.getQueueSize() > maxTranslationQueueSize;
        }

        private boolean isTransferOverloaded() {
            return maxTransferQueueSize > 0 && TransferService.getQueueSize() > maxTransferQueueSize;
        }
    }
}
