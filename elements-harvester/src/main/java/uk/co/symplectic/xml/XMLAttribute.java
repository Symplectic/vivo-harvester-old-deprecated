/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

public class XMLAttribute {
    String prefix;
    String name;
    String value;

    public XMLAttribute(String prefix, String name, String value) {
        this.prefix = prefix;
        this.name = name;
        this.value = value;
    }

    public XMLAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

