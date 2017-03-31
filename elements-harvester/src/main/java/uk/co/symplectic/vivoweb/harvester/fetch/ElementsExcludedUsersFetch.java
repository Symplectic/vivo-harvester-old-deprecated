/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIFeedObjectQuery;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.store.ElementsObjectStore;
import uk.co.symplectic.vivoweb.harvester.store.ElementsStoreFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ElementsExcludedUsersFetch {
    private ElementsAPI elementsAPI = null;
    private String groupsToExclude;
    private Set<String> excludedUserIds = new HashSet<String>();

    public ElementsExcludedUsersFetch(ElementsAPI api) {
        if (api == null) {
            throw new IllegalStateException();
        }

        this.elementsAPI = api;
    }

    public boolean isConfigured() {
        return !StringUtils.isEmpty(groupsToExclude);
    }

    public void setGroupsToExclude(String groupsToExclude) {
        this.groupsToExclude = groupsToExclude;
    }

    public Set<String> getExcludedUsers() {
        return this.excludedUserIds;
    }

    public String getGroupsToExclude() {
        return groupsToExclude;
    }

    public void execute() throws IOException {
        if (this.isConfigured()) {
            ElementsObjectStore objectStore = ElementsStoreFactory.getObjectStore();
            ElementsAPIFeedObjectQuery excludedUsersQuery = new ElementsAPIFeedObjectQuery();

            excludedUsersQuery.setFullDetails(false);
            excludedUsersQuery.setPerPage(100);
            excludedUsersQuery.setProcessAllPages(true);
            excludedUsersQuery.setGroups(this.getGroupsToExclude());
            excludedUsersQuery.setCategory(ElementsObjectCategory.USER);

            ElementsObjectExcludeObserver objectObserver = new ElementsObjectExcludeObserver();

            elementsAPI.execute(excludedUsersQuery, new ElementsObjectHandler(objectStore).addObserver(objectObserver));

            this.excludedUserIds = objectObserver.getExcludedUsers();
        }
    }
}
