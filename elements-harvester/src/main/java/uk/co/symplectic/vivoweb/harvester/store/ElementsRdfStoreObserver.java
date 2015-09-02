/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import java.io.File;

public interface ElementsRdfStoreObserver {
    public void storedObjectRdf(ElementsObjectInfo objectInfo, File storedRdf, FileFormat storedFormat);

    public void storedObjectExtraRdf(ElementsObjectInfo objectInfo, String type, File storedRdf, FileFormat storedFormat);

    public void storedRelationshipRdf(ElementsRelationshipInfo relationshipInfoInfo, File storedRdf, FileFormat storedFormat);
}
