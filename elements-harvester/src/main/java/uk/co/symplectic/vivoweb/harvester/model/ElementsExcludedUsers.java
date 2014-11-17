/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class ElementsExcludedUsers {
    private final Set<String> excludedUserIds = new LinkedHashSet<String>();

    public boolean contains(String userId) {
        return excludedUserIds.contains(userId);
    }

    public void addUserId(String userId) {
        excludedUserIds.add(userId);
    }
}