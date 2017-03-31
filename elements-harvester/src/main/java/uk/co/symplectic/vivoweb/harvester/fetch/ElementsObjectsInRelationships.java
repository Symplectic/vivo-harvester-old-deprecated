/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import uk.co.symplectic.elements.api.ElementsObjectCategory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ElementsObjectsInRelationships {
    private final Map<ElementsObjectCategory, Set<String>> objectCategoryIdMap = new ConcurrentHashMap<ElementsObjectCategory, Set<String>>();

    public synchronized void add(ElementsObjectCategory category, String id) {
        Set<String> idSet = getIdSet(category);
        if (idSet != null) {
            idSet.add(id);
        }
    }

    public Set<String> get(ElementsObjectCategory category) {
        return Collections.unmodifiableSet(getIdSet(category));
    }

    private Set<String> getIdSet(ElementsObjectCategory category) {
        Set<String> idSet = objectCategoryIdMap.get(category);
        if (idSet == null) {
            idSet = new HashSet<String>();
            objectCategoryIdMap.put(category, idSet);
        }

        return idSet;
    }
}
