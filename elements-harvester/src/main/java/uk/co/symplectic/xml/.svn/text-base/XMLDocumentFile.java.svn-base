/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XMLDocumentFile implements XMLDocument {
    private File xmlFile;

    public XMLDocumentFile(File xmlFile) {
        this.xmlFile = xmlFile;

    }

    @Override
    public Document getDocument() throws ParserConfigurationException, SAXException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(xmlFile);
        } catch (IOException ioe) {
            return null;
        }
    }

    @Override
    public XMLStreamReader getStreamReader() throws XMLStreamException {
        try {
            XMLInputFactory xmlInputFactory = StAXUtils.getXMLInputFactory();
            return xmlInputFactory.createXMLStreamReader(new FileInputStream(xmlFile));
        } catch (FileNotFoundException fileNotFoundException) {
            return null;
        }
    }

    @Override
    public String getString() {
        try {
            return FileUtils.readFileToString(xmlFile);
        } catch (IOException e) {
            return null;
        }
    }
}
