/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.utils.ExecutorServiceUtils;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsTransferredRdfStore;

import java.io.File;
import java.util.concurrent.Callable;

final public class TransferServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);
    private static final ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("TransferService");

    private TransferServiceImpl() { }

    static void transfer(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, File translatedRdf) {
        wrapper.submit(new TransferObjectHandler(outputStore, objectInfo, translatedRdf));
    }

    static void transfer(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, String type, File translatedRdf) {
        wrapper.submit(new TransferObjectExtraHandler(outputStore, objectInfo, type, translatedRdf));
    }

    static void transfer(ElementsTransferredRdfStore outputStore, ElementsRelationshipInfo relationshipInfo, File translatedRdf) {
        wrapper.submit(new TransferRelationshipHandler(outputStore, relationshipInfo, translatedRdf));
    }

    static class TransferObjectHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;

        private ElementsObjectInfo objectInfo;

        TransferObjectHandler(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, File translatedRdf) {
            this.outputStore   = outputStore;
            this.objectInfo    = objectInfo;
            this.translatedRdf = translatedRdf;
        }

        public Boolean call() throws Exception {
            outputStore.replaceObjectRdf(objectInfo, translatedRdf);
            return true;
        }
    }

    static class TransferObjectExtraHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;

        private ElementsObjectInfo objectInfo;
        private String type;

        TransferObjectExtraHandler(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, String type, File translatedRdf) {
            this.outputStore   = outputStore;
            this.objectInfo    = objectInfo;
            this.type          = type;
            this.translatedRdf = translatedRdf;
        }

        public Boolean call() throws Exception {
            outputStore.replaceObjectExtraRdf(objectInfo, type, translatedRdf);
            return true;
        }
    }

    static class TransferRelationshipHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;

        private ElementsRelationshipInfo relationshipInfo;

        TransferRelationshipHandler(ElementsTransferredRdfStore outputStore, ElementsRelationshipInfo relationshipInfo, File translatedRdf) {
            this.outputStore      = outputStore;
            this.relationshipInfo = relationshipInfo;
            this.translatedRdf    = translatedRdf;
        }

        public Boolean call() throws Exception {
            outputStore.replaceRelationshipRdf(relationshipInfo, translatedRdf);
            return true;
        }
    }


    static void shutdown() {
        wrapper.shutdown();
    }
}
