/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.PostTranslateCallback;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectId;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectsInRelationships;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredRelationship;

import java.io.File;

public class ElementsRelationshipTranslationCallback implements PostTranslateCallback {
    private ElementsStoredRelationship relationship;
    private File outputFile;

    private ElementsObjectStore objectStore;

    private boolean currentStaffOnly = true;
    private boolean visibleLinksOnly = true;

    private ElementsObjectsInRelationships objectsInRelationships = null;

    public ElementsRelationshipTranslationCallback(ElementsStoredRelationship relationship, File outputFile, ElementsObjectsInRelationships objectsInRelationships, ElementsObjectStore objectStore) {
        this.relationship = relationship;
        this.outputFile = outputFile;
        this.objectsInRelationships = objectsInRelationships;
        this.objectStore = objectStore;
    }

    public void setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
    }

    public void setVisibleLinksOnly(boolean visibleLinksOnly) {
        this.visibleLinksOnly = visibleLinksOnly;
    }

    @Override
    public void translationSuccess() {
        postTranslationHandling();
    }

    @Override
    public void translationFailure(Exception caughtException) {
        postTranslationHandling();
    }

    private void postTranslationHandling() {
        boolean deleteOutputFile = false;
        boolean includeRelationship = true;

        ElementsRelationshipInfo relationshipInfo = relationship.getRelationshipInfo();

        if (visibleLinksOnly && includeRelationship) {
            includeRelationship = relationshipInfo.getIsVisible();
        }

        if (currentStaffOnly && includeRelationship) {
            String userId = relationshipInfo.getUserId();
            if (!StringUtils.isEmpty(userId)) {
                ElementsStoredObject user = objectStore.retrieveObject(ElementsObjectCategory.USER, userId);
                if (user != null && user.getObjectInfo() != null) {
                    includeRelationship = ((ElementsUserInfo)user.getObjectInfo()).getIsCurrentStaff();
                } else {
                    includeRelationship = false;
                }
            }
        }

        if (includeRelationship) {
            for (ElementsObjectId id : relationshipInfo.getObjectIds()) {
                objectsInRelationships.add(id.getCategory(), id.getId());
            }

            if (outputFile.length() < 3) {
                deleteOutputFile = true;
            }
        } else {
            deleteOutputFile = true;
        }

        if (deleteOutputFile) {
            outputFile.delete();
        }
    }
}
