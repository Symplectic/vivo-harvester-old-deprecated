/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.cache;

import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import java.io.File;

public class CachingService {
    public void put(File file, String xml) {
        CachingServiceImpl.put(file.getAbsolutePath(), xml);
    }

    public String get(File file) {
        return CachingServiceImpl.get(file.getAbsolutePath());
    }

    public void remove(File file) {
        CachingServiceImpl.remove(file.getAbsolutePath());
    }
}
