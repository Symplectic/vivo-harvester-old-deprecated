/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.io.InputStream;

class ElementsAPIHttpClient {
    private String username;
    private String password;

    private String url;

    ElementsAPIHttpClient(String url, String username, String password) {
        this.url      = url;
        this.username = username;
        this.password = password;

    }

    ElementsAPIHttpClient(String url) {
        this.url = url;
    }

    InputStream executeGetRequest() throws IOException {
        HttpClient client = new HttpClient(ElementsAPIConnectionManager.getInstance());
        if (username != null) {
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        }

        GetMethod getMethod = new GetMethod(url);
        client.executeMethod(getMethod);
        return getMethod.getResponseBodyAsStream();
    }

    InputStream executeGetRequest(int maxRetries) throws IOException {
        if (maxRetries == 0) {
            return executeGetRequest();
        }

        IOException lastError = null;

        while (maxRetries-- > 0) {
            try {
                return executeGetRequest();
            } catch (IOException io) {
                lastError = io;
            }
        }

        throw lastError;
    }
}
