/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import org.vivoweb.harvester.util.repo.JenaConnect;
import uk.co.symplectic.vivoweb.harvester.jena.JenaWrapper;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStoreObserver;
import uk.co.symplectic.vivoweb.harvester.store.ElementsTransferredRdfStore;

import java.io.File;

public class TransferElementsRdfStoreObserver implements ElementsRdfStoreObserver {
    private TransferElementsRdfStoreObserver() { }

    private JenaWrapper outputStore;
    private ElementsTransferredRdfStore transferredRdfStore;

    private TransferService transferService = new TransferService();

    public static TransferElementsRdfStoreObserver create() {
        return new TransferElementsRdfStoreObserver();
    }

    public TransferElementsRdfStoreObserver setTransferredRdfStore(ElementsTransferredRdfStore transferredRdfStore) {
        this.transferredRdfStore = transferredRdfStore;
        return this;

    }

    public TransferElementsRdfStoreObserver setTripleStore(JenaWrapper outputStore) {
        this.outputStore = outputStore;
        return this;
    }

    @Override
    public void storedObjectRdf(ElementsObjectInfo objectInfo, File storedRdf) {
        transferService.transfer(outputStore, transferredRdfStore.getObjectFile(objectInfo), storedRdf);
    }

    @Override
    public void storedObjectExtraRdf(ElementsObjectInfo objectInfo, String type, File storedRdf) {
        transferService.transfer(outputStore, transferredRdfStore.getObjectExtraFile(objectInfo, type), storedRdf);
    }

    @Override
    public void storedRelationshipRdf(ElementsRelationshipInfo relationshipInfoInfo, File storedRdf) {
        transferService.transfer(outputStore, transferredRdfStore.getRelationshipFile(relationshipInfoInfo), storedRdf);
    }
}
