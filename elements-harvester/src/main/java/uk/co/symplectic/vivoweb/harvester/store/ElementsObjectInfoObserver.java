/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.store;

import uk.co.symplectic.elements.api.ElementsObjectCategory;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsObjectInfo;
import uk.co.symplectic.vivoweb.harvester.fetch.model.ElementsUserInfo;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLElement;
import uk.co.symplectic.xml.XMLStreamObserver;
import uk.co.symplectic.xml.XMLStreamReaderProxy;
import uk.co.symplectic.xml.XMLUtils;

import javax.xml.stream.XMLStreamException;

public class ElementsObjectInfoObserver implements XMLStreamObserver {
    private ElementsObjectInfo objectInfo = null;

    public ElementsObjectInfo getObjectInfo() {
        if (objectInfo == null) {

        }

        return objectInfo;
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
        if ("api".equals(element.getPrefix()) && "object".equals(element.getLocalName())) {
            objectInfo = ElementsObjectInfo.create(XMLUtils.getObjectCategory(element.getAttributes()), XMLUtils.getId(element.getAttributes()));
            if (objectInfo.getCategory() == ElementsObjectCategory.USER) {
                ((ElementsUserInfo)objectInfo).setUsername(XMLUtils.getUsername(element.getAttributes()));
            }
        } else {
            if (objectInfo.getCategory() == ElementsObjectCategory.USER) {
                ElementsUserInfo userInfo = (ElementsUserInfo)objectInfo;
                if ("api".equals(element.getPrefix()) && "photo".equals(element.getLocalName())) {
                    if (element.getAttributes() != null) {
                        for (XMLAttribute attribute : element.getAttributes()) {
                            if ("href".equals(attribute.getName())) {
                                userInfo.setPhotoUrl(attribute.getValue());
                            }
                        }
                    }
                } else if ("api".equals(element.getPrefix()) && "is-current-staff".equals(element.getLocalName())) {
                    userInfo.setIsCurrentStaff(Boolean.parseBoolean(elementText));
                }
            }
        }
    }
}
