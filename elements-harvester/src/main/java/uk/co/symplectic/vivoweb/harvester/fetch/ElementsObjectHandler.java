/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPIFeedObjectStreamHandler;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Templates;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ElementsObjectHandler implements ElementsAPIFeedObjectStreamHandler {
    private List<ElementsObjectObserver> objectObservers = new ArrayList<ElementsObjectObserver>();
    private ElementsObjectStore objectStore = null;

    ElementsObjectHandler(ElementsObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public void addObjectObserver(ElementsObjectObserver newObserver) {
        objectObservers.add(newObserver);
    }

    @Override
    public void handle(List<XMLAttribute> attributes, XMLStreamFragmentReader objectReader, String docEncoding, String docVersion) throws XMLStreamException {
        ElementsStoredObject object = objectStore.storeObject(attributes, objectReader, docEncoding, docVersion);

        for (ElementsObjectObserver objectObserver : objectObservers) {
            objectObserver.observe(object);
        }
    }
}
