/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.app;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.FileTranslationSource;
import uk.co.symplectic.translate.TemplatesHolder;
import uk.co.symplectic.translate.TranslationResult;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;

import java.io.File;
import java.io.IOException;

public class ElementsTranslate implements RecordStreamOrigin {
    private static final String ARG_RAW_OUTPUT_DIRECTORY = "rawOutput";
    private static final String ARG_RDF_OUTPUT_DIRECTORY = "rdfOutput";

    private static final String ARG_API_QUERY_OBJECTS     = "queryObjects";
    private static final String ARG_XSL_TEMPLATE         = "xslTemplate";

    private static final String ARG_OUTPUT_LEGACY         = "legacyLayout";

    // These should really be pulled from configuration
    private static final String RAW_RECORD_STORE = "data/raw-records";
    private static final String RDF_RECORD_STORE = "data/translated-records";


    /**
     * SLF4J Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ElementsTranslate.class);

    private String xslFilename;
    private String queryObjects;

    private String rawRecordStoreDir = RAW_RECORD_STORE;
    private String rdfRecordStoreDir = RDF_RECORD_STORE;

    private final TranslationService translationService = new TranslationService();
    private TemplatesHolder template = null;
    private void processDir(ElementsObjectCategory category, File dir) {
        ElementsRdfStore rdfStore = new ElementsRdfStore(rdfRecordStoreDir);

        for (File file : dir.listFiles()) {
            TranslationResult output;
            if (category != null) {
                output = rdfStore.getObjectTranslationResult(ElementsObjectInfo.create(category, file.getName()));
            } else {
                output = rdfStore.getRelationshipTranslationResult(ElementsRelationshipInfo.create(file.getName()));
            }

            if (output != null) {
                translationService.translate(new FileTranslationSource(file), output, template, Configuration.getXslParameters());
            }
        }
    }

    /**
     * Executes the task
     * @throws java.io.IOException error processing search
     */
    public void execute() throws IOException {
        if (!StringUtils.isEmpty(xslFilename)) {
            template = new TemplatesHolder(xslFilename);
            translationService.setIgnoreFileNotFound(true);
        }

        File rawRecordStore = new File(rawRecordStoreDir);
        if (rawRecordStore.exists() && rawRecordStore.isDirectory()) {
            for (String category : queryObjects.split("\\s*,\\s*")) {
                ElementsObjectCategory eoCategory = ElementsObjectCategory.valueOf(category);
                if (eoCategory != null) {
                    File categoryDir = new File(rawRecordStore, eoCategory.getSingular());
                    if (categoryDir.exists() && categoryDir.isDirectory()) {
                        processDir(eoCategory, categoryDir);
                    }
                }
            }
        }

        File relationshipDir = new File(rawRecordStore, "relationship");
        if (relationshipDir.exists() && relationshipDir.isDirectory()) {
            processDir(null, relationshipDir);
        }

        TranslationService.shutdown();
    }

    @Override
    public void writeRecord(String s, String s1) throws IOException {
    }

    /**
     * Constructor
     * @param argList parsed argument list
     * @throws java.io.IOException error creating task
     */
    private ElementsTranslate(ArgList argList) {
        xslFilename = argList.get(ARG_XSL_TEMPLATE);
        queryObjects = argList.get(ARG_API_QUERY_OBJECTS);
    }

    /**
     * Get the ArgParser for this task
     * @param appName the application name
     * @return the ArgParser
     */
    private static ArgParser getParser(String appName) {
        //RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O"))
        ArgParser parser = new ArgParser(appName);
        parser.addArgument(new ArgDef().setShortOption('r').setLongOpt(ARG_RAW_OUTPUT_DIRECTORY).setDescription("Raw RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('t').setLongOpt(ARG_RDF_OUTPUT_DIRECTORY).setDescription("Translated RecordHandler config file path").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('c').setLongOpt(ARG_API_QUERY_OBJECTS).setDescription("Elements API object categories").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('l').setLongOpt(ARG_OUTPUT_LEGACY).setDescription("Legacy layout").withParameter(true, "CONFIG_FILE"));

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
                new ElementsTranslate(parser.parse(args)).execute();
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

