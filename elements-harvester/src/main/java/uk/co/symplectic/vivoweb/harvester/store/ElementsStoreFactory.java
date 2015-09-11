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

    public synchronized static ElementsObjectStore getObjectStore() {
        if (objectStore == null) {
            objectStore = new ElementsObjectStore(Configuration.getRawOutputDir());
        }

        return objectStore;
    }

    public synchronized static ElementsRdfStore getRdfStore() {
        if (rdfStore == null) {
            rdfStore = new ElementsRdfStore(Configuration.getRdfOutputDir());
        }
        return rdfStore;
    }

    public synchronized static ElementsTransferredRdfStore getTransferredRdfStore() {
        if (transferStore == null) {
            Model tripleStore    = Configuration.getAssertedModel();
            String transferDir = Configuration.getTransferDir();

            Model inferenceStore = Configuration.getInferenceModel();
            String inferenceGraphUri = Configuration.getInferenceModelUri();

            if (tripleStore != null && !StringUtils.isEmpty(transferDir)) {
                transferStore = new ElementsTransferredRdfStore(tripleStore, transferDir, inferenceStore, inferenceGraphUri);
            }
        }

        return transferStore;
    }
}
