/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// TODO: This would preferably be an interceptor
public class ElementsObjectExcludeObserver implements ElementsObjectObserver {
    private final Set<String> excludedUserIds = new HashSet<String>();

    public Set<String> getExcludedUsers() {
        return Collections.unmodifiableSet(this.excludedUserIds);
    }

    public void observe(ElementsStoredObject object) {
        if (object.getCategory() == ElementsObjectCategory.USER) {
            ElementsUserInfo userInfo = (ElementsUserInfo)object.getObjectInfo();
            excludedUserIds.add(userInfo.getId());
        }
    }
}
