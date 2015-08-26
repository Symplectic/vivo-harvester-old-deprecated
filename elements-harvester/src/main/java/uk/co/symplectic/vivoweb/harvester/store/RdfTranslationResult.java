/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.translate.TranslationResult;
import uk.co.symplectic.vivoweb.harvester.config.Configuration;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RdfTranslationResult implements TranslationResult {
    private static final Logger log = LoggerFactory.getLogger(RdfTranslationResult.class);
    private static FileTempMemStore fileMemStore = new FileTempMemStore();
    private File output;

    private ElementsObjectInfo objectInfo = null;
    private ElementsRelationshipInfo relationshipInfo = null;

    private boolean keepEmpty = false;

    private final List<ElementsRdfStoreObserver> storeObservers = new ArrayList<ElementsRdfStoreObserver>();

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    RdfTranslationResult(ElementsObjectInfo objectInfo, File output) {
        this.output = output;
        this.objectInfo = objectInfo;
    }
    RdfTranslationResult(ElementsRelationshipInfo relationshipInfo, File output) {
        this.output = output;
        this.relationshipInfo = relationshipInfo;
    }

    public RdfTranslationResult setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
        return this;
    }

    public RdfTranslationResult setRdfStoreObservers(List<ElementsRdfStoreObserver> observers) {
        if (observers != null) {
            storeObservers.addAll(observers);
        }
        return this;
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

        if (keepEmpty || (arr != null && arr.length > 0)) {
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(output);
                outputStream.write(arr);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

            fileMemStore.put(output, arr);

            for (ElementsRdfStoreObserver observer : storeObservers) {
                if (objectInfo != null) {
                    observer.storedObjectRdf(objectInfo, output);
                } else if (relationshipInfo != null) {
                    observer.storedRelationshipRdf(relationshipInfo, output);
                }
            }
        }
    }
}
