/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.cache;

import uk.co.symplectic.vivoweb.harvester.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;

import java.io.File;

public class CachingService {
    public static enum Type {
        API,
        RDF
    }

    public void put(File file, String xml) {
        CachingServiceImpl.put("file_" + file.getAbsolutePath(), xml);
    }

    public void put(ElementsObjectInfo objectInfo, Type type, String xml) {
        String key = makeKey(objectInfo, type);
        if (key != null) {
            CachingServiceImpl.put(key, xml);
        }
    }

    public void put(ElementsRelationshipInfo relationshipInfo, Type type, String xml) {
        String key = makeKey(relationshipInfo, type);
        if (key != null) {
            CachingServiceImpl.put(key, xml);
        }
    }

    public String get(File file) {
        return CachingServiceImpl.get("file_" + file.getAbsolutePath());
    }

    public String get(ElementsObjectInfo objectInfo, Type type) {
        String key = makeKey(objectInfo, type);
        if (key != null) {
            return CachingServiceImpl.get(key);
        }
        return null;
    }

    public String get(ElementsRelationshipInfo relationshipInfo, Type type) {
        String key = makeKey(relationshipInfo, type);
        if (key != null) {
            return CachingServiceImpl.get(key);
        }
        return null;
    }

    public void remove(File file) {
        CachingServiceImpl.remove("file_" + file.getAbsolutePath());
    }

    public void remove(ElementsObjectInfo objectInfo, Type type) {
        String key = makeKey(objectInfo, type);
        if (key != null) {
            CachingServiceImpl.remove(key);
        }
    }

    public void remove(ElementsRelationshipInfo relationshipInfo, Type type) {
        String key = makeKey(relationshipInfo, type);
        if (key != null) {
            CachingServiceImpl.remove(key);
        }
    }

    private String makeKey(ElementsObjectInfo objectInfo, Type type) {
        switch (type) {
            case API:
                return "api_" + objectInfo.getCategory() + "_" + objectInfo.getId();

            case RDF:
                return "rdf_" + objectInfo.getCategory() + "_" + objectInfo.getId();
        }

        return null;
    }

    private String makeKey(ElementsRelationshipInfo relationshipInfo, Type type) {
        switch (type) {
            case API:
                return "api_relationship_" + relationshipInfo.getId();

            case RDF:
                return "rdf_relationship_" + relationshipInfo.getId();

        }

        return null;
    }
}
