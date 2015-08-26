/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsTransferredRdfStore;

import java.io.File;

public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    //private TransferServiceConfig config = new TransferServiceConfig();

    public void transfer(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, File translatedRdf) {
        TransferServiceImpl.transfer(outputStore, objectInfo, translatedRdf);
    }

    public void transfer(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, String type, File translatedRdf) {
        TransferServiceImpl.transfer(outputStore, objectInfo, type, translatedRdf);
    }

    public void transfer(ElementsTransferredRdfStore outputStore, ElementsRelationshipInfo relationshipInfo, File translatedRdf) {
        TransferServiceImpl.transfer(outputStore, relationshipInfo, translatedRdf);
    }

    public static void shutdown() {
        TransferServiceImpl.shutdown();
    }
}
