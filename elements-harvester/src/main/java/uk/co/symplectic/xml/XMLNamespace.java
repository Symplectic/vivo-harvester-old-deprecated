/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

public class XMLNamespace {
    String prefix;
    String uri;

    public XMLNamespace(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getURI() {
        return uri;
    }
}

