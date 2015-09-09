/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
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
    private final Model tripleStore;

    // Destination inference store
    private final Model inferenceStore;

    // Cache of loaded RDF
    private File dir = null;

    // Strategy for laying out the cache directory
    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    // Access to a temporary cache for passing data between stores
    private FileTempCache fileMemStore = new FileTempCache();

    public ElementsTransferredRdfStore(Model outputStore, Model inferenceStore, String dir) {
        this.tripleStore = outputStore;
        this.inferenceStore = inferenceStore;
        this.dir = new File(dir);
    }

    /**
     * Replace the RDF associated with an "object" (user, publication, etc.)
     */
    public void replaceObjectRdf(ElementsObjectInfo objectInfo, File storedRdf, FileFormat rdfFormat) throws IOException {
        transfer(objectInfo.getCategory().getPlural(), layoutStrategy.getObjectFile(dir, objectInfo.getCategory(), objectInfo.getId(), rdfFormat), storedRdf, rdfFormat);
    }

    /**
     * Replace the RDF associated with an extra RDF file related to an "object" (e.g. used for user photos)
     */
    public void replaceObjectExtraRdf(ElementsObjectInfo objectInfo, String type, File storedRdf, FileFormat rdfFormat) throws IOException {
        transfer(null, layoutStrategy.getObjectExtraFile(dir, objectInfo.getCategory(), objectInfo.getId(), type, rdfFormat), storedRdf, rdfFormat);
    }

    /**
     * Replace the RDF associated with a relationship
     */
    public void replaceRelationshipRdf(ElementsRelationshipInfo relationshipInfo, File storedRdf, FileFormat rdfFormat) throws IOException {
        transfer(Statistics.RELATIONSHIPS, layoutStrategy.getRelationshipFile(dir, relationshipInfo.getId(), rdfFormat), storedRdf, rdfFormat);
    }

    /**
     * Transfer the RDF to the triple store, removing the cached data and updating the cache as necessary
     * Note: Handles either file not being present, so general purpose method for adding, updating and removing RDF
     */
    private boolean transfer(String statisticsCategory, File transferredRdf, File translatedRdf, FileFormat rdfFormat) throws IOException {
        boolean wasRemoved = false;
        boolean wasAdded = false;

        // Jena model for data that has previously been loaded into the triple store
        Model transferredModel = null;
        try {
            // If we have previously loaded data, construct the Jena model
            transferredModel = loadRdf(transferredRdf, rdfFormat);

            // If we have constructed a model of previously loaded data, remove it from the output store
            if (transferredModel != null) {
                tripleStore.enterCriticalSection(Lock.WRITE);
                try {
                    tripleStore.remove(transferredModel);
                    wasRemoved = true;
                } finally {
                    tripleStore.leaveCriticalSection();
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
                    tripleStore.enterCriticalSection(Lock.WRITE);
                    try {
                        tripleStore.add(transferredModel);
                        wasRemoved = false;
                    } finally {
                        tripleStore.leaveCriticalSection();
                    }
                    return false;
                }
            }
        } catch (IOException ioe) {
            log.error("Unable to read transferred RDF" + (transferredRdf == null ? "" : transferredRdf.getAbsolutePath()), ioe);
            throw ioe;
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
            translatedModel = loadRdf(translatedRdf, rdfFormat);

            // If we have constructed a model of data to load, add it to the output store
            if (translatedModel != null) {
                tripleStore.enterCriticalSection(Lock.WRITE);
                try {
                    tripleStore.add(translatedModel);
                    wasAdded = true;
                } finally {
                    tripleStore.leaveCriticalSection();
                }

                try {
                    // We've added the new data, so move the incoming file to the previously transferred store
                    // (this allows us to use it for removing the loaded data on a future update)
                    Files.move(translatedRdf.toPath(), transferredRdf.toPath());
                } catch (Exception e) {
                    log.error("Unable to move file " + translatedRdf.toPath() + " to " + transferredRdf.toPath(), e);
                    // Oops, we couldn't move the file, so remove the newly loaded data from the triple store
                    if (!transferredRdf.exists()) {
                        tripleStore.enterCriticalSection(Lock.WRITE);
                        try {
                            tripleStore.remove(translatedModel);
                            wasAdded = false;
                        } finally {
                            tripleStore.leaveCriticalSection();
                        }
                    }
                }
            } else {
                // The translated data was empty, so just remove the file
                if (translatedRdf != null && translatedRdf.exists()) {
                    if (!translatedRdf.delete()) {
                        log.error("Unable to remove unused translated RDF");
                    }
                }
            }
        } catch (IOException ioe) {
            log.error("Unable to read translated RDF" + (translatedRdf == null ? "" : translatedRdf.getAbsolutePath()), ioe);
            throw ioe;
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
    private Model loadRdf(File rdfFile, FileFormat rdfFormat) throws IOException {
        if (rdfFile != null) {
            InputStream is = getRdfInputStream(rdfFile);
            if (is != null) {
                try {
                    if (rdfFormat != null) {
                        return ModelFactory.createDefaultModel().read(is, null, rdfFormat.getJenaLang());
                    } else {
                        return ModelFactory.createDefaultModel().read(is, null);
                    }
                } catch (JenaException je) {
                    log.error("Unable to read " + rdfFile.getName(), je);
                    throw new IOException("Unable to read " + rdfFile.getName(), je);
                } finally {
                    is.close();
                }
            }
        }

        return null;
    }

    // Helper method to get an RDF/XML data stream
    private InputStream getRdfInputStream(File rdfFile) throws IOException {
        // Check whether there is a cached document for the RDF/XML file
        byte[] xml = fileMemStore.remove(rdfFile);
        if (xml != null && xml.length > 2) {
            // Return a memory based InputStream from the cached document
            return new ByteArrayInputStream(xml);
        } else if (rdfFile.exists() && rdfFile.length() > 2) {
            // Return a file based InputStream
            return new BufferedInputStream(new FileInputStream(rdfFile));
        }

        return null;
    }
}
