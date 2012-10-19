/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import uk.co.symplectic.elements.api.ElementsObjectCategory;

import java.util.List;

public final class XMLUtils {
    private XMLUtils() {}

    public static ElementsObjectCategory getObjectCategory(List<XMLAttribute> attributeList) {
        XMLAttribute catAttr = getAttribute(attributeList, null, "category");
        if (catAttr != null) {
            return ElementsObjectCategory.valueOf(catAttr.getValue());
        }

        return null;
    }

    public static XMLAttribute getAttribute(List<XMLAttribute> attributeList, String attributePrefix, String attributeName) {
        for (XMLAttribute attribute : attributeList) {
            if (attributePrefix != null) {
                if (attributePrefix.equals(attribute.getPrefix()) && attributeName.equals(attribute.getName())) {
                    return attribute;
                }
            }

            if (attribute.getPrefix() == null && attributeName.equals(attribute.getName())) {
                return attribute;
            }
        }

        return null;
    }
}
