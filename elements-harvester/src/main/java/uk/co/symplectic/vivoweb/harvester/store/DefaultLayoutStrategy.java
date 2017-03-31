/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.util.ThreadSafe;

import java.io.File;

public class DefaultLayoutStrategy implements LayoutStrategy {
    @Override
    public File getObjectFile(File storeDir, ElementsObjectCategory category, String id, FileFormat format) {
        return  getObjectExtraFile(storeDir, category, id, null, format);
    }

    @Override
    public File getObjectExtraFile(File storeDir, ElementsObjectCategory category, String id, String type, FileFormat format) {
        if (storeDir == null || category == null) {
            throw new IllegalStateException();
        }

        // Get the directory we will be writing the file to
        File file = new File(storeDir, category.getSingular());

        // If the directory doesn't exist
        if (!ThreadSafe.mkdirs(file)) {
            throw new IllegalStateException("Missing directory " + file.getAbsolutePath());
        }

        String extension = StringUtils.isEmpty(format.getExtension()) ? "" : "." + format.getExtension();
        if (!StringUtils.isEmpty(type)) {
            return new File(file, id + "-" + type + extension);
        } else {
            return new File(file, id + extension);
        }
    }

    @Override
    public File getResourceFile(File storeDir, ElementsObjectCategory category, String resourceLabel, String id) {
        File file = storeDir;
        if (storeDir == null || category == null) {
            throw new IllegalStateException();
        }

        file = new File(file, category.getSingular() + "-" + resourceLabel);
        if (!ThreadSafe.mkdirs(file)) {
            throw new IllegalStateException("Missing directory " + file.getAbsolutePath());
        }

        return new File(file, id);
    }

    @Override
    public File getRelationshipFile(File storeDir, String id, FileFormat format) {
        File file = storeDir;
        if (storeDir == null) {
            throw new IllegalStateException();
        }

        file = new File(file, "relationship");
        if (!ThreadSafe.mkdirs(file)) {
            throw new IllegalStateException("Missing directory " + file.getAbsolutePath());
        }

        String extension = StringUtils.isEmpty(format.getExtension()) ? "" : "." + format.getExtension();
        return new File(file, id + extension);
    }

    public String getRootNodeForType(String type) {
        return "entry";
    }
}
