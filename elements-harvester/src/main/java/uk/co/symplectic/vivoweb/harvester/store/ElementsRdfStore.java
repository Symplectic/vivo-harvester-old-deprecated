/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.utils.DeletionService;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

public class ElementsRdfStore {
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();
    private DeletionService deletionService = new DeletionService();

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
                    if (idsToKeep.contains(file.getName())) {
                        deletionService.keep(file);
                    } else {
                        deletionService.deleteOnExit(file);
                    }
                } else if (file.getName().startsWith(prefix)) {
                    boolean keepFile = false;
                    for (String id : idsToKeep) {
                        if (file.getName().equals(prefix + id)) {
                            keepFile = true;
                        }
                    }

                    if (keepFile) {
                        deletionService.keep(file);
                    } else {
                        deletionService.deleteOnExit(file);
                    }
                }
            }
        }
    }

    public File getObjectFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getObjectFile(dir, XMLUtils.getObjectCategory(attributeList), XMLUtils.getId(attributeList));
    }

    public File getObjectFile(ElementsObjectCategory category, String id) {
        return layoutStrategy.getObjectFile(dir, category, id);
    }

    public File getObjectFile(ElementsObjectInfo objectInfo) {
        return layoutStrategy.getObjectFile(dir, objectInfo.getCategory(), objectInfo.getId());
    }

    public File getRelationshipFile(List<XMLAttribute> attributeList) {
        return layoutStrategy.getRelationshipFile(dir, XMLUtils.getId(attributeList));

    }

    public File getRelationshipFile(String id) {
        return layoutStrategy.getRelationshipFile(dir, id);
    }

    public File getRelationshipFile(ElementsRelationshipInfo relationshipInfo) {
        return layoutStrategy.getRelationshipFile(dir, relationshipInfo.getId());
    }

    public boolean writeObjectExtra(ElementsObjectInfo objectInfo, String type, String rdf) {
        File file = layoutStrategy.getObjectExtraFile(dir, objectInfo.getCategory(), objectInfo.getId(), type);

        if (file != null) {
            try {
                Writer writer = new BufferedWriter(new FileWriter(file));
                try {
                    writer.write(rdf);
                } finally {
                    writer.close();
                }
            } catch (IOException ioe) {
                // Log error
                return false;
            }
        }

        return true;
    }
}
