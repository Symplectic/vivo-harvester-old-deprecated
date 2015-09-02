/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.vivoweb.harvester.util.Statistics;

import java.io.*;
import java.nio.file.Files;

/**
 * Class for managing the store of "transferred" RDF data - i.e. data that has been loaded into a triple store.
 *
 * It is responsible for managing access to the destination triple store, and the cache of loaded RDF files.
 *
 * By maintaining the cache of RDF files, it can use the cached file to remove the loaded triples
 * when it needs to remove a file (e.g. object is deleted), or update the existing contents.
 */
public class ElementsTransferredRdfStore {
    private final static Logger log = LoggerFactory.getLogger(ElementsTransferredRdfStore.class);

    // Destination triple store
    private Model tripleStore;

    // Cache of loaded RDF
    private File dir = null;

    // Strategy for laying out the cache directory
    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    // Access to a temporary cache for passing data between stores
    private FileTempCache fileMemStore = new FileTempCache();

    public ElementsTransferredRdfStore(Model outputStore, String dir) {
        this.tripleStore = outputStore;
        this.dir = new File(dir);
    }

    /**
     * Replace the RDF associated with an "object" (user, publication, etc.)
     */
    public void replaceObjectRdf(ElementsObjectInfo objectInfo, File storedRdf) throws Exception {
        transfer(objectInfo.getCategory().getPlural(), layoutStrategy.getObjectFile(dir, objectInfo.getCategory(), objectInfo.getId(), FileFormat.RDF_XML), storedRdf);
    }

    /**
     * Replace the RDF associated with an extra RDF file related to an "object" (e.g. used for user photos)
     */
    public void replaceObjectExtraRdf(ElementsObjectInfo objectInfo, String type, File storedRdf) throws Exception {
        transfer(null, layoutStrategy.getObjectExtraFile(dir, objectInfo.getCategory(), objectInfo.getId(), type, FileFormat.RDF_XML), storedRdf);
    }

    /**
     * Replace the RDF associated with a relationship
     */
    public void replaceRelationshipRdf(ElementsRelationshipInfo relationshipInfo, File storedRdf) throws Exception {
        transfer(Statistics.RELATIONSHIPS, layoutStrategy.getRelationshipFile(dir, relationshipInfo.getId(), FileFormat.RDF_XML), storedRdf);
    }

    /**
     * Transfer the RDF to the triple store, removing the cached data and updating the cache as necessary
     * Note: Handles either file not being present, so general purpose method for adding, updating and removing RDF
     */
    private boolean transfer(String statisticsCategory, File transferredRdf, File translatedRdf) throws Exception {
        boolean wasRemoved = false;
        boolean wasAdded = false;

        // Jena model for data that has previously been loaded into the triple store
        Model transferredModel = null;
        try {
            // If we have previously loaded data, construct the Jena model
            if (transferredRdf != null && transferredRdf.exists() && transferredRdf.length() > 3) {
                transferredModel = loadRdfXml(transferredRdf);
            }

            // If we have constructed a model of previously loaded data, remove it from the output store
            if (transferredModel != null) {
                synchronized (tripleStore) {
                    tripleStore.remove(transferredModel);
                    wasRemoved = true;
                }
            }

            // If a file exists in the previously transferred directory
            if (transferredRdf != null && transferredRdf.exists()) {
                // Delete the file
                boolean deleted = false;
                try {
                    deleted = transferredRdf.delete();
                } catch (Exception e) {
                    log.error("Unable to delete unloaded RDF: " + transferredRdf.getAbsolutePath(), e);
                }

                // If we were unable to delete the file, reload the previously loaded data and abort
                if (!deleted && transferredModel != null) {
                    synchronized (tripleStore) {
                        tripleStore.add(transferredModel);
                        wasRemoved = false;
                    }
                    return false;
                }
            }
        } finally {
            // We no longer need the model of previously loaded data, so free the resources
            if (transferredModel != null) {
                transferredModel.close();
            }
        }

        // Jena model for data that needs to be loaded
        Model translatedModel = null;
        try {
            // If we have data to load, construct the Jena model
            if (translatedRdf != null && translatedRdf.exists() && translatedRdf.length() > 3) {
                translatedModel = loadRdfXml(translatedRdf);
            }

            // If we have constructed a model of data to load, add it to the output store
            if (translatedModel != null) {
                synchronized (tripleStore) {
                    tripleStore.add(translatedModel);
                    wasAdded = true;
                }
                try {
                    // We've added the new data, so move the incoming file to the previously transferred store
                    // (this allows us to use it for removing the loaded data on a future update)
                    Files.move(translatedRdf.toPath(), transferredRdf.toPath());
                } catch (Exception e) {
                    log.error("Unable to move file " + translatedRdf.toPath() + " to " + transferredRdf.toPath(), e);
                    // Oops, we couldn't move the file, so remove the newly loaded data from the triple store
                    if (!transferredRdf.exists()) {
                        synchronized (tripleStore) {
                            tripleStore.remove(translatedModel);
                            wasAdded = false;
                        }
                    }
                }
            } else {
                // The translated data was empty, so just remove the file
                if (translatedRdf != null && translatedRdf.exists()) {
                    if (!translatedRdf.delete()) {
                        log.error("Unable to remove unused translated RDF/XML");
                    }
                }
            }
        } finally {
            // We no longer need the model of data to add, so free the resources
            if (translatedModel != null) {
                translatedModel.close();
            }

            if (statisticsCategory != null) {
                if (wasRemoved && wasAdded) {
                    Statistics.updated(statisticsCategory);
                } else if (wasRemoved) {
                    Statistics.removed(statisticsCategory);
                } else if (wasAdded) {
                    Statistics.added(statisticsCategory);
                }
            }
        }

        return true;
    }

    // Helper method to load RDF/XML to a Jena Model
    private Model loadRdfXml(File rdfXml) throws IOException {
        Model model = ModelFactory.createDefaultModel();

        InputStream is = getRdfXmlInputStream(rdfXml);
        try {
            model.read(is, "RDF/XML");
            // "TURTLE"
            // "TriG"
        } finally {
            is.close();
        }

        return model;
    }

    // Helper method to get an RDF/XML data stream
    private InputStream getRdfXmlInputStream(File rdfXml) throws IOException {
        // Check whether there is a cached document for the RDF/XML file
        byte[] xml = fileMemStore.remove(rdfXml);
        if (xml != null) {
            // Return a memory based InputStream from the cached document
            return new ByteArrayInputStream(xml);
        }

        // Return a file based InputStream
        return new BufferedInputStream(new FileInputStream(rdfXml));
    }
}
