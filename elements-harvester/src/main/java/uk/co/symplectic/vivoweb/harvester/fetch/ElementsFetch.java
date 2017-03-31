/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.symplectic.elements.api.ElementsAPI;
import uk.co.symplectic.elements.api.ElementsAPIFeedObjectQuery;
import uk.co.symplectic.elements.api.ElementsAPIFeedRelationshipQuery;
import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.store.*;
import uk.co.symplectic.vivoweb.harvester.util.Statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ElementsFetch {
    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(ElementsFetch.class);

    private String objectsToHarvest;
    private String groupsToHarvest;

    // Default of 25 is required by 4.6 API since we request full detail for objects
    private int objectsPerPage = 25;

    // Default of 100 for optimal performance
    private int relationshipsPerPage = 100;

    private Date modifiedSince = null;
    private boolean elementsDeltas = false;

    private final List<ElementsObjectObserver> objectObservers = new ArrayList<ElementsObjectObserver>();
    private final List<ElementsRelationshipObserver> relationshipObservers = new ArrayList<ElementsRelationshipObserver>();

    private final List<ElementsFetchObserver> fetchObservers = new ArrayList<ElementsFetchObserver>();

    private ElementsAPI elementsAPI = null;

    public ElementsFetch(ElementsAPI api) {
        if (api == null) {
            throw new IllegalStateException();
        }

        this.elementsAPI = api;
    }

    public ElementsFetch addObjectObserver(ElementsObjectObserver newObserver) {
        objectObservers.add(newObserver);
        return this;
    }

    public ElementsFetch addRelationshipObserver(ElementsRelationshipObserver newObserver) {
        relationshipObservers.add(newObserver);
        return this;
    }

    public ElementsFetch setElementsDeltas(boolean pruning) {
        this.elementsDeltas = pruning;
        return this;
    }

    public ElementsFetch setModifiedSince(Date modifiedSince) {
        this.modifiedSince = modifiedSince == null ? null : new Date(modifiedSince.getTime());
        return this;
    }

    public ElementsFetch setGroupsToHarvest(String groupsToHarvest) {
        this.groupsToHarvest = groupsToHarvest;
        return this;
    }

    public ElementsFetch setObjectsToHarvest(String objectsToHarvest) {
        this.objectsToHarvest = objectsToHarvest;
        return this;
    }

    public ElementsFetch setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
        return this;
    }

    public ElementsFetch setRelationshipsPerPage(int relationshipsPerPage) {
        this.relationshipsPerPage = relationshipsPerPage;
        return this;
    }

    public ElementsFetch addFetchObserver(ElementsFetchObserver newObserver) {
        this.fetchObservers.add(newObserver);
        return this;
    }
    /**
     * Executes the task
     * @throws IOException error processing search
     */
    public void execute() throws IOException {
        ElementsObjectStore objectStore = ElementsStoreFactory.getObjectStore();
        ElementsRdfStore rdfStore = ElementsStoreFactory.getRdfStore();

        ElementsAPIFeedObjectQuery deletedQuery = new ElementsAPIFeedObjectQuery();
        ElementsAPIFeedObjectQuery feedQuery = new ElementsAPIFeedObjectQuery();

        // Ensure deleted query is just for deleted objects
        deletedQuery.setDeleted(true);

        // When retrieving objects, always get the full record
        feedQuery.setFullDetails(true);

        // Get N objects per request
        deletedQuery.setPerPage(objectsPerPage);
        feedQuery.setPerPage(objectsPerPage);

        // Load all pages, not just one
        deletedQuery.setProcessAllPages(true);
        feedQuery.setProcessAllPages(true);

        if (!StringUtils.isEmpty(groupsToHarvest)) {
            deletedQuery.setGroups(groupsToHarvest);
            feedQuery.setGroups(groupsToHarvest);
        }

        if (modifiedSince != null) {
            deletedQuery.setModifiedSince(modifiedSince);
            feedQuery.setModifiedSince(modifiedSince);
        }

        // objectsToHarvest is a comma delimited list of object categories that we wish to pull
        // As the API requires that we handle each category separately, we split the string and loop over the contents
        for (String category : objectsToHarvest.split("\\s*,\\s*")) {
            ElementsObjectCategory eoCategory = ElementsObjectCategory.valueOf(category);
            if (eoCategory != null) {
                Statistics.register(eoCategory.getPlural());
                feedQuery.setCategory(eoCategory);
                elementsAPI.execute(feedQuery, new ElementsObjectHandler(objectStore).addObservers(objectObservers).addObserver(ObjectStatisticsObserver.modifiedCounter()));

                // If we are only processing changes, we need to get the deleted relationships
                if (modifiedSince != null) {
                    deletedQuery.setCategory(eoCategory);
                    elementsAPI.execute(deletedQuery, new ElementsObjectHandler(objectStore).addObservers(objectObservers).addObserver(ObjectStatisticsObserver.deletedCounter()));
                }
            }
        }

        ElementsObjectsInRelationships objectsInRelationships = new ElementsObjectsInRelationships();
        if (objectsToHarvest.contains(",")) {
            Statistics.register(Statistics.RELATIONSHIPS);
            ElementsAPIFeedRelationshipQuery relationshipFeedQuery = new ElementsAPIFeedRelationshipQuery();
            relationshipFeedQuery.setProcessAllPages(true);
            relationshipFeedQuery.setPerPage(relationshipsPerPage);
            if (modifiedSince != null) {
                relationshipFeedQuery.setModifiedSince(modifiedSince);
            }

            elementsAPI.execute(relationshipFeedQuery, new ElementsRelationshipHandler(elementsAPI, objectStore, objectsInRelationships).addObservers(relationshipObservers).addObserver(RelationshipStatisticsObserver.modifiedCounter()));

            // If we are only processing changes, we need to get the deleted relationships
            if (modifiedSince != null) {
                relationshipFeedQuery.setDeleted(true);
                elementsAPI.execute(relationshipFeedQuery, new ElementsRelationshipHandler(elementsAPI, objectStore, objectsInRelationships).addObservers(relationshipObservers).addObserver(RelationshipStatisticsObserver.deletedCounter()));
            }
        }

        for (ElementsFetchObserver observer : fetchObservers) {
            observer.postFetch();
        }

        if (!elementsDeltas) {
            for (String category : objectsToHarvest.split("\\s*,\\s*")) {
                ElementsObjectCategory eoCategory = ElementsObjectCategory.valueOf(category);
                if (eoCategory != null && eoCategory != ElementsObjectCategory.USER) {
                    // Delete the RDF objects not marked to be kept
                    rdfStore.pruneExcept(eoCategory, objectsInRelationships.get(eoCategory));
                }
            }
        }
    }

    private static class ObjectStatisticsObserver implements ElementsObjectObserver {
        private boolean countingDeleted = false;
        private ObjectStatisticsObserver(boolean type) { this.countingDeleted = type; }

        static ObjectStatisticsObserver modifiedCounter() { return new ObjectStatisticsObserver(false); }
        static ObjectStatisticsObserver deletedCounter()  { return new ObjectStatisticsObserver(true); }

        @Override
        public void observe(ElementsStoredObject object) {
            if (countingDeleted) {
                Statistics.deleted(object.getCategory().getPlural());
            } else {
                Statistics.modified(object.getCategory().getPlural());
            }
        }
    }

    private static class RelationshipStatisticsObserver implements ElementsRelationshipObserver {
        private boolean countingDeleted = false;
        private RelationshipStatisticsObserver(boolean type) { this.countingDeleted = type; }

        static RelationshipStatisticsObserver modifiedCounter() { return new RelationshipStatisticsObserver(false); }
        static RelationshipStatisticsObserver deletedCounter() { return new RelationshipStatisticsObserver(true); }

        @Override
        public void observe(ElementsStoredRelationship relationship, ElementsObjectsInRelationships objectsInRelationships) {
            if (countingDeleted) {
                Statistics.deleted(Statistics.RELATIONSHIPS);
            } else {
                Statistics.modified(Statistics.RELATIONSHIPS);
            }
        }
    }
}

