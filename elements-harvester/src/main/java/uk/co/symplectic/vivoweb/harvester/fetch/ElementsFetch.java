/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIFeedObjectQuery;
import uk.co.symplectic.elements.api.ElementsAPIFeedRelationshipQuery;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElementsFetch implements RecordStreamOrigin {
    public static final String ARG_RAWOUTPUT_DIRECTORY      = "rawOutput";
    public static final String ARG_RDFOUTPUT_DIRECTORY      = "rdfOutput";

    public static final String ARG_XSL_TEMPLATE         = "xslTemplate";

    public static final String ARG_OUTPUT_LEGACY         = "legacyLayout";
    public static final String ARG_ELEMENTS_API_ENDPOINT = "apiEndpoint";
    public static final String ARG_ELEMENTS_API_SECURE   = "apiIsSecure";
    public static final String ARG_ELEMENTS_API_VERSION  = "apiVersion";
    public static final String ARG_ELEMENTS_API_USERNAME = "apiUsername";
    public static final String ARG_ELEMENTS_API_PASSWORD = "apiPassword";

    public static final String ARG_CURRENT_STAFF_ONLY    = "currentStaffOnly";
    public static final String ARG_VISIBLE_LINKS_ONLY    = "visibleLinksOnly";

    public static final String ARG_API_QUERY_OBJECTS     = "queryObjects";
    public static final String ARG_API_PARAMS_GROUPS     = "paramGroups";

    // These should really be pulled from configuration
    private static final String RAW_RECORD_STORE = "data/raw-records";
    private static final String RDF_RECORD_STORE = "data/translated-records";


    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(ElementsFetch.class);

    private String  apiEndpoint;
    private String  apiVersion;
    private boolean apiIsSecure;
    private String  apiObjects;
    private String  apiUsername;
    private String  apiPassword;
    private String  xslFilename;
    private String groups;
    private boolean useLegacyLayout = false;
    private boolean currentStaffOnly = true;
    private boolean visibleLinksOnly = true;

    private String rawRecordStoreDir = RAW_RECORD_STORE;
    private String rdfRecordStoreDir = RDF_RECORD_STORE;

    /**
     * Executes the task
     * @throws IOException error processing search
     */
    public void execute() throws IOException {
        ElementsAPI api = ElementsAPI.getAPI(apiVersion, apiEndpoint, apiIsSecure);
        if (apiIsSecure) {
            api.setUsername(apiUsername);
            api.setPassword(apiPassword);
        }

        ElementsObjectStore objectStore = new ElementsObjectStore(rawRecordStoreDir);
        ElementsRdfStore rdfStore = new ElementsRdfStore(rdfRecordStoreDir);
        if (useLegacyLayout) {
            objectStore.setUseLegacyLayout(useLegacyLayout);
            rdfStore.setUseLegacyLayout(useLegacyLayout);
        }

        ElementsAPIFeedObjectQuery feedQuery = new ElementsAPIFeedObjectQuery();

        // When retrieving objects, always get the full record
        feedQuery.setFullDetails(true);

        // Load all pages, not just one
        feedQuery.setProcessAllPages(true);

        if (!StringUtils.isEmpty(groups)) {
            feedQuery.setGroups(groups);
        }

        // apiObjects is a comma delimited list of object categories that we wish to pull
        // As the API requires that we handle each category separately, we split the string and loop over the contents
        for (String category : apiObjects.split("\\s*,\\s*")) {
            ElementsObjectCategory eoCategory = ElementsObjectCategory.valueOf(category);
            if (eoCategory != null) {
                feedQuery.setCategory(eoCategory);
                api.execute(feedQuery, new ElementsObjectHandler(objectStore, rdfStore, xslFilename));
            }
        }

        ElementsAPIFeedRelationshipQuery relationshipFeedQuery = new ElementsAPIFeedRelationshipQuery();
        relationshipFeedQuery.setProcessAllPages(true);
//        relationshipFeedQuery.setFullDetails(true);
        ElementsObjectsInRelationships objectsInRelationships = new ElementsObjectsInRelationships();

        ElementsRelationshipHandler relationshipHandler = new ElementsRelationshipHandler(objectStore, rdfStore, xslFilename, objectsInRelationships);
        relationshipHandler.setCurrentStaffOnly(currentStaffOnly);
        relationshipHandler.setVisibleLinksOnly(visibleLinksOnly);
        api.execute(relationshipFeedQuery, relationshipHandler);

        TranslationService.shutdown();

        for (String category : apiObjects.split("\\s*,\\s*")) {
            ElementsObjectCategory eoCategory = ElementsObjectCategory.valueOf(category);
            if (eoCategory != null && eoCategory != ElementsObjectCategory.USER) {
                // Delete the 'unkept' RDF objects
                rdfStore.pruneExcept(eoCategory, objectsInRelationships.get(eoCategory));
            }
        }
    }

    @Override
    public void writeRecord(String s, String s1) throws IOException {
    }

    /**
     * Constructor
     * @param argList parsed argument list
     * @throws IOException error creating task
     */
    protected ElementsFetch(ArgList argList) {
        // Extract arguments and assign to the fields
//        this(argList.get("m"), argList.get("t"), argList.get("n"), argList.get("b"), RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")));
        apiEndpoint = argList.get(ARG_ELEMENTS_API_ENDPOINT);
        apiObjects = argList.get(ARG_API_QUERY_OBJECTS);
        apiVersion = argList.get(ARG_ELEMENTS_API_VERSION);

        apiUsername = argList.get(ARG_ELEMENTS_API_USERNAME);
        apiPassword = argList.get(ARG_ELEMENTS_API_PASSWORD);

        groups = argList.get(ARG_API_PARAMS_GROUPS);

        xslFilename = argList.get(ARG_XSL_TEMPLATE);

        String isSecure = argList.get(ARG_ELEMENTS_API_SECURE);
        if ("false".equalsIgnoreCase(isSecure)) {
            apiIsSecure = false;
        } else if ("true".equalsIgnoreCase(isSecure)) {
            apiIsSecure = true;
        } else if (apiEndpoint != null && apiEndpoint.startsWith("http://")) {
            apiIsSecure = false;
        } else {
            apiIsSecure = true;
        }

        String legacyLayout = argList.get(ARG_OUTPUT_LEGACY);
        if ("false".equalsIgnoreCase(legacyLayout)) {
            useLegacyLayout = false;
        } else if ("true".equalsIgnoreCase(legacyLayout)) {
            useLegacyLayout = true;
        } else {
            useLegacyLayout = false;
        }

        String currentStaffArg = argList.get(ARG_CURRENT_STAFF_ONLY);
        if ("false".equalsIgnoreCase(currentStaffArg)) {
            currentStaffOnly = false;
        } else {
            currentStaffOnly = true;
        }

        String visibleLinksArg = argList.get(ARG_VISIBLE_LINKS_ONLY);
        if ("false".equalsIgnoreCase(visibleLinksArg)) {
            visibleLinksOnly = false;
        } else {
            visibleLinksOnly = true;
        }
    }

    /**
     * Get the ArgParser for this task
     * @param appName the application name
     * @return the ArgParser
     */
    private static ArgParser getParser(String appName) {
        //RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O"))
        ArgParser parser = new ArgParser(appName);
        parser.addArgument(new ArgDef().setShortOption('r').setLongOpt(ARG_RAWOUTPUT_DIRECTORY).setDescription("Raw RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('t').setLongOpt(ARG_RDFOUTPUT_DIRECTORY).setDescription("Translated RecordHandler config file path").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('g').setLongOpt(ARG_API_PARAMS_GROUPS).setDescription("Groups to restrict queries to").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('e').setLongOpt(ARG_ELEMENTS_API_ENDPOINT).setDescription("Elements API endpoint url").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('s').setLongOpt(ARG_ELEMENTS_API_SECURE).setDescription("Is Elements API secure").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('c').setLongOpt(ARG_API_QUERY_OBJECTS).setDescription("Elements API object categories").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('v').setLongOpt(ARG_ELEMENTS_API_VERSION).setDescription("Elements API version").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('u').setLongOpt(ARG_ELEMENTS_API_USERNAME).setDescription("Elements API username").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('p').setLongOpt(ARG_ELEMENTS_API_PASSWORD).setDescription("Elements API password").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('l').setLongOpt(ARG_OUTPUT_LEGACY).setDescription("Legacy layout").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_CURRENT_STAFF_ONLY).setDescription("Current Staff Only").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_VISIBLE_LINKS_ONLY).setDescription("Visible Links Only").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('z').setLongOpt(ARG_XSL_TEMPLATE).setDescription("XSL Template").withParameter(true, "CONFIG_FILE"));
        return parser;
    }

    /**
     * Main method
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        Throwable caught = null;
        ArgParser parser = null;
        try {
            parser = getParser("ElementsFetch");
            try {
                InitLog.initLogger(args, parser);
                log.debug("ElementsFetch: Start");
                new ElementsFetch(parser.parse(args)).execute();
            } catch (IOException e) {
                caught = e;
            }

        } catch (UsageException e) {
            caught = e;
            if (parser != null) {
                log.info("Printing Usage:");
                System.out.println(parser.getUsage());
            }
        } finally {
            log.debug("ElementsFetch: End");
            if (caught != null) {
                System.exit(1);
            }

//            System.exit(0);
        }
    }
}

