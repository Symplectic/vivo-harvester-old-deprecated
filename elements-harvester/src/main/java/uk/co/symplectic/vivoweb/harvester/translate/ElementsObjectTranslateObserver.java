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
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectObserver;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ElementsObjectTranslateObserver implements ElementsObjectObserver {
    private final List<ElementsObjectTranslateStagesObserver> objectObservers = new ArrayList<ElementsObjectTranslateStagesObserver>();
    private ElementsRdfStore rdfStore = null;

    private boolean currentStaffOnly = true;

    private Set<String> excludedUsers;

    private final TranslationService translationService = new TranslationService();
    private TemplatesHolder templatesHolder = null;

    private boolean keepEmpty = false;

    private Map<String, String> xslParameters = null;

    private ElementsObjectTranslateObserver() { }

    public static ElementsObjectTranslateObserver create() {
        return new ElementsObjectTranslateObserver();
    }

    public ElementsObjectTranslateObserver setRdfStore(ElementsRdfStore rdfStore) {
        this.rdfStore = rdfStore;
        return this;
    }

    public ElementsObjectTranslateObserver setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
        return this;
    }

    public ElementsObjectTranslateObserver setXslTemplate(String xslFilename) {
        if (!StringUtils.isEmpty(xslFilename)) {
            templatesHolder = new TemplatesHolder(xslFilename);
            translationService.setIgnoreFileNotFound(true);
        }
        return this;
    }

    public ElementsObjectTranslateObserver setXslParameters(Map<String, String> xslParameters) {
        this.xslParameters = xslParameters;
        return this;
    }

    public ElementsObjectTranslateObserver addObserver(ElementsObjectTranslateStagesObserver newObserver) {
        if (newObserver != null) {
            objectObservers.add(newObserver);
        }
        return this;
    }

    public ElementsObjectTranslateObserver addObservers(ElementsObjectTranslateStagesObserver... newObservers) {
        if (newObservers != null) {
            for (ElementsObjectTranslateStagesObserver newObserver : newObservers) {
                if (newObserver != null) {
                    objectObservers.add(newObserver);
                }
            }
        }

        return this;
    }

    public ElementsObjectTranslateObserver setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
        return this;
    }

    public ElementsObjectTranslateObserver setExcludedUsers(Set<String> excludedUsers) {
        this.excludedUsers = excludedUsers;
        return this;
    }

    public void observe(ElementsStoredObject object) {
        boolean translateObject = true;

        // TODO: This block with two conditions should be replaced with two interceptor objects...
        if (object.getCategory() == ElementsObjectCategory.USER) {
            ElementsUserInfo userInfo = (ElementsUserInfo)object.getObjectInfo();
            if (currentStaffOnly) {
                translateObject = userInfo.getIsCurrentStaff();
            }
            if (excludedUsers != null && excludedUsers.contains(userInfo.getId())) {
                translateObject = false;  //override if user is in an excluded group
            }
        }

        if (translateObject) {
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
                    object.getTranslationSource(),
                    rdfStore.getObjectTranslationResult(object.getObjectInfo()).setKeepEmpty(keepEmpty),
                    templatesHolder,
                    xslParameters
            );

            for (ElementsObjectTranslateStagesObserver objectObserver : objectObservers) {
                objectObserver.beingTranslated(task, object.getObjectInfo());
            }
        }
    }
}
