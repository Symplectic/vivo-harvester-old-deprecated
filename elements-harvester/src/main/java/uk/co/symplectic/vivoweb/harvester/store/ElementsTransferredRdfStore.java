/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElementsTransferredRdfStore {
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    public ElementsTransferredRdfStore(String dir) {
        this.dir = new File(dir);
    }

    public File getObjectFile(ElementsObjectInfo objectInfo) {
        return layoutStrategy.getObjectFile(dir, objectInfo.getCategory(), objectInfo.getId());
    }

    public File getObjectExtraFile(ElementsObjectInfo objectInfo, String type) {
        return layoutStrategy.getObjectExtraFile(dir, objectInfo.getCategory(), objectInfo.getId(), type);
    }

    public File getRelationshipFile(ElementsRelationshipInfo relationshipInfo) {
        return layoutStrategy.getRelationshipFile(dir, relationshipInfo.getId());
    }
}
