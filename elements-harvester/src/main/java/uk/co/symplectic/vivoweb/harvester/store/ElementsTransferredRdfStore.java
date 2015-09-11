/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import org.apache.commons.lang.StringUtils;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
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
    private final Node  inferenceGraph;

    // Cache of loaded RDF
    private File dir = null;

    // Strategy for laying out the cache directory
    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();

    // Access to a temporary cache for passing data between stores
    private FileTempCache fileMemStore = new FileTempCache();

    public ElementsTransferredRdfStore(Model outputStore, String dir, Model inferenceStore, String inferenceGraphUri) {
        this.tripleStore = outputStore;
        this.inferenceStore = inferenceStore;
        if (inferenceStore != null && !StringUtils.isEmpty(inferenceGraphUri)) {
            this.inferenceGraph = Node.createURI(inferenceGraphUri);
        } else {
            this. inferenceGraph = null;
        }

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

        try {
            // Jena model for data that has previously been loaded into the triple store
            Models transferredModels = null;
            try {
                // If we have previously loaded data, construct the Jena model
                transferredModels = loadRdf(transferredRdf, rdfFormat);

                // If we have constructed a model of previously loaded data, remove it from the output store
                if (transferredModels != null) {
                    if (transferredModels.assertions != null) {
                        tripleStore.enterCriticalSection(Lock.WRITE);
                        try {
                            tripleStore.remove(transferredModels.assertions);
                            wasRemoved = true;
                        } finally {
                            tripleStore.leaveCriticalSection();
                        }
                    }

                    if (wasRemoved && inferenceStore != null && transferredModels.inferences != null) {
                        inferenceStore.enterCriticalSection(Lock.WRITE);
                        try {
                            inferenceStore.remove(transferredModels.inferences);
                        } finally {
                            inferenceStore.leaveCriticalSection();
                        }
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
                    if (!deleted && transferredModels != null) {
                        if (transferredModels.assertions != null) {
                            tripleStore.enterCriticalSection(Lock.WRITE);
                            try {
                                tripleStore.add(transferredModels.assertions);
                                wasRemoved = false;
                            } finally {
                                tripleStore.leaveCriticalSection();
                            }
                        }

                        if (!wasRemoved && inferenceStore != null && transferredModels.inferences != null) {
                            inferenceStore.enterCriticalSection(Lock.WRITE);
                            try {
                                inferenceStore.add(transferredModels.inferences);
                            } finally {
                                inferenceStore.leaveCriticalSection();
                            }
                        }
                        return false;
                    }
                }
            } finally {
                // We no longer need the model of previously loaded data, so free the resources
                if (transferredModels != null) {
                    if (transferredModels.assertions != null) {
                        transferredModels.assertions.close();
                    }
                    if (transferredModels.inferences != null) {
                        transferredModels.inferences.close();
                    }
                }
            }
        } catch (IOException ioe) {
            log.error("Unable to read transferred RDF" + (transferredRdf == null ? "" : transferredRdf.getAbsolutePath()), ioe);
            throw ioe;
        }

        try {
            // Jena model for data that needs to be loaded
            Models translatedModels = null;
            try {
                // If we have data to load, construct the Jena model
                translatedModels = loadRdf(translatedRdf, rdfFormat);

                // If we have constructed a model of data to load, add it to the output store
                if (translatedModels != null && translatedModels.assertions != null) {
                    tripleStore.enterCriticalSection(Lock.WRITE);
                    try {
                        tripleStore.add(translatedModels.assertions);
                        wasAdded = true;
                    } finally {
                        tripleStore.leaveCriticalSection();
                    }

                    if (inferenceStore != null && translatedModels.inferences != null) {
                        inferenceStore.enterCriticalSection(Lock.WRITE);
                        try {
                            inferenceStore.add(translatedModels.inferences);
                        } finally {
                            inferenceStore.leaveCriticalSection();
                        }
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
                                tripleStore.remove(translatedModels.assertions);
                                wasAdded = false;
                            } finally {
                                tripleStore.leaveCriticalSection();
                            }

                            if (inferenceStore != null && translatedModels.inferences != null) {
                                inferenceStore.enterCriticalSection(Lock.WRITE);
                                try {
                                    inferenceStore.remove(translatedModels.inferences);
                                } finally {
                                    inferenceStore.leaveCriticalSection();
                                }
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
            } finally {
                // We no longer need the model of data to add, so free the resources
                if (translatedModels != null) {
                    if (translatedModels.assertions != null) {
                        translatedModels.assertions.close();
                    }
                    if (translatedModels.inferences != null) {
                        translatedModels.inferences.close();
                    }
                }

            }
        } catch (IOException ioe) {
            log.error("Unable to read translated RDF" + (translatedRdf == null ? "" : translatedRdf.getAbsolutePath()), ioe);
            throw ioe;
        } finally {
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
    private Models loadRdf(File rdfFile, FileFormat rdfFormat) throws IOException {
        if (rdfFile != null) {
            Models models = new Models();
            InputStream is = getRdfInputStream(rdfFile);
            if (is != null) {
                try {
                    if (rdfFormat == FileFormat.TRIG) {
                        DatasetGraph graph = DatasetGraphFactory.createMem();
                        RiotLoader.read(is, graph, Lang.get(rdfFormat.getJenaLang()), null);

                        models.assertions = ModelFactory.createModelForGraph(graph.getDefaultGraph());
                        Graph infGraph = graph.getGraph(inferenceGraph);
                        if (infGraph != null && !infGraph.isEmpty()) {
                            models.inferences = ModelFactory.createModelForGraph(infGraph);
                            if (models.inferences.isEmpty()) {
                                models.inferences.close();
                                models.inferences = null;
                            }
                        }

                        return models;
                    } else if (rdfFormat != null) {
                        models.assertions = ModelFactory.createDefaultModel().read(is, null, rdfFormat.getJenaLang());
                        return models;
                    } else {
                        models.assertions = ModelFactory.createDefaultModel().read(is, null);
                        return models;
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

    private static class Models {
        Model assertions = null;
        Model inferences = null;
    }
}
