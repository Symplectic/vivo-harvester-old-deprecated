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
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
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
        if (dir != null) {
            File objectDir = new File(dir, category.getSingular());
            if (objectDir.exists()) {
                pruneIn(objectDir, idsToKeep, null);
            } else {
                pruneIn(dir, idsToKeep, category.getSingular());
            }
        }
    }

    private void pruneIn(File dir, Set<String> idsToKeep, String prefix) {
        File[] files = dir.listFiles();
        if (files != null) {
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
    }

    public File getObjectFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getObjectFile(dir, XMLUtils.getObjectCategory(attributeList), getId(attributeList));
    }

    public File getRelationshipFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getRelationshipFile(dir, getId(attributeList));

    }

    public boolean writeObjectExtra(List<XMLAttribute> attributeList, String type, String rdf) {
        File file = layoutStrategy.getObjectExtraFile(dir, XMLUtils.getObjectCategory(attributeList), getId(attributeList), type);

        if (file != null) {
            try {
                Writer writer = new BufferedWriter(new FileWriter(file));
                try {
                    writer.write(rdf);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (IOException ioe) {
                // Log error
                return false;
            }
        }

        return true;
    }

    private String getId(List<XMLAttribute> attributeList) {
        XMLAttribute idAttr = XMLUtils.getAttribute(attributeList, null, "id");
        if (idAttr == null) {
            throw new IllegalStateException();
        }

        return idAttr.getValue();
    }
}