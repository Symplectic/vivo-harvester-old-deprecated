/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.translate.FileTranslationSource;
import uk.co.symplectic.translate.TranslationSource;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.xml.StAXUtils;
import uk.co.symplectic.xml.XMLStreamProcessor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class ElementsStoredRelationship {
    private FileTempCache tempCache;
    private File file;
    private String id;

    private ElementsRelationshipInfo relationshipInfo = null;

    ElementsStoredRelationship(FileTempCache tempCache, File file, String id) {
        this.tempCache = tempCache;
        this.file = file;
        this.id = id;
    }

    ElementsStoredRelationship(FileTempCache tempCache, File file, String id, ElementsRelationshipInfo relationshipInfo) {
        this.tempCache = tempCache;
        this.file = file;
        this.id = id;
        this.relationshipInfo = relationshipInfo;
    }

    public TranslationSource getTranslationSource() {
        if (tempCache != null) {
            return tempCache.translationSource(file);
        }

        return new FileTranslationSource(file);
    }

    public String getId() {
        return id;
    }

    public ElementsRelationshipInfo getRelationshipInfo() {
        if (relationshipInfo == null) {
            parseRelationshipInfo();
        }

        return relationshipInfo;
    }

    private synchronized void parseRelationshipInfo() {
        if (relationshipInfo == null) {
            if (file != null && file.exists()) {
                InputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(file));

                    XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
                    XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);

                    ElementsRelationshipInfoObserver observer = new ElementsRelationshipInfoObserver();
                    XMLStreamProcessor processor = new XMLStreamProcessor();

                    processor.process(xmlReader, observer);

                    relationshipInfo = observer.getRelationshipInfo();
                } catch (FileNotFoundException fileNotFoundException) {
                } catch (XMLStreamException e) {
                } finally {
                    if (inputStream != null) {
                        try { inputStream.close(); } catch (IOException e) {}
                    }
                }
            }
        }
    }
}
