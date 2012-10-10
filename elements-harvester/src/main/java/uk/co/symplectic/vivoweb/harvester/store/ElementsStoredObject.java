/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;

import java.io.File;

public class ElementsStoredObject {
    private File file;
    private ElementsObjectCategory category;
    private String id;

    ElementsStoredObject(File file, ElementsObjectCategory category, String id) {
        this.file = file;
        this.category = category;
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public ElementsObjectCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }
}
