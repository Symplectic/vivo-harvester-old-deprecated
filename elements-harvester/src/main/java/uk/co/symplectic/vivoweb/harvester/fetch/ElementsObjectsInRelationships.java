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

class ElementsObjectsInRelationships {
    private Map<ElementsObjectCategory, Set<String>> objectCategoryIdMap = new HashMap<ElementsObjectCategory, Set<String>>();

    void add(ElementsObjectCategory category, String id) {
        Set<String> idSet = getIdSet(category);
        if (idSet != null) {
            idSet.add(id);
        }
    }

    Set<String> get(ElementsObjectCategory category) {
        return Collections.unmodifiableSet(getIdSet(category));
    }

    private synchronized Set<String> getIdSet(ElementsObjectCategory category) {
        Set<String> idSet = objectCategoryIdMap.get(category);
        if (idSet == null) {
            idSet = new HashSet<String>();
            objectCategoryIdMap.put(category, idSet);
        }

        return idSet;
    }
}
