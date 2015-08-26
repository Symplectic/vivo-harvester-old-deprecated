/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.config;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.lang.StringUtils;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final String ARG_RAW_OUTPUT_DIRECTORY = "rawOutput";
    private static final String ARG_RDF_OUTPUT_DIRECTORY = "rdfOutput";

    private static final String ARG_TRANSFER_DIR     = "transferDir";
    private static final String ARG_LIVE_TRIPLESTORE = "liveStore";

    private static final String ARG_XSL_TEMPLATE         = "xslTemplate";

    private static final String ARG_ELEMENTS_API_ENDPOINT = "apiEndpoint";
    private static final String ARG_ELEMENTS_API_SECURE   = "apiIsSecure";
    private static final String ARG_ELEMENTS_API_VERSION  = "apiVersion";
    private static final String ARG_ELEMENTS_API_USERNAME = "apiUsername";
    private static final String ARG_ELEMENTS_API_PASSWORD = "apiPassword";

    private static final String ARG_CURRENT_STAFF_ONLY    = "currentStaffOnly";
    private static final String ARG_VISIBLE_LINKS_ONLY    = "visibleLinksOnly";

    private static final String ARG_USE_FULL_UTF8         = "useFullUTF8";

    private static final String ARG_VIVO_IMAGE_DIR        = "vivoImageDir";
    private static final String ARG_VIVO_BASE_URI         = "vivoBaseURI";

    private static final String ARG_API_QUERY_OBJECTS     = "queryObjects";
    private static final String ARG_API_PARAMS_GROUPS     = "paramGroups";
    private static final String ARG_API_EXCLUDE_GROUPS    = "excludeGroups";

    private static final String ARG_API_OBJECTS_PER_PAGE  = "objectsPerPage";
    private static final String ARG_API_RELS_PER_PAGE     = "relationshipsPerPage";

    private static final String ARG_API_SOCKET_TIMEOUT    = "apiSocketTimeout";
    private static final String ARG_API_REQUEST_DELAY     = "apiRequestDelay";

    private static final String ARG_MAX_XSL_THREADS       = "maxXslThreads";
    private static final String ARG_MAX_RESOURCE_THREADS  = "maxResourceThreads";
    private static final String ARG_MAX_TRANSFER_THREADS  = "maxTransferThreads";

    private static final String ARG_IGNORE_SSL_ERRORS     = "ignoreSSLErrors";

    // Maximum of 25 is mandated by 4.6 and newer APIs since we request full detail for objects
    private static final int OBJECTS_PER_PAGE = 25;

    // Default of 100 for optimal performance
    private static final int RELATIONSHIPS_PER_PAGE = 100;

    private static final String DEFAULT_IMAGE_DIR = "/Library/Tomcat/webapps/vivo";
    private static final String DEFAULT_BASE_URI = "http://localhost:8080/vivo/individual/";

    private static final String DEFAULT_TRANSFER_DIR   = "transferred/";
    private static final String DEFAULT_RAW_OUTPUT_DIR = "data/raw-records/";
    private static final String DEFAULT_RDF_OUTPUT_DIR = "data/translated-records/";

    private static ArgParser parser = null;
    private static ArgList argList = null;

    private static class ConfigurationValues {
        private int maxThreadsResource = 2;
        private int maxThreadsTransfer = 2;
        private int maxThreadsXsl = 0;

        private String apiEndpoint;
        private String apiVersion;

        private String apiUsername;
        private String apiPassword;

        private int apiSoTimeout = 0;
        private int apiRequestDelay = -1;

        private int apiObjectsPerPage  = OBJECTS_PER_PAGE;
        private int apiRelationshipsPerPage  = RELATIONSHIPS_PER_PAGE;

        private String groupsToExclude;
        private String groupsToHarvest;
        private String objectsToHarvest;

        private boolean currentStaffOnly = true;
        private boolean visibleLinksOnly = false;

        private boolean useFullUTF8 = false;

        private String vivoImageDir = DEFAULT_IMAGE_DIR;
        private String baseURI = DEFAULT_BASE_URI;
        private String xslTemplate;

        private String rawOutputDir = DEFAULT_RAW_OUTPUT_DIR;
        private String rdfOutputDir = DEFAULT_RDF_OUTPUT_DIR;
        private String transferDir  = DEFAULT_TRANSFER_DIR;

        private static boolean ignoreSSLErrors = false;

        private Model tripleStore = null;

        private Map<String, String> xslParameters = new HashMap<String, String>();
    };

    private static ConfigurationValues values = new ConfigurationValues();

    public static Integer getMaxThreadsResource() { return values.maxThreadsResource; }

    public static Integer getMaxThreadsXsl() { return values.maxThreadsXsl; }

    public static Integer getMaxThreadsTransfer() { return values.maxThreadsTransfer; }

    public static String getApiEndpoint() {
        return values.apiEndpoint;
    }

    public static String getApiVersion() {
        return values.apiVersion;
    }

    public static String getApiUsername() {
        return values.apiUsername;
    }

    public static String getApiPassword() {
        return values.apiPassword;
    }

    public static int getApiSoTimeout() {
        return values.apiSoTimeout;
    }

    public static int getApiRequestDelay() {
        return values.apiRequestDelay;
    }

    public static int getApiObjectsPerPage() {
        return values.apiObjectsPerPage;
    }

    public static int getApiRelationshipsPerPage() {
        return values.apiRelationshipsPerPage;
    }

    public static String getGroupsToExclude() {
        return values.groupsToExclude;
    }

    public static String getGroupsToHarvest() {
        return values.groupsToHarvest;
    }

    public static String getObjectsToHarvest() {
        return values.objectsToHarvest;
    }

    public static boolean getCurrentStaffOnly() {
        return values.currentStaffOnly;
    }

    public static boolean getVisibleLinksOnly() {
        return values.visibleLinksOnly;
    }

    public static boolean getUseFullUTF8() { return values.useFullUTF8; }

    public static String getVivoImageDir() {
        return values.vivoImageDir;
    }

    public static String getBaseURI() {
        return values.baseURI;
    }

    public static String getXslTemplate() {
        return values.xslTemplate;
    }

    public static String getRawOutputDir() { return values.rawOutputDir; }
    public static String getRdfOutputDir() { return values.rdfOutputDir; }
    public static String getTransferDir() { return values.transferDir; }

    public static Model getTripleStore() { return values.tripleStore; }

    public static Map<String, String> getXslParameters() { return values.xslParameters; }

    public static boolean getIgnoreSSLErrors() { return values.ignoreSSLErrors; }

    public static void parse(String appName, String[] args) throws IOException, UsageException {
        argList = null;
        parser = new ArgParser(appName);
        parser.addArgument(new ArgDef().setShortOption('r').setLongOpt(ARG_RAW_OUTPUT_DIRECTORY).setDescription("Raw RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('t').setLongOpt(ARG_RDF_OUTPUT_DIRECTORY).setDescription("Translated RecordHandler config file path").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_TRANSFER_DIR).setDescription("Directory for holding RDF/XML for deltas").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_LIVE_TRIPLESTORE).setDescription("Live triple store (Elements deltas)").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('g').setLongOpt(ARG_API_PARAMS_GROUPS).setDescription("Groups to restrict queries to").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('g').setLongOpt(ARG_API_EXCLUDE_GROUPS).setDescription("Groups to exclude users from").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('e').setLongOpt(ARG_ELEMENTS_API_ENDPOINT).setDescription("Elements API endpoint url").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('s').setLongOpt(ARG_ELEMENTS_API_SECURE).setDescription("Is Elements API secure").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('c').setLongOpt(ARG_API_QUERY_OBJECTS).setDescription("Elements API object categories").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('v').setLongOpt(ARG_ELEMENTS_API_VERSION).setDescription("Elements API version").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('u').setLongOpt(ARG_ELEMENTS_API_USERNAME).setDescription("Elements API username").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('p').setLongOpt(ARG_ELEMENTS_API_PASSWORD).setDescription("Elements API password").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_CURRENT_STAFF_ONLY).setDescription("Current Staff Only").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_VISIBLE_LINKS_ONLY).setDescription("Visible Links Only").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_USE_FULL_UTF8).setDescription("Use Full UTF8").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_VIVO_IMAGE_DIR).setDescription("Vivo Image Directory").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_VIVO_BASE_URI).setDescription("Vivo Base URI").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_API_OBJECTS_PER_PAGE).setDescription("Objects Per Page").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_API_RELS_PER_PAGE).setDescription("Relationships Per Page").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_API_SOCKET_TIMEOUT).setDescription("HTTP Socket Timeout").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_API_REQUEST_DELAY).setDescription("API Request Delay").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_MAX_XSL_THREADS).setDescription("Maximum number of Threads to use for the XSL Translation").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_MAX_RESOURCE_THREADS).setDescription("Maximum number of Threads to use for the Resource (photo) downloads").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_MAX_TRANSFER_THREADS).setDescription("Maximum number of Threads to use for the Triple Store loading").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_IGNORE_SSL_ERRORS).setDescription("Ignore SSL Errors").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('z').setLongOpt(ARG_XSL_TEMPLATE).setDescription("XSL Template").withParameter(true, "CONFIG_FILE"));

        InitLog.initLogger(args, parser);
        argList = parser.parse(args);

        if (argList != null) {
            values.maxThreadsResource = getInt(ARG_MAX_RESOURCE_THREADS, values.maxThreadsResource);
            values.maxThreadsXsl      = getInt(ARG_MAX_XSL_THREADS, values.maxThreadsXsl);
            values.maxThreadsTransfer = getInt(ARG_MAX_TRANSFER_THREADS, values.maxThreadsTransfer);

            values.apiEndpoint = getString(ARG_ELEMENTS_API_ENDPOINT);
            values.apiVersion = getString(ARG_ELEMENTS_API_VERSION);

            values.apiUsername = getString(ARG_ELEMENTS_API_USERNAME);
            values.apiPassword = getString(ARG_ELEMENTS_API_PASSWORD);

            values.apiSoTimeout = getInt(ARG_API_SOCKET_TIMEOUT, 0);
            values.apiRequestDelay = getInt(ARG_API_REQUEST_DELAY, -1);

            values.groupsToExclude = getString(ARG_API_EXCLUDE_GROUPS);

            values.groupsToHarvest = getString(ARG_API_PARAMS_GROUPS);
            values.objectsToHarvest = getString(ARG_API_QUERY_OBJECTS);

            values.apiObjectsPerPage  = getInt(ARG_API_OBJECTS_PER_PAGE, OBJECTS_PER_PAGE);
            values.apiRelationshipsPerPage  = getInt(ARG_API_RELS_PER_PAGE, RELATIONSHIPS_PER_PAGE);

            values.currentStaffOnly = getBoolean(ARG_CURRENT_STAFF_ONLY, true);
            values.visibleLinksOnly = getBoolean(ARG_VISIBLE_LINKS_ONLY, false);

            values.useFullUTF8 = getBoolean(ARG_USE_FULL_UTF8, false);

            values.baseURI      = getString(ARG_VIVO_BASE_URI, DEFAULT_BASE_URI);
            values.vivoImageDir = getString(ARG_VIVO_IMAGE_DIR, DEFAULT_IMAGE_DIR);
            values.xslTemplate = getString(ARG_XSL_TEMPLATE);

            values.transferDir = getFileDir(argList.get(ARG_TRANSFER_DIR), DEFAULT_TRANSFER_DIR);
            values.rawOutputDir = getFileDirFromConfig(argList.get(ARG_RAW_OUTPUT_DIRECTORY), DEFAULT_RAW_OUTPUT_DIR);
            values.rdfOutputDir = getFileDirFromConfig(argList.get(ARG_RDF_OUTPUT_DIRECTORY), DEFAULT_RDF_OUTPUT_DIR);

            values.ignoreSSLErrors = getBoolean(ARG_IGNORE_SSL_ERRORS, false);

            if ("memory".equalsIgnoreCase(argList.get(ARG_LIVE_TRIPLESTORE))) {
                values.tripleStore = new MemJenaConnect().getJenaModel();
            } else if (!StringUtils.isEmpty(argList.get(ARG_LIVE_TRIPLESTORE))) {
                values.tripleStore = JenaConnect.parseConfig(argList.get(ARG_LIVE_TRIPLESTORE)).getJenaModel();
            }

            if (!StringUtils.isEmpty(values.baseURI)) {
                values.xslParameters.put("baseURI", values.baseURI);
            }

            if (!StringUtils.isEmpty(values.vivoImageDir)) {
                values.xslParameters.put("recordDir", values.vivoImageDir);
            }
        }
    }

    private static boolean getBoolean(String key, boolean defValue) {
        String value = argList.get(key);
        if (defValue) {
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }

            return true;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        return false;
    }

    private static int getInt(String key, int defValue) {
        try {
            String value = argList.get(key);
            if (value != null) {
                return Integer.parseInt(value, 10);

            }
        } catch (NumberFormatException nfe) {
            // TODO Add to error list
        }

        return defValue;
    }

    private static String getString(String key) {
        return argList.get(key);
    }

    private static String getString(String key, String defValue) {
        String str = argList.get(key);
        return StringUtils.isEmpty(str) ? defValue : str;
    }

    public static boolean isConfigured() {
        return argList != null;
    }

    public static String getUsage() {
        if (parser != null) {
            return parser.getUsage();
        }

        return "Error generating usage string";
    }

    private static String getFileDir(String fileDir, String defValue) {
        if (!StringUtils.isEmpty(fileDir)) {
            if (fileDir.contains("/")) {
                if (!fileDir.endsWith("/")) {
                    fileDir = fileDir + "/";
                }
            } else if (fileDir.contains("\\")) {
                if (!fileDir.endsWith("\\")) {
                    fileDir = fileDir + "\\";
                }
            } else {
                if (!fileDir.endsWith(File.separator)) {
                    fileDir = fileDir + File.separator;
                }
            }
        }

        return fileDir;
    }

    private static String getFileDirFromConfig(String filename, String defValue) {
        return getFileDir(getRawFileDirFromConfig(filename, defValue), defValue);
    }

    private static String getRawFileDirFromConfig(String filename, String defValue) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(file);
                if (doc != null) {
                    doc.getDocumentElement().normalize();
                    NodeList nodes = doc.getDocumentElement().getChildNodes();
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        if (node.hasAttributes()) {
                            Node nameAttr = node.getAttributes().getNamedItem("name");
                            if (nameAttr != null && "fileDir".equals( nameAttr.getTextContent() )) {
                                return node.getTextContent();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        }

        return defValue;
    }
}
