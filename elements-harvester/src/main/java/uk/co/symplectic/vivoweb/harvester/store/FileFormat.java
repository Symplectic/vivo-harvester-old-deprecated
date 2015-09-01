/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import java.util.HashMap;
import java.util.Map;

public final class FileFormat {
    private static final Map<String, FileFormat> formats = new HashMap<String, FileFormat>();

    public static final FileFormat XML     = new FileFormat("xml",    "xml");
    public static final FileFormat RDF_XML = new FileFormat("rdfxml", "xml");
    public static final FileFormat TRIG    = new FileFormat("trig",   "trig");

    private final String label;
    private final String extension;

    private FileFormat(String label, String ext) {
        this.label = label;
        this.extension = ext;

        formats.put(label, this);
    }

    public String getLabel() {
        return label;
    }

    public String getExtension() {
        return extension;
    }

    public static FileFormat valueOf(String value) {
        return formats.get(value);
    }
}
