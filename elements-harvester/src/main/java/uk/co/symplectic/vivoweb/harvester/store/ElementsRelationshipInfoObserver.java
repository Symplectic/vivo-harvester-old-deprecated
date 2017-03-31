/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.fetch.ElementsObjectId;
import uk.co.symplectic.vivoweb.harvester.model.ElementsRelationshipInfo;
import uk.co.symplectic.xml.*;

import javax.xml.stream.XMLStreamException;

public class ElementsRelationshipInfoObserver implements XMLStreamObserver {
    private ElementsRelationshipInfo relationshipInfo = null;

    public ElementsRelationshipInfo getRelationshipInfo() {
        if (relationshipInfo == null) {

        }

        return relationshipInfo;
    }

    @Override
    public void preProcessing() {
    }

    @Override
    public void postProcessing() {
    }

    @Override
    public void observeEvent(int eventType, XMLStreamReaderProxy readerProxy) throws XMLStreamException {
    }

    @Override
    public void observeElement(XMLElement element, String elementText) {
        if ("api".equals(element.getPrefix()) && "relationship".equals(element.getLocalName())) {
            relationshipInfo = ElementsRelationshipInfo.create(XMLUtils.getId(element.getAttributes()));
        } else if (relationshipInfo != null) {
            if ("api".equals(element.getPrefix()) && "object".equals(element.getLocalName())) {
                String category = null;
                String id = null;
                for (XMLAttribute attribute : element.getAttributes()) {
                    if ("category".equals(attribute.getName())) {
                        category = attribute.getValue();
                    } else if ("id".equals(attribute.getName())) {
                        id = attribute.getValue();
                    }
                }

                if (category != null && id != null) {
                    ElementsObjectCategory catObj = ElementsObjectCategory.valueOf(category);
                    if (catObj == ElementsObjectCategory.USER) {
                        relationshipInfo.setUserId(id);
                    }

                    relationshipInfo.addObjectId(new ElementsObjectId(catObj, id));
                }
            } else if ("api".equals(element.getPrefix()) && "is-visible".equals(element.getLocalName())) {
                relationshipInfo.setIsVisible(Boolean.parseBoolean(elementText));
            }
        }
    }
}
