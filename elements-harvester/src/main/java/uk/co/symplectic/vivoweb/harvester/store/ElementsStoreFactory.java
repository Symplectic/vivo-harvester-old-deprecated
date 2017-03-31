/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

public class ElementsStoreFactory {
    private static ElementsObjectStore objectStore = null;
    private static ElementsRdfStore rdfStore = null;

    // These should really be pulled from configuration
    private static final String RAW_RECORD_STORE = "data/raw-records";
    private static final String RDF_RECORD_STORE = "data/translated-records";

    public static ElementsObjectStore getObjectStore() {
        if (objectStore != null) {
            return objectStore;
        } else {
            synchronized (ElementsStoreFactory.class) {
                return objectStore != null ? objectStore : (objectStore = new ElementsObjectStore(RAW_RECORD_STORE));
            }
        }
    }

    public static ElementsRdfStore getRdfStore() {
        if (rdfStore != null) {
            return rdfStore;
        } else {
            synchronized (ElementsStoreFactory.class) {
                return rdfStore != null ? rdfStore : (rdfStore = new ElementsRdfStore(RDF_RECORD_STORE));
            }
        }
    }
}
