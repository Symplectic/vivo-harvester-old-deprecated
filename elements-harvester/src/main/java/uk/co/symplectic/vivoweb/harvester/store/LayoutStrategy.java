/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;

import java.io.File;

public interface LayoutStrategy {
    public File getObjectFile(File storeDir, ElementsObjectCategory category, String id, FileFormat format);

    public File getObjectExtraFile(File storeDir, ElementsObjectCategory category, String id, String type, FileFormat format);

    public File getResourceFile(File storeDir, ElementsObjectCategory category, String resourceLabel, String id);

    public File getRelationshipFile(File storeDir, String id, FileFormat format);

    public String getRootNodeForType(String type);
}
