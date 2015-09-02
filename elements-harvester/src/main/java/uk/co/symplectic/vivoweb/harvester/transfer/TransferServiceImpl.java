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
import uk.co.symplectic.vivoweb.harvester.store.FileFormat;

import java.io.File;
import java.util.concurrent.Callable;

final public class TransferServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);
    private static final ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("TransferService");

    private TransferServiceImpl() { }

    static void transferObjectRdf(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, File translatedRdf, FileFormat rdfFormat) {
        wrapper.submit(new TransferObjectHandler(outputStore, objectInfo, translatedRdf, rdfFormat));
    }

    static void transferObjectExtraRdf(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, String type, File translatedRdf, FileFormat rdfFormat) {
        wrapper.submit(new TransferObjectExtraHandler(outputStore, objectInfo, type, translatedRdf, rdfFormat));
    }

    static void transferRelationshipRdf(ElementsTransferredRdfStore outputStore, ElementsRelationshipInfo relationshipInfo, File translatedRdf, FileFormat rdfFormat) {
        wrapper.submit(new TransferRelationshipHandler(outputStore, relationshipInfo, translatedRdf, rdfFormat));
    }

    static class TransferObjectHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;
        private FileFormat rdfFormat;

        private ElementsObjectInfo objectInfo;

        TransferObjectHandler(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, File translatedRdf, FileFormat rdfFormat) {
            this.outputStore   = outputStore;
            this.objectInfo    = objectInfo;
            this.translatedRdf = translatedRdf;
            this.rdfFormat     = rdfFormat;
        }

        public Boolean call() throws Exception {
            outputStore.replaceObjectRdf(objectInfo, translatedRdf, rdfFormat);
            return true;
        }
    }

    static class TransferObjectExtraHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;
        private FileFormat rdfFormat;

        private ElementsObjectInfo objectInfo;
        private String type;

        TransferObjectExtraHandler(ElementsTransferredRdfStore outputStore, ElementsObjectInfo objectInfo, String type, File translatedRdf, FileFormat rdfFormat) {
            this.outputStore   = outputStore;
            this.objectInfo    = objectInfo;
            this.type          = type;
            this.translatedRdf = translatedRdf;
            this.rdfFormat     = rdfFormat;
        }

        public Boolean call() throws Exception {
            outputStore.replaceObjectExtraRdf(objectInfo, type, translatedRdf, rdfFormat);
            return true;
        }
    }

    static class TransferRelationshipHandler implements Callable<Boolean> {
        private ElementsTransferredRdfStore outputStore;
        private File translatedRdf;
        private FileFormat rdfFormat;

        private ElementsRelationshipInfo relationshipInfo;

        TransferRelationshipHandler(ElementsTransferredRdfStore outputStore, ElementsRelationshipInfo relationshipInfo, File translatedRdf, FileFormat rdfFormat) {
            this.outputStore      = outputStore;
            this.relationshipInfo = relationshipInfo;
            this.translatedRdf    = translatedRdf;
            this.rdfFormat        = rdfFormat;
        }

        public Boolean call() throws Exception {
            outputStore.replaceRelationshipRdf(relationshipInfo, translatedRdf, rdfFormat);
            return true;
        }
    }


    static long getQueueSize() {
        return wrapper.getQueueSize(); }

    static void shutdown() {
        wrapper.shutdown();
    }
}
