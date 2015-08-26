/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;

public class ElementsStoreFactory {
    private static ElementsObjectStore objectStore = null;
    private static ElementsRdfStore rdfStore = null;
    private static ElementsTransferredRdfStore transferStore = null;

    public static ElementsObjectStore getObjectStore() {
        if (objectStore != null) {
            return objectStore;
        } else {
            synchronized (ElementsStoreFactory.class) {
                return objectStore != null ? objectStore : (objectStore = new ElementsObjectStore(Configuration.getRawOutputDir()));
            }
        }
    }

    public static ElementsRdfStore getRdfStore() {
        if (rdfStore != null) {
            return rdfStore;
        } else {
            synchronized (ElementsStoreFactory.class) {
                return rdfStore != null ? rdfStore : (rdfStore = new ElementsRdfStore(Configuration.getRdfOutputDir()));
            }
        }
    }

    public static ElementsTransferredRdfStore getTransferredRdfStore() {
        if (transferStore != null) {
            return transferStore;
        } else {
            Model tripleStore  = Configuration.getTripleStore();
            String transferDir = Configuration.getTransferDir();
            if (tripleStore != null && !StringUtils.isEmpty(transferDir)) {
                synchronized (ElementsStoreFactory.class) {
                    return transferStore != null ? transferStore :
                            (transferStore = new ElementsTransferredRdfStore(tripleStore, transferDir));
                }
            }
        }

        return null;
    }
}
