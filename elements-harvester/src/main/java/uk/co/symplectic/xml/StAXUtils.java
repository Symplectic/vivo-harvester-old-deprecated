/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class StAXUtils {
    private static XMLInputFactory xmlInputFactory = null;
    private static XMLOutputFactory xmlOutputFactory = null;

    public static List<XMLAttribute> getAttributes(XMLStreamReader xsr) {
        if (xsr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalStateException();
        }

        List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

        for (int attIdx = 0; attIdx < xsr.getAttributeCount(); attIdx++) {
            if (xsr.getAttributeNamespace(attIdx) != null) {
                attributes.add(new XMLAttribute(xsr.getAttributeNamespace(attIdx), xsr.getAttributeLocalName(attIdx), xsr.getAttributeValue(attIdx)));
            } else {
                attributes.add(new XMLAttribute(xsr.getAttributeLocalName(attIdx), xsr.getAttributeValue(attIdx)));
            }
        }

        return attributes;
    }

    public static XMLInputFactory getXMLInputFactory() {
        if (xmlInputFactory == null) {
            synchronized (StAXUtils.class) {
                if (xmlInputFactory == null) {
                    xmlInputFactory = XMLInputFactory.newFactory();
                }
            }
        }

        return xmlInputFactory;
    }

    public static XMLOutputFactory getXMLOutputFactory() {
        if (xmlOutputFactory == null) {
            synchronized (StAXUtils.class) {
                if (xmlOutputFactory == null) {
                    xmlOutputFactory = XMLOutputFactory.newFactory();
                }
            }
        }

        return xmlOutputFactory;
    }
}
