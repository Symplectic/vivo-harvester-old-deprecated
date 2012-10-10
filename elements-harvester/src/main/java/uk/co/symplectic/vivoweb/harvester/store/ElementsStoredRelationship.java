/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import java.io.File;

public class ElementsStoredRelationship {
    private File file;
    private String id;

    ElementsStoredRelationship(File file, String id) {
        this.file = file;
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public String getId() {
        return id;
    }
}
