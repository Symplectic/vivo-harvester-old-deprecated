/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XMLDocumentString implements XMLDocument {
    private String xml;

    public XMLDocumentString(String xml) {
        this.xml = xml;
    }

    @Override
    public Document getDocument() throws ParserConfigurationException, SAXException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(xml);
        } catch (IOException ioe) {
            return null;
        }
    }

    @Override
    public XMLStreamReader getStreamReader() throws XMLStreamException {
        XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
        return xmlInputFactory.createXMLStreamReader(new StringInputStream(xml));
    }

    @Override
    public String getString() {
        return xml;
    }
}
