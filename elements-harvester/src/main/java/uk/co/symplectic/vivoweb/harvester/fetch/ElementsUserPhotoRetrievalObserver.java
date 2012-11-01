/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.ResourceFetchService;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;

import java.io.File;
import java.net.MalformedURLException;

public class ElementsUserPhotoRetrievalObserver implements ElementsObjectObserver {
    private ResourceFetchService fetchService = new ResourceFetchService();
    private ElementsObjectStore objectStore = null;
    private ElementsRdfStore rdfStore = null;
    private File vivoImageDir = null;

    private ElementsAPI elementsApi;

    ElementsUserPhotoRetrievalObserver(ElementsAPI elementsApi, ElementsObjectStore objectStore, ElementsRdfStore rdfStore, File vivoImageDir) {
        this.elementsApi  = elementsApi;
        this.objectStore = objectStore;
        this.rdfStore = rdfStore;
        this.vivoImageDir = vivoImageDir;
    }

    @Override
    public void beingTranslated(ElementsObjectInfo objectInfo) {
        if (objectInfo instanceof ElementsUserInfo) {
            ElementsUserInfo userInfo = (ElementsUserInfo)objectInfo;
            if (!StringUtils.isEmpty(userInfo.getPhotoUrl())) {
                if (elementsApi != null) {
                    try {
                        fetchService.fetchElements(elementsApi, userInfo.getPhotoUrl(), objectStore.generateResourceHandle(objectInfo, "photo"),
                                new ElementsUserPhotosFetchCallback(userInfo, rdfStore, vivoImageDir, null)
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
}
