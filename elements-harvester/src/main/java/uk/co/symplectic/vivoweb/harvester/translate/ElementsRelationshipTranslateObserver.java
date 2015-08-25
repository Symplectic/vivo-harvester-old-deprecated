/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.*;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectId;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectsInRelationships;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsRelationshipObserver;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredRelationship;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElementsRelationshipTranslateObserver implements ElementsRelationshipObserver {
    private final List<ElementsRelationshipTranslateStagesObserver> relationshipObservers = new ArrayList<ElementsRelationshipTranslateStagesObserver>();
    private final TranslationService translationService = new TranslationService();
    private TemplatesHolder templatesHolder = null;

    private ElementsObjectStore objectStore = null;
    private ElementsRdfStore rdfStore = null;

    private boolean currentStaffOnly = true;
    private boolean visibleLinksOnly = true;

    private boolean keepEmpty = false;

    private Map<String, String> xslParameters = null;

    private ElementsRelationshipTranslateObserver() {
    }

    public ElementsRelationshipTranslateObserver setObjectStore(ElementsObjectStore objectStore) {
        this.objectStore = objectStore;
        return this;
    }

    public ElementsRelationshipTranslateObserver setRdfStore(ElementsRdfStore rdfStore) {
        this.rdfStore = rdfStore;
        return this;
    }

    public ElementsRelationshipTranslateObserver setXslTemplate(String xslFilename) {
        if (!StringUtils.isEmpty(xslFilename)) {
            this.templatesHolder = new TemplatesHolder(xslFilename);
            this.translationService.setIgnoreFileNotFound(true);
        }

        return this;
    }

    public ElementsRelationshipTranslateObserver setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
        return this;
    }

    public ElementsRelationshipTranslateObserver setVisibleLinksOnly(boolean visibleLinksOnly) {
        this.visibleLinksOnly = visibleLinksOnly;
        return this;
    }

    public static ElementsRelationshipTranslateObserver create() {
        return new ElementsRelationshipTranslateObserver();
    }

    public ElementsRelationshipTranslateObserver setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
        return this;
    }

    public ElementsRelationshipTranslateObserver setXslParameters(Map<String, String> xslParameters) {
        this.xslParameters = xslParameters;
        return this;
    }

    public ElementsRelationshipTranslateObserver addObserver(ElementsRelationshipTranslateStagesObserver newObserver) {
        if (newObserver != null) {
            relationshipObservers.add(newObserver);
        }

        return this;
    }

    public ElementsRelationshipTranslateObserver addObservers(ElementsRelationshipTranslateStagesObserver... newObservers) {
        if (newObservers != null) {
            for (ElementsRelationshipTranslateStagesObserver newObserver : newObservers) {
                if (newObserver != null) {
                    relationshipObservers.add(newObserver);
                }
            }
        }

        return this;
    }

    public void observe(ElementsStoredRelationship relationship, ElementsObjectsInRelationships objectsInRelationships) {
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
            /**
             * Note that the translation service is designed to be asynchronous. Which means, when the translate() method
             * call returns, we are not guaranteed that the translation will have completed (in fact, we can be almost certain
             * that is WON'T have finished translating by the time that the method call returns.
             *
             * As a result, we can't do anything that relies on the translation having been completed by coding it after the
             * method call. For example, cleaning up empty files output from the translation.
             *
             * In order to get round this, a callback object can be supplied, which will execute after the translation code
             * has completed.
             *
             * In this case, we supply an object that will clean up any empty translation output.
             */
            TranslationTask task = translationService.translate(
                    relationship.getTranslationSource(),
                    rdfStore.getRelationshipTranslationResult(relationship.getRelationshipInfo()).setKeepEmpty(keepEmpty),
                    templatesHolder,
                    xslParameters
            );

            for (ElementsRelationshipTranslateStagesObserver relationshipObserver : relationshipObservers) {
                relationshipObserver.beingTranslated(task, relationship.getRelationshipInfo());
            }

            if (objectsInRelationships != null) {
                for (ElementsObjectId id : relationshipInfo.getObjectIds()) {
                    objectsInRelationships.add(id.getCategory(), id.getId());
                }
            }
        }
    }
}
