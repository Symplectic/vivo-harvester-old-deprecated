/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.FileTranslationSource;
import uk.co.symplectic.translate.TranslationSource;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfoCache;
import uk.co.symplectic.xml.StAXUtils;
import uk.co.symplectic.xml.XMLStreamProcessor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class ElementsStoredObject {
    private final static FileTempMemStore fileMemStore = new FileTempMemStore();
    private File file;
    private ElementsObjectCategory category;
    private String id;

    private ElementsObjectInfo objectInfo = null;

    ElementsStoredObject(File file, ElementsObjectCategory category, String id) {
        this.file = file;
        this.category = category;
        this.id = id;
    }

    ElementsStoredObject(File file, ElementsObjectCategory category, String id, ElementsObjectInfo objectInfo) {
        this.file = file;
        this.category = category;
        this.id = id;
        this.objectInfo = objectInfo;
    }

    public ElementsObjectInfo getObjectInfo() {
        if (objectInfo == null) {
            parseObjectInfo();
        }

        return objectInfo;
    }

    public TranslationSource getTranslationSource() {
        if (fileMemStore != null) {
            return fileMemStore.translationSource(file);
        }

        return new FileTranslationSource(file);
    }

    public ElementsObjectCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    private synchronized void parseObjectInfo() {
        if (objectInfo == null) {
            objectInfo = ElementsObjectInfoCache.get(category, id);

            if (objectInfo == null && file != null && file.exists()) {
                InputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(file));

                    XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
                    XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);

                    ElementsObjectInfoObserver observer = new ElementsObjectInfoObserver();
                    XMLStreamProcessor processor = new XMLStreamProcessor();

                    processor.process(xmlReader, observer);

                    objectInfo = observer.getObjectInfo();
                    ElementsObjectInfoCache.put(objectInfo);
                } catch (FileNotFoundException fileNotFoundException) {
                } catch (XMLStreamException xmlStreamException) {
                    throw new IllegalStateException("Catastrophic failure reading files - abandoning", xmlStreamException);
                } finally {
                    if (inputStream != null) {
                        try { inputStream.close(); } catch (IOException e) {}
                    }
                }
            }
        }
    }
}
