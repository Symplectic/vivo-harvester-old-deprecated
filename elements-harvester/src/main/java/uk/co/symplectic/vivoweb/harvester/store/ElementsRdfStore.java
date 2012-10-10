/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.utils.StAXUtils;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

public class ElementsRdfStore {
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    public ElementsRdfStore(String dir) {
        this.dir = new File(dir);
    }

    public void setUseLegacyLayout(boolean layout) {
        if (layout) {
            layoutStrategy = new LegacyLayoutStrategy();
        } else {
            layoutStrategy = new DefaultLayoutStrategy();
        }
    }

    public void pruneExcept(ElementsObjectCategory category, Set<String> idsToKeep) {
        File objectDir = new File(dir, category.getSingular());
        if (objectDir.exists()) {
            pruneIn(objectDir, idsToKeep, null);
        } else {
            pruneIn(dir, idsToKeep, category.getSingular());
        }
    }

    private void pruneIn(File dir, Set<String> idsToKeep, String prefix) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                pruneIn(file, idsToKeep, prefix);
            } else if (StringUtils.isEmpty(prefix)) {
                if (!idsToKeep.contains(file.getName())) {
                    file.delete();
                }
            } else if (file.getName().startsWith(prefix)) {
                boolean keepFile = false;
                for (String id : idsToKeep) {
                    if (file.getName().equals(prefix + id)) {
                        keepFile = true;
                    }
                }

                if (!keepFile) {
                    file.delete();
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

    public File getObjectFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getObjectFile(dir, getObjectCategory(attributeList), getId(attributeList));
    }

    public File getRelationshipFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getRelationshipFile(dir, getId(attributeList));

    }

    private String getId(List<XMLAttribute> attributeList) {
        XMLAttribute idAttr = getAttribute(attributeList, null, "id");
        if (idAttr == null) {
            throw new IllegalStateException();
        }

        return idAttr.getValue();
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
