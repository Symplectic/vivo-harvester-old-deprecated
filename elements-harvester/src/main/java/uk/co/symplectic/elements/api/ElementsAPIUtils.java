/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

public final class ElementsAPIUtils {
    public static String normalizeUrl(String url) {
        while (url.length() > 0 && url.endsWith("/")) {
            url = url.substring(0, url.length() -1);
        }

        return url;
    }
}
