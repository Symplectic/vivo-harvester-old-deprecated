/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.translate.TranslationTask;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.ResourceFetchService;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.translate.ElementsObjectTranslateStagesObserver;

import java.io.File;
import java.net.MalformedURLException;

public class ElementsUserPhotoRetrievalObserver implements ElementsObjectTranslateStagesObserver {
    private final ResourceFetchService fetchService = new ResourceFetchService();
    private ElementsObjectStore objectStore = null;
    private ElementsRdfStore rdfStore = null;
    private File vivoImageDir = null;
    private String vivoBaseURI = null;

    private ElementsAPI elementsApi;

    private ElementsUserPhotoRetrievalObserver() { }

    public static ElementsUserPhotoRetrievalObserver create() {
        return new ElementsUserPhotoRetrievalObserver();
    }

    public ElementsUserPhotoRetrievalObserver setElementsAPI(ElementsAPI elementsApi) {
        this.elementsApi = elementsApi;
        return this;
    }

    public ElementsUserPhotoRetrievalObserver setObjectStore(ElementsObjectStore objectStore) {
        this.objectStore = objectStore;
        return this;
    }

    public ElementsUserPhotoRetrievalObserver setRdfStore(ElementsRdfStore rdfStore) {
        this.rdfStore = rdfStore;
        return this;
    }

    public ElementsUserPhotoRetrievalObserver setImageDir(File vivoImageDir) {
        this.vivoImageDir = vivoImageDir;
        return this;
    }

    public ElementsUserPhotoRetrievalObserver setBaseURI(String vivoBaseURI) {
        this.vivoBaseURI = vivoBaseURI;
        return this;
    }

    @Override
    public void beingTranslated(final TranslationTask task, final ElementsObjectInfo objectInfo) {
        if (objectInfo instanceof ElementsUserInfo) {
            ElementsUserInfo userInfo = (ElementsUserInfo)objectInfo;
            if (!StringUtils.isEmpty(userInfo.getPhotoUrl())) {
                if (elementsApi != null) {
                    try {
                        fetchService.fetchElements(elementsApi, userInfo.getPhotoUrl(), objectStore.generateResourceHandle(objectInfo, "photo"),
                                new ElementsUserPhotosFetchCallback(userInfo, rdfStore, vivoImageDir, vivoBaseURI, null)
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
