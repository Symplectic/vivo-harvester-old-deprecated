/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.model;

import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElementsRelationshipInfo {
    private String id = null;
    private boolean isVisible = true;
    private String userId = null;
    private final List<ElementsObjectId> objectIds = new ArrayList<ElementsObjectId>();

    public ElementsRelationshipInfo() {}

    private ElementsRelationshipInfo(String id) {
        this.id = id;
    }

    public static ElementsRelationshipInfo create(String id) {
        return new ElementsRelationshipInfo(id);
    }

    public String getId() {
        return id;
    }

    public boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void addObjectId(ElementsObjectId id) {
        objectIds.add(id);
    }

    public List<ElementsObjectId> getObjectIds() {
        return Collections.unmodifiableList(objectIds);
    }
}
