/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

public class ElementsFeedInfo {
    private String feedId;
    private String lastUpdated;

    public String getFeedId() {
        return feedId;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
