/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIFeedObjectStreamHandler;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.ResourceFetchService;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Templates;
import javax.xml.xpath.XPathConstants;
import java.io.*;
import java.net.MalformedURLException;
import java.util.List;

public class ElementsObjectHandler implements ElementsAPIFeedObjectStreamHandler {
    private ElementsObjectStore objectStore = null;
    private ElementsRdfStore rdfStore = null;

    private File vivoImageDir = null;

    private boolean currentStaffOnly = true;

    private TranslationService translationService = new TranslationService();
    private ResourceFetchService fetchService     = new ResourceFetchService();
    private Templates template = null;

    private ElementsAPI elementsApi;

    ElementsObjectHandler(ElementsAPI elementsApi, ElementsObjectStore objectStore, ElementsRdfStore rdfStore, String xslFilename) {
        this.elementsApi = elementsApi;
        this.objectStore = objectStore;
        this.rdfStore = rdfStore;
        if (!StringUtils.isEmpty(xslFilename)) {
            try {
                template = translationService.compileSource(new BufferedInputStream(new FileInputStream(xslFilename)));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("XSL Translation file not found", e);
            }
        }
    }

    public void setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
    }

    public void setVivoImageDir(File vivoImageDir) {
        this.vivoImageDir = vivoImageDir;
    }

    @Override
    public void handle(List<XMLAttribute> attributes, XMLStreamFragmentReader objectReader, String docEncoding, String docVersion) throws XMLStreamException {
        ElementsStoredObject object = objectStore.storeObject(attributes, objectReader, docEncoding, docVersion);

        boolean translateObject = true;

        if (currentStaffOnly && object.getCategory() == ElementsObjectCategory.USER) {
            ElementsUserInfo userInfo = ElementsXMLParsers.parseUserInfo(object.getFile());
            if (userInfo != null && vivoImageDir != null) {
                translateObject = userInfo.getIsCurrentStaff();
                if (!StringUtils.isEmpty(userInfo.getPhotoUrl())) {
                    if (elementsApi != null) {
                        try {
                            fetchService.fetchElements(elementsApi, userInfo.getPhotoUrl(), objectStore.generateResourceHandle(attributes, "photo"),
                                    new ElementsUserPhotosFetchCallback(attributes, rdfStore, vivoImageDir, null)
                            );
                        } catch (MalformedURLException mue) {
                            // Log error
                        }
                    } else {
                        // Log missing API object
                    }
                }
            }
        }

        if (translateObject) {
            File outFile = rdfStore.getObjectFile(attributes);

            /**
             * Note that the translation service is designed to be asynchronous. Which means, when the translate() method
             * call returns, we are not guaranteed that the translation will have completed (in fact, we can be almost certain
             * that is WON'T have finished translating by the time that the method call returns.
             *
             * As a result, we can't do anything that relies on the translation having been completed by coding it after the
             * method call. For example, cleaning up empty files output from the translation.
             *
             * In order to get round this, a callback object can be supplied, which will execute after the translation code
             * has completed.
             *
             * In this case, we supply an object that will clean up any empty translation output.
             */
            translationService.translate(object.getFile(), outFile, template, new ElementsDeleteEmptyTranslationCallback(outFile));
        }
    }
}
