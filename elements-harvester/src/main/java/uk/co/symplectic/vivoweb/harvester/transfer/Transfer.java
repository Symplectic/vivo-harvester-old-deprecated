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
import uk.co.symplectic.vivoweb.harvester.cache.CachingService;
import uk.co.symplectic.vivoweb.harvester.jena.JenaHelper;
import uk.co.symplectic.vivoweb.harvester.jena.JenaWrapper;

import java.io.*;
import java.nio.file.Files;

final class Transfer {
    private static final Logger log = LoggerFactory.getLogger(Transfer.class);
    private static final CachingService cachingService = new CachingService();

    public Transfer() {
    }

    public boolean transfer(JenaWrapper outputStore, File transferredRdf, File translatedRdf) throws Exception {
        Model transferredModel = null;
        try {
            if (transferredRdf != null && transferredRdf.exists() && transferredRdf.length() > 3) {
                transferredModel = JenaHelper.loadRdfXml(transferredRdf);
            }

            if (transferredModel != null) {
                outputStore.remove(transferredModel);
            }

            if (transferredRdf.exists()) {
                boolean deleted = false;
                try {
                    deleted = transferredRdf.delete();
                } catch (Exception e) {
                    log.error("Unable to delete unloaded RDF: " + transferredRdf.getAbsolutePath(), e);
                }

                if (!deleted && transferredModel != null) {
                    outputStore.add(transferredModel);
                    return false;
                }
            }
        } finally {
            if (transferredModel != null) {
                transferredModel.close();
            }
        }

        Model translatedModel = null;
        try {
            if (translatedRdf != null && translatedRdf.exists() && translatedRdf.length() > 3) {
                translatedModel = JenaHelper.loadRdfXml(translatedRdf);
            }

            if (translatedModel != null) {
                outputStore.add(translatedModel);
                try {
                    Files.move(translatedRdf.toPath(), transferredRdf.toPath());
                } catch (Exception e) {
                    log.error("Unable to move file " + translatedRdf.toPath() + " to " + transferredRdf.toPath(), e);
                    outputStore.remove(translatedModel);
                }
            } else if (translatedRdf.exists()) {
                translatedRdf.delete();
            }
        } finally {
            if (translatedModel != null) {
                translatedModel.close();
            }
            cachingService.remove(translatedRdf);
        }

        return true;
    }
}
