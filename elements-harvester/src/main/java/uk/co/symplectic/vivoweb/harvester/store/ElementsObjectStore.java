/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.utils.StAXUtils;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsObjectInfo;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
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
        store(file, reader, "object", docEncoding, docVersion);
        return new ElementsStoredObject(file, XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList));
    }

    public ElementsStoredRelationship storeRelationship(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getRelationshipFile(XMLUtils.getId(attributeList));
        store(file, reader, "relationship", docEncoding, docVersion);
        return new ElementsStoredRelationship(file, XMLUtils.getId(attributeList));
    }

    private void store(File destFile, XMLStreamFragmentReader reader, String type, String docEncoding, String docVersion) throws XMLStreamException {
        Writer writer = null;
        try {
            writer = new FileWriter(destFile);
            StAXUtils.fragmentToWriter(reader, writer, layoutStrategy.getRootNodeForType(type), docEncoding, docVersion);
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
