/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import java.util.HashMap;
import java.util.Map;

public class ElementsObjectCategory {
    private static final Map<String, ElementsObjectCategory> singularMap = new HashMap<String, ElementsObjectCategory>();
    private static final Map<String, ElementsObjectCategory> pluralMap   = new HashMap<String, ElementsObjectCategory>();

//    private static final List<ElementsObjectCategory> availableCategories = new ArrayList<ElementsObjectCategory>();

    public static final ElementsObjectCategory ACTIVITY          = new ElementsObjectCategory("activity", "activities");
    public static final ElementsObjectCategory EQUIPMENT         = new ElementsObjectCategory("equipment", "equipment");
    public static final ElementsObjectCategory GRANT             = new ElementsObjectCategory("grant", "grants");
    public static final ElementsObjectCategory ORG_STRUCTURE     = new ElementsObjectCategory("org-structure", "org-structures");
    public static final ElementsObjectCategory PROJECT           = new ElementsObjectCategory("project", "projects");
    public static final ElementsObjectCategory PUBLICATION       = new ElementsObjectCategory("publication", "publications");
    public static final ElementsObjectCategory USER              = new ElementsObjectCategory("user", "users");
    public static final ElementsObjectCategory TEACHING_ACTIVITY = new ElementsObjectCategory("teaching-activity", "teaching-activities");

    private final String singular;
    private final String plural;


    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    public static ElementsObjectCategory valueOf(String value) {
        if (singularMap.containsKey(value)) {
            return singularMap.get(value);
        }

        return pluralMap.get(value);
    }

    public static ElementsObjectCategory valueOfSingular(String value) {
        return singularMap.get(value);
    }

    public static ElementsObjectCategory valueOfPlural(String value) {
        return pluralMap.get(value);
    }

    private ElementsObjectCategory(final String singular, final String plural) {
        this.singular = singular;
        this.plural   = plural;

        if (singularMap.put(singular, this) != null  || pluralMap.put(plural, this) != null) {
            throw new IllegalStateException("Duplicate value given for singular / plural in Elements object category");
        }
    }
}
