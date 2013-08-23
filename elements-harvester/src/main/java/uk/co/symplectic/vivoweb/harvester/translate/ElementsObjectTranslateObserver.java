/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.translate.TranslationService;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectObserver;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;

import javax.xml.transform.Templates;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ElementsObjectTranslateObserver implements ElementsObjectObserver {
    private final List<ElementsObjectTranslateStagesObserver> objectObservers = new ArrayList<ElementsObjectTranslateStagesObserver>();
    private ElementsRdfStore rdfStore = null;

    private boolean currentStaffOnly = true;

    private final TranslationService translationService = new TranslationService();
    private Templates template = null;

    public ElementsObjectTranslateObserver(ElementsRdfStore rdfStore, String xslFilename) {
        this.rdfStore = rdfStore;
        if (!StringUtils.isEmpty(xslFilename)) {
            try {
                template = translationService.compileSource(new BufferedInputStream(new FileInputStream(xslFilename)));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("XSL Translation file not found", e);
            }

            translationService.setIgnoreFileNotFound(true);
        }
    }

    public void addObserver(ElementsObjectTranslateStagesObserver newObserver) {
        objectObservers.add(newObserver);
    }

    public void setCurrentStaffOnly(boolean currentStaffOnly) {
        this.currentStaffOnly = currentStaffOnly;
    }

    public void observe(ElementsStoredObject object) {
        boolean translateObject = true;

        if (object.getCategory() == ElementsObjectCategory.USER) {
            if (currentStaffOnly) {
                ElementsUserInfo userInfo = (ElementsUserInfo)object.getObjectInfo();
                translateObject = userInfo.getIsCurrentStaff();
            }
        }

        if (translateObject) {
            File outFile = rdfStore.getObjectFile(object.getObjectInfo());

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
            translationService.translate(object.getFile(), outFile, template, new ElementsDeleteEmptyTranslationCallback(outFile));

            for (ElementsObjectTranslateStagesObserver objectObserver : objectObservers) {
                objectObserver.beingTranslated(object.getObjectInfo());
            }
        }
    }
}
