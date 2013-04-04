/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsObjectInfoCache;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLNamespace;
import uk.co.symplectic.xml.XMLStreamCopyToWriterObserver;
import uk.co.symplectic.xml.XMLStreamFragmentReader;
import uk.co.symplectic.xml.XMLStreamObserver;
import uk.co.symplectic.xml.XMLStreamProcessor;
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class ElementsObjectStore {
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    public ElementsObjectStore(String dir) {
        this.dir = new File(dir);
    }

    public void setUseLegacyLayout(boolean layout) {
        if (layout) {
            layoutStrategy = new LegacyLayoutStrategy();
        } else {
            layoutStrategy = new DefaultLayoutStrategy();
        }
    }

    public File generateResourceHandle(ElementsObjectInfo objectInfo, String resourceLabel) {
        return getResourceFile(objectInfo.getCategory(), resourceLabel, objectInfo.getId());
    }

    public ElementsStoredObject retrieveObject(ElementsObjectCategory category, String id) {
        File objectFile = getObjectFile(category, id);
        return objectFile == null ? null : new ElementsStoredObject(objectFile, category, id);
    }

    public ElementsStoredRelationship retrieveRelationship(String id) {
        File relationshipFile = getRelationshipFile(id);
        return relationshipFile == null ? null : new ElementsStoredRelationship(relationshipFile, id);
    }

    public ElementsStoredObject storeObject(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getObjectFile(XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList));
        ElementsObjectInfoObserver infoObserver = new ElementsObjectInfoObserver();
        store(file, reader, "object", docEncoding, docVersion, infoObserver);
        ElementsObjectInfoCache.put(infoObserver.getObjectInfo());
        return new ElementsStoredObject(file, XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList), infoObserver.getObjectInfo());
    }

    public ElementsStoredRelationship storeRelationship(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getRelationshipFile(XMLUtils.getId(attributeList));
        ElementsRelationshipInfoObserver infoObserver = new ElementsRelationshipInfoObserver();
        store(file, reader, "relationship", docEncoding, docVersion, infoObserver);
        return new ElementsStoredRelationship(file, XMLUtils.getId(attributeList), infoObserver.getRelationshipInfo());
    }

    private void store(File outputFile, XMLStreamFragmentReader reader, String type, String docEncoding, String docVersion, XMLStreamObserver observer) throws XMLStreamException {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            XMLStreamProcessor processor = new XMLStreamProcessor();
            processor.process(reader,
                    new XMLStreamCopyToWriterObserver(writer,
                            layoutStrategy.getRootNodeForType(type),
                            docEncoding,
                            docVersion,
                            Arrays.asList(new XMLNamespace("", "http://www.symplectic.co.uk/vivo/"), new XMLNamespace("api", "http://www.symplectic.co.uk/publications/api"))
                    ),
                    observer);

        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private File getObjectFile(ElementsObjectCategory category, String id) {
        return layoutStrategy.getObjectFile(dir, category, id);
    }

    private File getResourceFile(ElementsObjectCategory category, String resourceLabel, String id) {
        return layoutStrategy.getResourceFile(dir, category, resourceLabel, id);
    }

    private File getRelationshipFile(String id) {
        return layoutStrategy.getRelationshipFile(dir, id);
    }
}
