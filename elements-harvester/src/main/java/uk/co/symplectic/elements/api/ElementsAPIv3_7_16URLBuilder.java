/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.utils.URLBuilder;

class ElementsAPIv3_7_16URLBuilder implements ElementsAPIURLBuilder {
    @Override
    public String buildObjectFeedQuery(String endpointUrl, ElementsAPIFeedObjectQuery feedQuery) {
        URLBuilder queryUrl = new URLBuilder(endpointUrl);

        if (feedQuery.getCategory() != null) {
            queryUrl.appendPath(feedQuery.getCategory().getPlural());
        } else {
            queryUrl.appendPath("objects");
        }

        if (!StringUtils.isEmpty(feedQuery.getGroups())) {
            queryUrl.addParam("groups", feedQuery.getGroups());
        }

        if (feedQuery.getFullDetails()) {
            queryUrl.addParam("detail", "full");
        }

        if (!StringUtils.isEmpty(feedQuery.getModifiedSince())) {
            queryUrl.addParam("modified-since", feedQuery.getModifiedSince());
        }

        return queryUrl.toString();
    }

    @Override
    public String buildRelationshipFeedQuery(String endpointUrl, ElementsAPIFeedRelationshipQuery feedQuery) {
        URLBuilder queryUrl = new URLBuilder(endpointUrl);

        queryUrl.appendPath("relationships");

        if (feedQuery.getFullDetails()) {
            queryUrl.addParam("detail", "full");
        }

        return queryUrl.toString();
    }
}
