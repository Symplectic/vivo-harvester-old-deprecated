/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfoCache;
import uk.co.symplectic.xml.*;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ElementsObjectStore {
    private final static List<XMLNamespace> namespaces = Arrays.asList(new XMLNamespace("", "http://www.symplectic.co.uk/vivo/"), new XMLNamespace("api", "http://www.symplectic.co.uk/publications/api"));
    private final static FileTempCache tempCache = new FileTempCache();
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    public ElementsObjectStore(String dir) {
        this.dir = new File(dir);
    }

    public File generateResourceHandle(ElementsObjectInfo objectInfo, String resourceLabel) {
        return getResourceFile(objectInfo.getCategory(), resourceLabel, objectInfo.getId());
    }

    public ElementsStoredObject retrieveObject(ElementsObjectCategory category, String id) {
        File objectFile = getObjectFile(category, id);
        return objectFile == null ? null : new ElementsStoredObject(tempCache, objectFile, category, id);
    }

    public ElementsStoredRelationship retrieveRelationship(String id) {
        File relationshipFile = getRelationshipFile(id);
        return relationshipFile == null ? null : new ElementsStoredRelationship(tempCache, relationshipFile, id);
    }

    public ElementsStoredObject storeObject(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getObjectFile(XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList));
        ElementsObjectInfoObserver infoObserver = new ElementsObjectInfoObserver();
        store(file, reader, "object", docEncoding, docVersion, infoObserver);
        ElementsObjectInfoCache.put(infoObserver.getObjectInfo());
        return new ElementsStoredObject(tempCache, file, XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList), infoObserver.getObjectInfo());
    }

    public ElementsStoredRelationship storeRelationship(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getRelationshipFile(XMLUtils.getId(attributeList));
        ElementsRelationshipInfoObserver infoObserver = new ElementsRelationshipInfoObserver();
        store(file, reader, "relationship", docEncoding, docVersion, infoObserver);
        return new ElementsStoredRelationship(tempCache, file, XMLUtils.getId(attributeList), infoObserver.getRelationshipInfo());
    }

    private void store(File outputFile, XMLStreamFragmentReader reader, String type, String docEncoding, String docVersion, XMLStreamObserver observer) throws XMLStreamException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer memoryWriter = new OutputStreamWriter(baos, "utf-8");

            XMLStreamProcessor processor = new XMLStreamProcessor();
            processor.process(reader,
                    new XMLStreamCopyToWriterObserver(memoryWriter,
                            layoutStrategy.getRootNodeForType(type),
                            docEncoding,
                            docVersion,
                            namespaces
                    ),
                    observer);

            memoryWriter.flush();

            byte[] xml = baos.toByteArray();
            OutputStream os = new FileOutputStream(outputFile);
            try {
                os.write(xml);
            } finally {
                os.close();
            }

            if (tempCache != null) {
                tempCache.put(outputFile, xml);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private File getObjectFile(ElementsObjectCategory category, String id) {
        return layoutStrategy.getObjectFile(dir, category, id, FileFormat.XML);
    }

    private File getResourceFile(ElementsObjectCategory category, String resourceLabel, String id) {
        return layoutStrategy.getResourceFile(dir, category, resourceLabel, id);
    }

    private File getRelationshipFile(String id) {
        return layoutStrategy.getRelationshipFile(dir, id, FileFormat.XML);
    }
}
