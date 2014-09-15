package uk.co.symplectic.vivoweb.harvester.app;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIHttpClient;
import uk.co.symplectic.utils.ExecutorServiceUtils;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsFetch;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsUserPhotoRetrievalObserver;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoreFactory;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsObjectTranslateObserver;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsRelationshipTranslateObserver;

import java.io.File;
import java.io.IOException;

public class ElementsFetchAndTranslate {
    private static final String ARG_RAW_OUTPUT_DIRECTORY = "rawOutput";
    private static final String ARG_RDF_OUTPUT_DIRECTORY = "rdfOutput";

    private static final String ARG_XSL_TEMPLATE         = "xslTemplate";

    private static final String ARG_ELEMENTS_API_ENDPOINT = "apiEndpoint";
    private static final String ARG_ELEMENTS_API_SECURE   = "apiIsSecure";
    private static final String ARG_ELEMENTS_API_VERSION  = "apiVersion";
    private static final String ARG_ELEMENTS_API_USERNAME = "apiUsername";
    private static final String ARG_ELEMENTS_API_PASSWORD = "apiPassword";

    private static final String ARG_CURRENT_STAFF_ONLY    = "currentStaffOnly";
    private static final String ARG_VISIBLE_LINKS_ONLY    = "visibleLinksOnly";

    private static final String ARG_VIVO_IMAGE_DIR        = "vivoImageDir";
    private static final String ARG_VIVO_BASE_URI         = "vivoBaseURI";

    private static final String ARG_API_QUERY_OBJECTS     = "queryObjects";
    private static final String ARG_API_PARAMS_GROUPS     = "paramGroups";
    private static final String ARG_API_PAGE              = "page";

    private static final String ARG_API_OBJECTS_PER_PAGE  = "objectsPerPage";
    private static final String ARG_API_RELS_PER_PAGE     = "relationshipsPerPage";

    private static final String ARG_API_SOCKET_TIMEOUT    = "apiSocketTimeout";
    private static final String ARG_API_REQUEST_DELAY     = "apiRequestDelay";

    private static final String ARG_MAX_XSL_THREADS       = "maxXslThreads";
    private static final String ARG_MAX_RESOURCE_THREADS  = "maxResourceThreads";

    /**
     * SLF4J Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ElementsFetchAndTranslate.class);

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

        parser.addArgument(new ArgDef().setShortOption('g').setLongOpt(ARG_API_PARAMS_GROUPS).setDescription("Groups to restrict queries to").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('d').setLongOpt(ARG_API_PAGE).setDescription("Restrict Page #").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('e').setLongOpt(ARG_ELEMENTS_API_ENDPOINT).setDescription("Elements API endpoint url").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('s').setLongOpt(ARG_ELEMENTS_API_SECURE).setDescription("Is Elements API secure").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('c').setLongOpt(ARG_API_QUERY_OBJECTS).setDescription("Elements API object categories").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('v').setLongOpt(ARG_ELEMENTS_API_VERSION).setDescription("Elements API version").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setShortOption('u').setLongOpt(ARG_ELEMENTS_API_USERNAME).setDescription("Elements API username").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('p').setLongOpt(ARG_ELEMENTS_API_PASSWORD).setDescription("Elements API password").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_CURRENT_STAFF_ONLY).setDescription("Current Staff Only").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_VISIBLE_LINKS_ONLY).setDescription("Visible Links Only").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_VIVO_IMAGE_DIR).setDescription("Vivo Image Directory").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_VIVO_BASE_URI).setDescription("Vivo Base URI").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_API_OBJECTS_PER_PAGE).setDescription("Objects Per Page").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_API_RELS_PER_PAGE).setDescription("Relationships Per Page").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_API_SOCKET_TIMEOUT).setDescription("HTTP Socket Timeout").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_API_REQUEST_DELAY).setDescription("API Request Delay").withParameter(true, "CONFIG_FILE"));

        parser.addArgument(new ArgDef().setLongOpt(ARG_MAX_XSL_THREADS).setDescription("Maximum number of Threads to use for the XSL Translation").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setLongOpt(ARG_MAX_RESOURCE_THREADS).setDescription("Maximum number of Threads to use for the Resource (photo) downloads").withParameter(true, "CONFIG_FILE"));

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
            parser = getParser("ElementsFetchAndTranslate");
            try {
                InitLog.initLogger(args, parser);
                log.debug("ElementsFetchAndTranslate: Start");

                ArgList parsedArgs = parser.parse(args);

                setExecutorServiceMaxThreadsForPool("TranslationService",   parsedArgs.get(ARG_MAX_XSL_THREADS));
                setExecutorServiceMaxThreadsForPool("ResourceFetchService", parsedArgs.get(ARG_MAX_RESOURCE_THREADS));

                ElementsAPI elementsAPI = ElementsFetchAndTranslate.getElementsAPI(parsedArgs);
                ElementsFetch fetcher = new ElementsFetch(elementsAPI);

                fetcher.setGroupsToHarvest(ElementsFetchAndTranslate.getGroupsToHarvest(parsedArgs));
                fetcher.setObjectsToHarvest(ElementsFetchAndTranslate.getObjectsToHarvest(parsedArgs));
                fetcher.setObjectsPerPage(ElementsFetchAndTranslate.getObjectsPerPage(parsedArgs));
                fetcher.setRelationshipsPerPage(ElementsFetchAndTranslate.getRelationshipsPerPage(parsedArgs));
                fetcher.setPageToHarvest(ElementsFetchAndTranslate.getPage(parsedArgs));

                ElementsObjectStore objectStore = ElementsStoreFactory.getObjectStore();
                ElementsRdfStore rdfStore = ElementsStoreFactory.getRdfStore();

                boolean currentStaffOnly = ElementsFetchAndTranslate.getCurrentStaffOnly(parsedArgs);
                boolean visibleLinksOnly = ElementsFetchAndTranslate.getVisibleLinksOnly(parsedArgs);

                String xslFilename = ElementsFetchAndTranslate.getXslFilename(parsedArgs);
                File vivoImageDir = ElementsFetchAndTranslate.getVivoImageDir(parsedArgs);
                String vivoBaseURI = ElementsFetchAndTranslate.getBaseURI(parsedArgs);

                ElementsObjectTranslateObserver objectObserver = new ElementsObjectTranslateObserver(rdfStore, xslFilename);
                objectObserver.setCurrentStaffOnly(currentStaffOnly);
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
            if (parser != null) {
                log.info("Printing Usage:");
                System.out.println(parser.getUsage());
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

    private static ElementsAPI getElementsAPI(ArgList argList) {
        String apiEndpoint = argList.get(ARG_ELEMENTS_API_ENDPOINT);
        String apiVersion = argList.get(ARG_ELEMENTS_API_VERSION);

        String apiUsername = argList.get(ARG_ELEMENTS_API_USERNAME);
        String apiPassword = argList.get(ARG_ELEMENTS_API_PASSWORD);

        boolean apiIsSecure;
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

        String strSoTimeout = argList.get(ARG_API_SOCKET_TIMEOUT);
        if (!StringUtils.isEmpty(strSoTimeout)) {
            int soTimeout = Integer.parseInt(strSoTimeout, 10);
            if (soTimeout > 4999 && soTimeout < (30 * 60 * 1000)) {
                ElementsAPIHttpClient.setSoTimeout(soTimeout);
            }
        }

        String strRequestDelay = argList.get(ARG_API_REQUEST_DELAY);
        if (!StringUtils.isEmpty(strRequestDelay)) {
            int requestDelay = Integer.parseInt(strRequestDelay, 10);
            if (requestDelay > -1 && requestDelay < (5 * 60 * 1000)) {
                ElementsAPIHttpClient.setRequestDelay(requestDelay);
            }
        }

        ElementsAPI api = ElementsAPI.getAPI(apiVersion, apiEndpoint, apiIsSecure);
        if (apiIsSecure) {
            api.setUsername(apiUsername);
            api.setPassword(apiPassword);
        }

        return api;
    }

    private static String getGroupsToHarvest(ArgList argList) {
        return argList.get(ARG_API_PARAMS_GROUPS);
    }

    private static String getObjectsToHarvest(ArgList argList) {
        return argList.get(ARG_API_QUERY_OBJECTS);
    }

    private static String getPage(ArgList argList) {
        return argList.get(ARG_API_PAGE);
    }

    private static File getVivoImageDir(ArgList argList) {
        File vivoImageDir = null;
        String vivoImageDirArg = argList.get(ARG_VIVO_IMAGE_DIR);
        if (!StringUtils.isEmpty(vivoImageDirArg)) {
            vivoImageDir = new File(vivoImageDirArg);
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

    private static String getXslFilename(ArgList argList) {
        return argList.get(ARG_XSL_TEMPLATE);
    }

    private static String getBaseURI(ArgList argList) {
        String uri = argList.get(ARG_VIVO_BASE_URI);
        if (StringUtils.isEmpty(uri)) {
            return "http://vivo.symplectic.co.uk/";
        }

        return uri;
    }

    private static boolean getCurrentStaffOnly(ArgList argList) {
        String currentStaffArg = argList.get(ARG_CURRENT_STAFF_ONLY);
        if ("false".equalsIgnoreCase(currentStaffArg)) {
            return false;
        }

        return true;
    }

    private static boolean getVisibleLinksOnly(ArgList argList) {
        String visibleLinksArg = argList.get(ARG_VISIBLE_LINKS_ONLY);
        if ("false".equalsIgnoreCase(visibleLinksArg)) {
            return false;
        }

        return true;
    }

    private static int getObjectsPerPage(ArgList argList) {
        String strObjectsPerPage = argList.get(ARG_API_OBJECTS_PER_PAGE);
        if (!StringUtils.isEmpty(strObjectsPerPage)) {
            int tmpObjectsPerPage = Integer.parseInt(strObjectsPerPage, 10);
            if (tmpObjectsPerPage > 0 && tmpObjectsPerPage < 1001) {
                return tmpObjectsPerPage;
            }
        }

        return 25;
    }

    private static int getRelationshipsPerPage(ArgList argList) {
        String strRelsPerPage = argList.get(ARG_API_RELS_PER_PAGE);
        if (!StringUtils.isEmpty(strRelsPerPage)) {
            int tmpRelsPerPage = Integer.parseInt(strRelsPerPage, 10);
            if (tmpRelsPerPage > 0 && tmpRelsPerPage < 501) {
                return tmpRelsPerPage;
            }
        }

        return 25;
    }

    private static void setExecutorServiceMaxThreadsForPool(String poolName, String maxThreads) {
        if (!StringUtils.isEmpty(maxThreads)) {
            int maxThreadsAsInt = Integer.parseInt(maxThreads, 10);
            if (maxThreadsAsInt > 0) {
                ExecutorServiceUtils.setMaxProcessorsForPool(poolName, maxThreadsAsInt);
            }
        }
    }
}
