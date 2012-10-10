/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.utils.StAXUtils;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

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

    public boolean hasObject(String category, String id) {
        File file = dir;
        if (dir == null) {
            throw new IllegalStateException();
        }

        file = new File(file, category);
        file = new File(file, id);

        return file.exists();
    }

    public boolean hasRelationship(String id) {
        File file = dir;
        if (dir == null) {
            throw new IllegalStateException();
        }

        file = new File(file, "relationship");
        file = new File(file, id);

        return file.exists();
    }

    public void prune(List<ElementsObjectCategory> keepAllInTheseCategories) {

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
        File file = getObjectFile(getObjectCategory(attributeList), getId(attributeList));
        store(file, reader, "object", docEncoding, docVersion);
        return new ElementsStoredObject(file, getObjectCategory(attributeList), getId(attributeList));
    }

    public ElementsStoredRelationship storeRelationship(List<XMLAttribute> attributeList, XMLStreamFragmentReader reader, String docEncoding, String docVersion) throws XMLStreamException {
        File file = getRelationshipFile(getId(attributeList));
        store(file, reader, "relationship", docEncoding, docVersion);
        return new ElementsStoredRelationship(file, getId(attributeList));
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

    private ElementsObjectCategory getObjectCategory(List<XMLAttribute> attributeList) {
        XMLAttribute catAttr = getAttribute(attributeList, null, "category");
        if (catAttr != null) {
            return ElementsObjectCategory.valueOf(catAttr.getValue());
        }

        return null;
    }

    private String getId(List<XMLAttribute> attributeList) {
        XMLAttribute idAttr = getAttribute(attributeList, null, "id");
        if (idAttr == null) {
            throw new IllegalStateException();
        }

        return idAttr.getValue();
    }

    private File getObjectFile(ElementsObjectCategory category, String id) {
        return layoutStrategy.getObjectFile(dir, category, id);
    }

    private File getRelationshipFile(String id) {
        return layoutStrategy.getRelationshipFile(dir, id);
    }

    private XMLAttribute getAttribute(List<XMLAttribute> attributeList, String attributePrefix, String attributeName) {
        for (XMLAttribute attribute : attributeList) {
            if (attributePrefix != null) {
                if (attributePrefix.equals(attribute.getPrefix()) && attributeName.equals(attribute.getName())) {
                    return attribute;
                }
            }

            if (attribute.getPrefix() == null && attributeName.equals(attribute.getName())) {
                return attribute;
            }
        }

        return null;
    }
}
