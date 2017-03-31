/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import uk.co.symplectic.elements.api.ElementsAPIFeedObjectStreamHandler;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class ElementsObjectHandler implements ElementsAPIFeedObjectStreamHandler {
    private final List<ElementsObjectObserver> objectObservers = new ArrayList<ElementsObjectObserver>();
    private ElementsObjectStore objectStore = null;

    ElementsObjectHandler(ElementsObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public ElementsObjectHandler addObserver(ElementsObjectObserver newObserver) {
        objectObservers.add(newObserver);
        return this;
    }

    public ElementsObjectHandler addObservers(List<ElementsObjectObserver> newObservers) {
        if (newObservers != null) {
            for (ElementsObjectObserver newObserver : newObservers) {
                objectObservers.add(newObserver);
            }
        }
        return this;
    }

    public ElementsObjectHandler addObservers(ElementsObjectObserver... newObservers) {
        if (newObservers != null) {
            for (ElementsObjectObserver newObserver : newObservers) {
                objectObservers.add(newObserver);
            }
        }
        return this;
    }

    @Override
    public void handle(List<XMLAttribute> attributes, XMLStreamFragmentReader objectReader, String docEncoding, String docVersion) throws XMLStreamException {
        ElementsStoredObject object = objectStore.storeObject(attributes, objectReader, docEncoding, docVersion);

        for (ElementsObjectObserver objectObserver : objectObservers) {
            objectObserver.observe(object);
        }
    }
}
