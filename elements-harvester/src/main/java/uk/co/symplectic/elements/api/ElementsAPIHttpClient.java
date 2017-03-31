/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class ElementsAPIHttpClient {
    private String username;
    private String password;

    private String url;

    private static int soTimeout = 5 * 60 * 1000; // 5 minutes, in milliseconds

    public static void setSoTimeout(int millis) {
        soTimeout = millis;
    }

    ElementsAPIHttpClient(String url, String username, String password) {
        this.url      = url;
        this.username = username;
        this.password = password;
    }

    ElementsAPIHttpClient(String url) {
        this.url = url;
    }

    InputStream executeGetRequest() throws IOException {
        // Prepare the HttpClient
        HttpClient client = new HttpClient(ElementsAPIConnectionManager.getInstance());
        if (username != null) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            client.getState().setCredentials(AuthScope.ANY, credentials);
            HttpClientParams params = new HttpClientParams(client.getParams());
            params.setSoTimeout(soTimeout);
            client.setParams(params);
        }

        // Issue get request
        GetMethod getMethod = new GetMethod(url);
        client.executeMethod(getMethod);
        return getMethod.getResponseBodyAsStream();
    }

    /**
     * Execute a get request,
     * @param maxRetries Number of times to retry the request
     * @return InputStream corresponding to the request body
     * @throws IOException Failure reading the request stream
     */
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
