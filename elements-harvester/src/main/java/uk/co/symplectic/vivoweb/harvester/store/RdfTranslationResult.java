/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.translate.TranslationResult;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class RdfTranslationResult implements TranslationResult {
    private static final Logger log = LoggerFactory.getLogger(RdfTranslationResult.class);
    private ElementsRdfStore rdfStore;

    private ElementsObjectInfo objectInfo = null;
    private ElementsRelationshipInfo relationshipInfo = null;

    private FileFormat outputFormat = FileFormat.RDF_XML;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    RdfTranslationResult(ElementsRdfStore rdfStore, ElementsObjectInfo objectInfo, FileFormat outputFormat) {
        this.rdfStore = rdfStore;
        this.objectInfo = objectInfo;
        this.outputFormat = outputFormat;
    }
    RdfTranslationResult(ElementsRdfStore rdfStore, ElementsRelationshipInfo relationshipInfo, FileFormat outputFormat) {
        this.rdfStore = rdfStore;
        this.relationshipInfo = relationshipInfo;
        this.outputFormat = outputFormat;
    }

    @Override
    public Result result() {
        return new StreamResult(baos);
    }

    @Override
    public void release() throws IOException {
        byte[] arr = null;

        if (Configuration.getUseFullUTF8()) {
            arr = baos.toByteArray();
        } else {
            String xml;
            try {
                xml = baos.toString("utf-8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("Something serious went wrong, can't parse utf-8");
            }

            xml = xml.replaceAll("[^\\u0000-\\uFFFF]", "\uFFFD");
            arr = xml.getBytes("utf-8");
        }

        if (objectInfo != null) {
            rdfStore.writeObject(objectInfo, arr, outputFormat);
        } else if (relationshipInfo != null) {
            rdfStore.writeRelationship(relationshipInfo, arr, outputFormat);
        }
    }
}
