/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

class ElementsAPIURLValidator {
    private String url;
    private boolean isSecured;

    private String failureMsg = null;

    ElementsAPIURLValidator(String url, boolean isSecured) {
        this.url = url;
        this.isSecured = isSecured;
    }

    boolean isValid() {
        failureMsg = null;
        if (url == null) {
            return failValidation("URL must not be null");
        }

        if (isSecured) {
            if (!urlStartsWithHTTPS()) {
                return failValidation("Secured endpoint does not begin with https://");
            }

        } else if (!urlStartsWithHTTP()) {
            return failValidation("Unsecured endpoint does not begin with http://");
        }

        try {
            URL urlCheck = new URL(url);
            urlCheck.toURI();
        } catch (URISyntaxException use) {
            return failValidation("Endpoint is not a valid URI");
        } catch (MalformedURLException mue) {
            return failValidation("Endpoint is not a valid URL");
        }

        return true;
    }

    String getLastValidationMessage() {
        return failureMsg;
    }

    private boolean failValidation(String msg) {
        failureMsg = msg;
        return false;
    }

    private boolean urlStartsWithHTTP() {
        if (url == null || url.length() < 8) {
            return false;
        }

        return "http://".equalsIgnoreCase(url.substring(0, 7));
    }

    private boolean urlStartsWithHTTPS() {
        if (url == null || url.length() < 9) {
            return false;
        }

        return "https://".equalsIgnoreCase(url.substring(0, 8));
    }
}
