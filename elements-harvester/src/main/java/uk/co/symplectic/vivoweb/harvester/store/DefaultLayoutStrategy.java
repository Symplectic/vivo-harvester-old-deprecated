/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;

import java.io.File;

public class DefaultLayoutStrategy implements LayoutStrategy {
    @Override
    public File getObjectFile(File storeDir, ElementsObjectCategory category, String id) {
        return  getObjectExtraFile(storeDir, category, id, null);
    }

    @Override
    public File getObjectExtraFile(File storeDir, ElementsObjectCategory category, String id, String type) {
        File file = storeDir;
        if (storeDir == null || category == null) {
            throw new IllegalStateException();
        }

        file = new File(file, category.getSingular());
        if (!file.exists()) {
            file.mkdirs();
        }

        if (!StringUtils.isEmpty(type)) {
            return new File(file, id + "-" + type);
        } else {
            return new File(file, id);
        }
    }

    @Override
    public File getResourceFile(File storeDir, ElementsObjectCategory category, String resourceLabel, String id) {
        File file = storeDir;
        if (storeDir == null || category == null) {
            throw new IllegalStateException();
        }

        file = new File(file, category.getSingular() + "-" + resourceLabel);
        if (!file.exists()) {
            file.mkdirs();
        }

        return new File(file, id);
    }

    @Override
    public File getRelationshipFile(File storeDir, String id) {
        File file = storeDir;
        if (storeDir == null) {
            throw new IllegalStateException();
        }

        file = new File(file, "relationship");
        if (!file.exists()) {
            file.mkdirs();
        }

        return new File(file, id);
    }

    public String getRootNodeForType(String type) {
        return "entry";
    }
}
