/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.repo.JenaConnect;

import java.io.*;
import java.nio.file.Files;

final class Transfer {
    private static final Logger log = LoggerFactory.getLogger(Transfer.class);

    public Transfer() {

    }

//    Transfer() {}

    public boolean transfer(JenaConnect outputStore, File transferredRdf, File translatedRdf) throws Exception {
        log.trace("Transferring: " + transferredRdf.toPath());
        if (transferredRdf != null && transferredRdf.exists()) {
            if (transferredRdf.length() > 3) {
                try {
                    Model model = loadRdfXml(transferredRdf);
                    outputStore.getJenaModel().remove(model);
                    model.close();
                } catch (Exception e) {
                    log.error("Unable to unload RDF: " + transferredRdf.getAbsolutePath(), e);
                    throw e;
                }
            }

            boolean deleted = false;
            try {
                deleted = transferredRdf.delete();
            } catch (Exception e) {
                log.error("Unable to delete unloaded RDF: " + transferredRdf.getAbsolutePath(), e);
            }

            if (!deleted) {
                try {
                    outputStore.loadRdfFromFile(transferredRdf.getAbsolutePath(), null, null);
                    return false;
                } catch (Exception e) {
                    log.error("Unable to restore unloaded RDF: " + transferredRdf.getAbsolutePath(), e);
                    throw e;
                }
            }
        }

        if (translatedRdf != null && translatedRdf.exists()) {
            if (translatedRdf.length() > 3) {
                try {
                    outputStore.loadRdfFromFile(translatedRdf.getAbsolutePath(), null, null);
                } catch (Exception e) {
                    log.error("Unable to load RDF: " + translatedRdf.getAbsolutePath(), e);
                    throw e;
                }

                try {
                    Files.move(translatedRdf.toPath(), transferredRdf.toPath());
                } catch (Exception e) {
                    log.error("Unable to move file " + transferredRdf.toPath() + " to " + translatedRdf.toPath(), e);

                    try {
                        Model model = loadRdfXml(translatedRdf);
                        outputStore.getJenaModel().remove(model);
                        model.close();
                    } catch (Exception ee) {
                        log.error("Unable to remove loaded RDF: " + translatedRdf.getAbsolutePath(), ee);
                    }
                    throw e;
                }
            } else {
                translatedRdf.delete();
            }
        }

        return true;
    }

    private Model loadRdfXml(File rdfXml) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(rdfXml));
        Model model = ModelFactory.createDefaultModel();

        model.read(is, null);

        is.close();

        return model;
    }
}
