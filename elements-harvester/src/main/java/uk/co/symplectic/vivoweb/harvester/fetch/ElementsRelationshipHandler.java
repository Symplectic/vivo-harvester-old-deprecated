/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIFeedRelationshipStreamHandler;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredRelationship;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Templates;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ElementsRelationshipHandler implements ElementsAPIFeedRelationshipStreamHandler {
    private final List<ElementsRelationshipObserver> relationshipObservers = new ArrayList<ElementsRelationshipObserver>();

    private ElementsObjectStore objectStore = null;

    private ElementsObjectsInRelationships objectsInRelationships = null;

    private ElementsAPI elementsApi;

    ElementsRelationshipHandler(ElementsAPI elementsApi, ElementsObjectStore objectStore, ElementsObjectsInRelationships objectsInRelationships) {
        this.elementsApi = elementsApi;
        this.objectStore = objectStore;
        this.objectsInRelationships = objectsInRelationships;
    }

    public void addObserver(ElementsRelationshipObserver newObserver) {
        relationshipObservers.add(newObserver);
    }

    @Override
    public void handle(List<XMLAttribute> attributes, XMLStreamFragmentReader objectReader, String docEncoding, String docVersion) throws XMLStreamException {
        ElementsStoredRelationship relationship = objectStore.storeRelationship(attributes, objectReader, docEncoding, docVersion);

        for (ElementsRelationshipObserver relationshipObserver : relationshipObservers) {
            relationshipObserver.observe(relationship, objectsInRelationships);
        }
    }
}
