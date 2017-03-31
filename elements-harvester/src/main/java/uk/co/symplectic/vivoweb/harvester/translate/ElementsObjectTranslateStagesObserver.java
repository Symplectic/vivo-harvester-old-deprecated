/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.translate;

import uk.co.symplectic.translate.TranslationTask;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;

public interface ElementsObjectTranslateStagesObserver {
    void beingTranslated(final TranslationTask task, final ElementsObjectInfo info);
}
