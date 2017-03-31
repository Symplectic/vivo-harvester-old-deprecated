/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import uk.co.symplectic.translate.PostTranslateCallback;
import uk.co.symplectic.utils.DeletionService;

import java.io.File;

public class ElementsDeleteEmptyTranslationCallback implements PostTranslateCallback {
    private DeletionService deletionService = new DeletionService();
    private File outputFile;

    public ElementsDeleteEmptyTranslationCallback(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void translationSuccess() {
        if (outputFile.length() < 3) {
            deletionService.delete(outputFile);
        }
    }

    @Override
    public void translationFailure(Exception caughtException) {
        if (outputFile.length() < 3) {
            deletionService.delete(outputFile);
        }
    }
}
