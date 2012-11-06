/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

class ElementsAPIConnectionManager {
    private static HttpConnectionManager connectionManager;
    private static HttpConnectionManagerParams params = null;

    static {
        params = new HttpConnectionManagerParams();
        params.setConnectionTimeout(30000);
    }

    private ElementsAPIConnectionManager() {}

    static HttpConnectionManager getInstance() {
        if (connectionManager == null) {
            ElementsAPIConnectionManager.initialize();
        }

        return connectionManager;
    }

    private static synchronized void initialize() {
        if (connectionManager == null) {
            connectionManager = new MultiThreadedHttpConnectionManager();
            if (params != null) {
                connectionManager.setParams(params);
            }
        }
    }
}
