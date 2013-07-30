/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import uk.co.symplectic.translate.PostTranslateCallback;

import java.io.File;

public class ElementsDeleteEmptyTranslationCallback implements PostTranslateCallback {
    private File outputFile;

    ElementsDeleteEmptyTranslationCallback(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void translationSuccess() {
        if (outputFile.length() < 3) {
            outputFile.delete();
        }
    }

    @Override
    public void translationFailure(Exception caughtException) {
        if (outputFile.length() < 3) {
            outputFile.delete();
        }
    }
}
