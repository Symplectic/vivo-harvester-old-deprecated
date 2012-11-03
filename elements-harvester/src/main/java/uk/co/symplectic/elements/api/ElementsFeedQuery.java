/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

abstract class ElementsFeedQuery {
    private boolean fullDetails = false;
    private boolean processAllPages = false;

    ElementsFeedQuery() {
        super();
    }

    public boolean getFullDetails() {
        return fullDetails;
    }

    public boolean getProcessAllPages() {
        return processAllPages;
    }

    public void setFullDetails(boolean fullDetails) {
        this.fullDetails = fullDetails;
    }

    public void setProcessAllPages(boolean processAllPages) {
        this.processAllPages = processAllPages;
    }
}
