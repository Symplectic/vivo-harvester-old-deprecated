/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.utils.DeletionService;
import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElementsRdfStore {
    private File dir = null;

    private LayoutStrategy layoutStrategy = new DefaultLayoutStrategy();
    private DeletionService deletionService = new DeletionService();
    private FileTempCache fileMemStore = new FileTempCache();

    private boolean keepEmpty = false;

    private final List<ElementsRdfStoreObserver> storeObservers = new ArrayList<ElementsRdfStoreObserver>();

    public ElementsRdfStore(String dir) {
        this.dir = new File(dir);
    }

    public ElementsRdfStore setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
        return this;
    }

    public ElementsRdfStore addObserver(ElementsRdfStoreObserver newObserver) {
        this.storeObservers.add(newObserver);
        return this;
    }

    public void pruneExcept(ElementsObjectCategory category, Set<String> idsToKeep) {
        if (dir != null) {
            File objectDir = new File(dir, category.getSingular());
            if (objectDir.exists()) {
                pruneIn(objectDir, idsToKeep, null);
            } else {
                pruneIn(dir, idsToKeep, category.getSingular());
            }
        }
    }

    private void pruneIn(File dir, Set<String> idsToKeep, String prefix) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    pruneIn(file, idsToKeep, prefix);
                } else if (StringUtils.isEmpty(prefix)) {
                    if (idsToKeep.contains(file.getName())) {
                        deletionService.keep(file);
                    } else {
                        deletionService.deleteOnExit(file);
                    }
                } else if (file.getName().startsWith(prefix)) {
                    boolean keepFile = false;
                    for (String id : idsToKeep) {
                        if (file.getName().equals(prefix + id)) {
                            keepFile = true;
                        }
                    }

                    if (keepFile) {
                        deletionService.keep(file);
                    } else {
                        deletionService.deleteOnExit(file);
                    }
                }
            }
        }
    }

    public RdfTranslationResult getObjectTranslationResult(ElementsObjectInfo objectInfo) {
        return new RdfTranslationResult(this, objectInfo);
    }

    public RdfTranslationResult getRelationshipTranslationResult(ElementsRelationshipInfo relationshipInfo) {
        return new RdfTranslationResult(this, relationshipInfo);
    }

    public void writeObject(ElementsObjectInfo objectInfo, byte[] rdf) throws IOException {
        File file = layoutStrategy.getObjectFile(dir, objectInfo.getCategory(), objectInfo.getId(), FileFormat.RDF_XML);

        if (keepEmpty || (rdf != null && rdf.length > 0)) {
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(rdf);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

            if (fileMemStore != null) {
                fileMemStore.put(file, rdf);
            }

            for (ElementsRdfStoreObserver observer : storeObservers) {
                observer.storedObjectRdf(objectInfo, file);
            }
        }
    }

    public boolean writeObjectExtra(ElementsObjectInfo objectInfo, String type, byte[] rdf, FileFormat format) {
        File file = layoutStrategy.getObjectExtraFile(dir, objectInfo.getCategory(), objectInfo.getId(), type, format);

        if (file != null) {
            try {
                OutputStream os = new FileOutputStream(file);
                try {
                    os.write(rdf);
                } finally {
                    os.close();
                }

                fileMemStore.put(file, rdf);

                for (ElementsRdfStoreObserver observer : storeObservers) {
                    observer.storedObjectExtraRdf(objectInfo, type, file);
                }
            } catch (IOException ioe) {
                // Log error
                return false;
            }
        }

        return true;
    }

    public void writeRelationship(ElementsRelationshipInfo relationshipInfo, byte[] rdf) throws IOException {
        File file = layoutStrategy.getRelationshipFile(dir, relationshipInfo.getId(), FileFormat.RDF_XML);

        if (keepEmpty || (rdf != null && rdf.length > 0)) {
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(rdf);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

            if (fileMemStore != null) {
                fileMemStore.put(file, rdf);
            }

            for (ElementsRdfStoreObserver observer : storeObservers) {
                observer.storedRelationshipRdf(relationshipInfo, file);
            }
        }
    }
}
