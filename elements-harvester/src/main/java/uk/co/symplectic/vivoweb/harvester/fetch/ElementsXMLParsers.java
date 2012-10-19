/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.axiom.om.util.StAXUtils;
import uk.co.symplectic.elements.api.ElementsObjectCategory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class ElementsXMLParsers {
    public static ElementsUserInfo parseUserInfo(File inputFile) {
        ElementsUserInfo userInfo = new ElementsUserInfo();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(inputFile));

            XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
            XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);

            while (xmlReader.hasNext()) {
                String prefix;
                String name;
                switch (xmlReader.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        prefix = xmlReader.getPrefix();
                        name = xmlReader.getLocalName();
                        if ("api".equals(prefix) && "is-current-staff".equals(name)) {
                            userInfo.setIsCurrentStaff(Boolean.parseBoolean(xmlReader.getElementText()));
                        } else if ("api".equals(prefix) && "photo".equals(name)) {
                            for (int attIdx = 0; attIdx < xmlReader.getAttributeCount(); attIdx++) {
                                if ("href".equals(xmlReader.getAttributeLocalName(attIdx))) {
                                    userInfo.setPhotoUrl(xmlReader.getAttributeValue(attIdx));
                                }
                            }
                        }
                        break;
                }

                if (xmlReader.hasNext()) {
                    xmlReader.next();
                }
            }

        } catch (FileNotFoundException fnfe) {
        } catch (XMLStreamException e) {
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
        }

        return userInfo;
    }

    public static ElementsRelationshipInfo parseRelatonshipInfo(File inputFile) {
        ElementsRelationshipInfo relationshipInfo = new ElementsRelationshipInfo();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(inputFile));

            XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
            XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);

            while (xmlReader.hasNext()) {
                String prefix;
                String name;
                switch (xmlReader.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        prefix = xmlReader.getPrefix();
                        name = xmlReader.getLocalName();
                        if ("api".equals(prefix) && "is-visible".equals(name)) {
                            relationshipInfo.setIsVisible(Boolean.parseBoolean(xmlReader.getElementText()));
                        } else if ("api".equals(prefix) && "object".equals(name)) {
                            String category = null;
                            String id = null;
                            for (int attIdx = 0; attIdx < xmlReader.getAttributeCount(); attIdx++) {
                                if ("category".equals(xmlReader.getAttributeLocalName(attIdx))) {
                                    category = xmlReader.getAttributeValue(attIdx);
                                } else if ("id".equals(xmlReader.getAttributeLocalName(attIdx))) {
                                    id = xmlReader.getAttributeValue(attIdx);
                                }
                            }

                            if (category != null && id != null) {
                                ElementsObjectCategory catObj = ElementsObjectCategory.valueOf(category);
                                if (catObj == ElementsObjectCategory.USER) {
                                    relationshipInfo.setUserId(id);
                                }

                                relationshipInfo.addObjectId(new ElementsObjectId(catObj, id));
                            }
                        }
                        break;
                }

                if (xmlReader.hasNext()) {
                    xmlReader.next();
                }
            }

        } catch (FileNotFoundException fnfe) {
        } catch (XMLStreamException e) {
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
        }

        return relationshipInfo;
    }
}
