/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public final class FileFormat {
    private static final Map<String, FileFormat> formats = new HashMap<String, FileFormat>();

    public static final FileFormat XML     = new FileFormat("xml",    "xml",  null);
    public static final FileFormat RDF_XML = new FileFormat("rdfxml", "rdf",  "RDF/XML");
    public static final FileFormat TRIG    = new FileFormat("trig",   "trig", "TriG");
    public static final FileFormat TURTLE  = new FileFormat("turtle", "ttl",  "TURTLE");

    private final String label;
    private final String extension;
    private final String jenaFormat;

    private FileFormat(String label, String ext, String jenaFormat) {
        this.label = label;
        this.extension = ext;
        this.jenaFormat = jenaFormat;

        formats.put(label.toLowerCase(), this);
    }

    public String getLabel() {
        return label;
    }

    public String getExtension() {
        return extension;
    }

    public String getJenaLang() {
        return jenaFormat;
    }

    public static FileFormat valueOf(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        FileFormat format = formats.get(value.toLowerCase());
        if (format == null) {
            throw new IllegalArgumentException("Unsupported RDF File Format: " + value);
        }

        return formats.get(value);
    }
}
