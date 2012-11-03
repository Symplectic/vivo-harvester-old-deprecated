/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLElement {
    private XMLElement() {}

    QName name = null;  // prefix:localName
    String prefix = null;
    String localName = null;

    List<XMLAttribute> attributes = null;
    List<XMLNamespace> namespaces = null;

    public QName getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLocalName() {
        return localName;
    }

    public List<XMLAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    private void populateIdentity(XMLStreamReader xmlStreamReader) {
        name = xmlStreamReader.getName();
        prefix = xmlStreamReader.getPrefix();
        localName = xmlStreamReader.getLocalName();
    }

    private void populateAttributes(XMLStreamReader xmlStreamReader) {
        attributes = new ArrayList<XMLAttribute>();

        for (int attIdx = 0; attIdx < xmlStreamReader.getAttributeCount(); attIdx++) {
            if (xmlStreamReader.getAttributeNamespace(attIdx) != null) {
                attributes.add(new XMLAttribute(xmlStreamReader.getAttributeNamespace(attIdx), xmlStreamReader.getAttributeLocalName(attIdx), xmlStreamReader.getAttributeValue(attIdx)));
            } else {
                attributes.add(new XMLAttribute(xmlStreamReader.getAttributeLocalName(attIdx), xmlStreamReader.getAttributeValue(attIdx)));
            }
        }
    }

    private void populateNamespaces(XMLStreamReader xmlStreamReader) {
        namespaces = new ArrayList<XMLNamespace>();

        for (int nsIdx = 0; nsIdx < xmlStreamReader.getNamespaceCount(); nsIdx++) {
            namespaces.add(new XMLNamespace(xmlStreamReader.getNamespacePrefix(nsIdx), xmlStreamReader.getNamespaceURI(nsIdx)));
        }
    }

    public static XMLElement create(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        XMLElement element = null;

        if (xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            element = new XMLElement();
            element.populateIdentity(xmlStreamReader);
            element.populateAttributes(xmlStreamReader);
            element.populateNamespaces(xmlStreamReader);
        } else if (xmlStreamReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
            element = new XMLElement();
            element.populateIdentity(xmlStreamReader);
        } else {
            throw new XMLStreamException("Parser must be on START_ELEMENT or END_ELEMENT", xmlStreamReader.getLocation());
        }

        return element;
    }
}
