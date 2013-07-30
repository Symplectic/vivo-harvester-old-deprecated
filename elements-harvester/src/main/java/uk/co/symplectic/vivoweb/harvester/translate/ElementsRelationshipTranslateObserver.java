/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectsInRelationships;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsRelationshipObserver;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsRelationshipTranslationCallback;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredRelationship;

import javax.xml.transform.Templates;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ElementsRelationshipTranslateObserver implements ElementsRelationshipObserver {
    private TranslationService translationService = new TranslationService();
    private Templates template = null;

    private ElementsObjectStore objectStore = null;
    private ElementsRdfStore rdfStore = null;

    private boolean currentStaffOnly = true;
    private boolean visibleLinksOnly = true;

    public ElementsRelationshipTranslateObserver(ElementsObjectStore objectStore, ElementsRdfStore rdfStore, String xslFilename) {
        this.objectStore = objectStore;
        this.rdfStore = rdfStore;
        if (!StringUtils.isEmpty(xslFilename)) {
            try {
                template = translationService.compileSource(new BufferedInputStream(new FileInputStream(xslFilename)));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("XSL Translation file not found", e);
            }
        }
    }

    public void setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
    }

    public void setVisibleLinksOnly(boolean visibleLinksOnly) {
        this.visibleLinksOnly = visibleLinksOnly;
    }

    public void observe(ElementsStoredRelationship relationship, ElementsObjectsInRelationships objectsInRelationships) {
        File outFile = rdfStore.getRelationshipFile(relationship.getRelationshipInfo());

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
        ElementsRelationshipTranslationCallback callback = new ElementsRelationshipTranslationCallback(relationship, outFile, objectsInRelationships, objectStore);
        callback.setCurrentStaffOnly(currentStaffOnly);
        callback.setVisibleLinksOnly(visibleLinksOnly);
        translationService.translate(relationship.getFile(), outFile, template, callback);
    }
}
