/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStoreObserver;
import uk.co.symplectic.vivoweb.harvester.store.ElementsTransferredRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.FileFormat;

import java.io.File;

/**
 * Observer to transfer data into a triple store when it is written to the RDF store
 */
public class TransferElementsRdfStoreObserver implements ElementsRdfStoreObserver {
    private TransferElementsRdfStoreObserver() { }

    // Location for RDF/XML that has been loaded into the triple store
    private ElementsTransferredRdfStore transferredRdfStore;

    private TransferService transferService = new TransferService();

    public static TransferElementsRdfStoreObserver create() {
        return new TransferElementsRdfStoreObserver();
    }

    public TransferElementsRdfStoreObserver setTransferredRdfStore(ElementsTransferredRdfStore transferredRdfStore) {
        this.transferredRdfStore = transferredRdfStore;
        return this;

    }

    @Override
    public void storedObjectRdf(ElementsObjectInfo objectInfo, File storedRdf, FileFormat storedFormat) {
        transferService.transferObjectRdf(transferredRdfStore, objectInfo, storedRdf, storedFormat);
    }

    @Override
    public void storedObjectExtraRdf(ElementsObjectInfo objectInfo, String type, File storedRdf, FileFormat storedFormat) {
        transferService.transferObjectExtraRdf(transferredRdfStore, objectInfo, type, storedRdf, storedFormat);
    }

    @Override
    public void storedRelationshipRdf(ElementsRelationshipInfo relationshipInfo, File storedRdf, FileFormat storedFormat) {
        transferService.transferRelationshipRdf(transferredRdfStore, relationshipInfo, storedRdf, storedFormat);
    }
}
