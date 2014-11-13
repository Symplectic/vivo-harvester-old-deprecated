/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.model.ElementsExcludedUsers;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoredObject;

// TODO: This would preferably be an interceptor
public class ElementsObjectExcludeObserver implements ElementsObjectObserver {
    private ElementsExcludedUsers excludedUsers;

    public void setExcludedUsers(ElementsExcludedUsers excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    public void observe(ElementsStoredObject object) {
        if (object.getCategory() == ElementsObjectCategory.USER) {
            ElementsUserInfo userInfo = (ElementsUserInfo)object.getObjectInfo();
            excludedUsers.addUserId(userInfo.getId());
        }
    }
}
