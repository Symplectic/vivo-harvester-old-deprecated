/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.*;

public class JenaHelper {
    public static Model loadRdfXml(File rdfXml) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(rdfXml));
        Model model = ModelFactory.createDefaultModel();
        model.read(is, null);
        is.close();
        return model;
    }

    public static Model loadRdfXml(InputStream is) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(is, null);
        return model;
    }
}
