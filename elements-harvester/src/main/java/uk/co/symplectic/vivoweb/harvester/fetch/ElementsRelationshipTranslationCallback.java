/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.PostTranslateCallback;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;

import java.io.*;

public class ElementsRelationshipTranslationCallback implements PostTranslateCallback {
    private File inputFile;
    private File outputFile;

    private ElementsObjectStore objectStore;

    private boolean currentStaffOnly = true;
    private boolean visibleLinksOnly = true;

    private ElementsObjectsInRelationships objectsInRelationships = null;

    ElementsRelationshipTranslationCallback(File inputFile, File outputFile, ElementsObjectsInRelationships objectsInRelationships, ElementsObjectStore objectStore) {
        this.inputFile  = inputFile;
        this.outputFile = outputFile;
        this.objectsInRelationships = objectsInRelationships;
        this.objectStore = objectStore;
    }

    void setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
    }

    void setVisibleLinksOnly(boolean visibleLinksOnly) {
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

        ElementsRelationshipInfo relationshipInfo = ElementsXMLParsers.parseRelatonshipInfo(inputFile);

        if (visibleLinksOnly && includeRelationship) {
            includeRelationship = relationshipInfo.getIsVisisble();
        }

        if (currentStaffOnly && includeRelationship) {
            String userId = relationshipInfo.getUserId();
            if (!StringUtils.isEmpty(userId)) {
                ElementsStoredObject user = objectStore.retrieveObject(ElementsObjectCategory.USER, userId);
                if (user != null && user.getFile().exists()) {
                    ElementsUserInfo userInfo = ElementsXMLParsers.parseUserInfo(user.getFile());
                    if (userInfo != null) {
                        includeRelationship = userInfo.getIsCurrentStaff();
                    } else {
                        includeRelationship = false;
                    }
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
