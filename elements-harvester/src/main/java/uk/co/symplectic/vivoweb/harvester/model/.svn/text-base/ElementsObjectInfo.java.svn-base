/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.model;

import uk.co.symplectic.elements.api.ElementsObjectCategory;

public class ElementsObjectInfo {
    private ElementsObjectCategory category = null;
    private String id = null;

    protected ElementsObjectInfo(ElementsObjectCategory category, String id) {
        this.category = category;
        this.id = id;
    }

    public static ElementsObjectInfo create(ElementsObjectCategory category, String id) {
        if (ElementsObjectCategory.ACTIVITY == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.CONCEPT == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.EQUIPMENT == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.GRANT == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.ORG_STRUCTURE == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.TEACHING_ACTIVITY == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.PROJECT == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.PUBLICATION == category) {
            return new ElementsUnknownObjectInfo(category, id);
        } else if (ElementsObjectCategory.USER == category) {
            return new ElementsUserInfo(id);
        }

        return new ElementsUnknownObjectInfo(category, id);
    }

    public ElementsObjectCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }
}
