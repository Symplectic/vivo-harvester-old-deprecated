/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

class ElementsFeedPagination {
    private int itemsPerPage;
    private String firstURL;
    private String lastURL;
    private String previousURL;
    private String nextURL;

    int getItemsPerPage() {
        return itemsPerPage;
    }

    String getFirstURL() {
        return firstURL;
    }

    String getLastURL() {
        return lastURL;
    }

    String getPreviousURL() {
        return previousURL;
    }

    String getNextURL() {
        return nextURL;
    }

    void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    void setFirstURL(String firstURL) {
        this.firstURL = firstURL;
    }

    void setLastURL(String lastURL) {
        this.lastURL = lastURL;
    }

    void setPreviousURL(String previousURL) {
        this.previousURL = previousURL;
    }

    void setNextURL(String nextURL) {
        this.nextURL = nextURL;
    }
}
