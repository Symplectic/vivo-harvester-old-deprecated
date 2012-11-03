/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.apache.commons.io.IOUtils;
import uk.co.symplectic.xml.StAXUtils;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main Elements API Client class
 */
public class ElementsAPI {
    // Definitions of available API versions
    // IF you add any value here, you must update getAPI()
    public static final String VERSION_3_7    = "3.7";
    public static final String VERSION_3_7_16 = "3.7.16";

    // The API version URL builder (needs to be turned into an template to handle multiple categories).
    private ElementsAPIURLBuilder urlBuilder = null;

    private String url = null;

    private String username = null;
    private String password = null;

    private boolean isSecured = true;

    /**
     * Factory method to obtain a handle to the API class, configured for a specific API version
     *
     * Throws IllegalStateException if the URL is invalid, or IllegalArgumentException if the version is unknown
     *
     * @param version
     * @param url
     * @param isSecured
     * @return
     */
    public static ElementsAPI getAPI(String version, String url, boolean isSecured) {
        ElementsAPI api = getAPI(version);
        api.setUrl(url);
        api.setIsSecured(isSecured);

        ElementsAPIURLValidator validator = new ElementsAPIURLValidator(api.url, api.isSecured);
        if (!validator.isValid()) {
            throw new IllegalStateException(validator.getLastValidationMessage());
        }

        return api;
    }

    /**
     * Factory method to obtain a handle to the API class, configured for a specific API version
     *
     * Throws IllegalStateException if the URL is invalid, or IllegalArgumentException if the version is unknown
     *
     * @param version
     * @param url
     * @return
     */
    public static ElementsAPI getAPI(String version, String url) {
        ElementsAPI api = getAPI(version);
        api.setUrl(url);

        ElementsAPIURLValidator validator = new ElementsAPIURLValidator(api.url, api.isSecured);
        if (!validator.isValid()) {
            throw new IllegalStateException(validator.getLastValidationMessage());
        }

        return api;
    }

    /**
     * Internal method to get the API object for a specific version.
     *
     * Throws IllegalArgumentException if the version is unknown.
     *
     * @param version
     * @return
     */
    static ElementsAPI getAPI(String version) {
        if (version == null) {
            throw new IllegalArgumentException("No version supplied");
        }

        // If the string starts with "v." or "version", etc. then strip that out to the version number
        if (Character.toLowerCase(version.charAt(0)) == 'v') {
            int firstDigitIdx = 1;
            if (version.toLowerCase().startsWith("version")) {
                firstDigitIdx = 7;
            }

            for (; firstDigitIdx < version.length(); firstDigitIdx++) {
                char currentChar = version.charAt(firstDigitIdx);
                if (Character.isDigit(currentChar)) {
                    version = version.substring(firstDigitIdx);
                    break;
                }

                if (!Character.isWhitespace(currentChar) && currentChar != '.') {
                    break;
                }
            }
        }

        return new ElementsAPI(version);
    }

    /**
     * Call the API based on the query supplied. For each object, call the supplied handler
     *
     * @param feedQuery
     * @param handler
     * @return
     */
    public ElementsFeedInfo execute(ElementsAPIFeedObjectQuery feedQuery, ElementsAPIFeedObjectHandler handler) {
        ElementsFeedInfo info = new ElementsFeedInfo();
        ElementsAPIFeedEntryObjectParser parser = new ElementsAPIFeedEntryObjectParser(handler, feedQuery.getFullDetails());

        String queryUrl = urlBuilder.buildObjectFeedQuery(url, feedQuery);

        ElementsFeedPagination pagination = executeQuery(queryUrl, parser);
        if (pagination != null && feedQuery.getProcessAllPages()) {
            while (pagination.getNextURL() != null) {
                pagination = executeQuery(pagination.getNextURL(), parser);
            }
        }

        return info;
    }

    /**
     * Call the API based on the query supplied. For each object, call the supplied handler
     *
     * @param relationshipFeedQuery
     * @param handler
     * @return
     */
    public ElementsFeedInfo execute(ElementsAPIFeedRelationshipQuery relationshipFeedQuery, ElementsAPIFeedRelationshipHandler handler) {
        ElementsFeedInfo info = new ElementsFeedInfo();
        ElementsAPIFeedEntryRelationshipParser parser = new ElementsAPIFeedEntryRelationshipParser(handler);

        String queryUrl = urlBuilder.buildRelationshipFeedQuery(url, relationshipFeedQuery);

        ElementsFeedPagination pagination = executeQuery(queryUrl, parser);
        if (pagination != null && relationshipFeedQuery.getProcessAllPages()) {
            while (pagination.getNextURL() != null) {
                pagination = executeQuery(pagination.getNextURL(), parser);
            }
        }

        return info;
    }

    public boolean fetchResource(String resourceURL, OutputStream outputStream) {
        InputStream apiResponse = null;
        try {
            ElementsAPIHttpClient apiClient;
            if (isSecured) {
                apiClient = new ElementsAPIHttpClient(resourceURL, username, password);
            } else {
                apiClient = new ElementsAPIHttpClient(resourceURL);
            }

            apiResponse = apiClient.executeGetRequest();
            IOUtils.copy(apiResponse, outputStream);
        } catch (IOException e) {
        } finally {
            if (apiResponse != null) {
                try {
                    apiResponse.close();
                } catch (IOException e) {

                }
            }
        }

        return true;
    }

    /**
     * Executes a single query. If paginated, may be called multiple times by execute()
     * @param url
     * @param parser
     * @return
     */
    private ElementsFeedPagination executeQuery(String url, ElementsFeedEntryParser parser) {
        InputStream apiResponse = null;
        try {
            ElementsAPIHttpClient apiClient;
            if (isSecured) {
                apiClient = new ElementsAPIHttpClient(url, username, password);
            } else {
                apiClient = new ElementsAPIHttpClient(url);
            }

            apiResponse = apiClient.executeGetRequest();
            return parseResponse(apiResponse, parser);
        } catch (IOException e) {
        } catch (XMLStreamException e) {
        } finally {
            if (apiResponse != null) {
                try {
                    apiResponse.close();
                } catch (IOException e) {

                }
            }
        }

        return null;
    }

    private ElementsFeedPagination parseResponse(InputStream response, ElementsFeedEntryParser parser) throws XMLStreamException {
        ElementsFeedPagination pagination = new ElementsFeedPagination();
        XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
        XMLStreamReader atomReader = xmlInputFactory.createXMLStreamReader(response);

        while (atomReader.hasNext()) {
            switch (atomReader.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    parser.setEncoding(atomReader.getEncoding());
                    parser.setVersion(atomReader.getVersion());
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    String prefix = atomReader.getPrefix();
                    String name = atomReader.getLocalName();
                    if ("entry".equals(name)) {
                        parser.parseEntry(new XMLStreamFragmentReader(atomReader));
                    } else if ("api".equals(prefix) && "pagination".equals(name)) {
                        for (int attIdx = 0; attIdx < atomReader.getAttributeCount(); attIdx++) {
                            if ("items-per-page".equals(atomReader.getAttributeLocalName(attIdx))) {
                                pagination.setItemsPerPage(Integer.parseInt(atomReader.getAttributeValue(attIdx)));
                            }
                        }
                    } else if ("api".equals(prefix) && "page".equals(name)) {
                        String position = null;
                        String href = null;
                        for (int attIdx = 0; attIdx < atomReader.getAttributeCount(); attIdx++) {
                            if ("position".equals(atomReader.getAttributeLocalName(attIdx))) {
                                position = atomReader.getAttributeValue(attIdx);
                            } else if ("href".equals(atomReader.getAttributeLocalName(attIdx))) {
                                href = atomReader.getAttributeValue(attIdx);
                            }
                        }

                        if (position != null && href != null) {
                            if ("first".equals(position)) {
                                pagination.setFirstURL(href);
                            } else if ("last".equals(position)) {
                                pagination.setLastURL(href);
                            } else if ("previous".equals(position)) {
                                pagination.setPreviousURL(href);
                            } else if ("next".equals(position)) {
                                pagination.setNextURL(href);
                            }
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    break;
            }

            if (atomReader.hasNext()) {
                atomReader.next();
            }
        }

        return pagination;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private ElementsAPI(String version) {
        if (VERSION_3_7_16.equals(version)) {
            urlBuilder = new ElementsAPIv3_7_16URLBuilder();
        } else if (VERSION_3_7.equals(version)) {
            urlBuilder = new ElementsAPIv3_7URLBuilder();
        } else {
            throw new IllegalArgumentException("Unsupported version");
        }
    }

    private void setUrl(String url) {
        if (url.endsWith("/")) {
            this.url = url;
        } else {
            this.url = url + "/";
        }
    }

    private void setIsSecured(boolean isSecured) {
        this.isSecured = isSecured;
    }
}
