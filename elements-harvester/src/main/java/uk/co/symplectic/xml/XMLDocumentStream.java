/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.co.symplectic.utils.StAXUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * XMLDocument holder for XML streams
 */
public class XMLDocumentStream implements XMLDocument {
    XMLStreamReader streamReader = null;
    boolean streamIsValid = true;

    XMLDocumentString xmlAsString = null;

    public XMLDocumentStream(XMLStreamReader streamReader) {
        this.streamReader = streamReader;
    }

    @Override
    public Document getDocument() throws ParserConfigurationException, SAXException {
        if (xmlAsString != null) {
            return xmlAsString.getDocument();
        }

        renderStreamToString();

        if (xmlAsString == null) {
            throw new IllegalStateException();
        }

        return xmlAsString.getDocument();
    }

    @Override
    public XMLStreamReader getStreamReader() throws XMLStreamException {
        if (streamReader != null) {
            if (streamIsValid) {
                return streamReader;
            }
        }

        if (xmlAsString != null) {
            return xmlAsString.getStreamReader();
        }

        throw new IllegalStateException();
    }

    @Override
    public String getString() {
        if (xmlAsString != null) {
            return xmlAsString.getString();
        }

        renderStreamToString();

        if (xmlAsString == null) {
            throw new IllegalStateException();
        }

        return xmlAsString.getString();
    }

    private synchronized void renderStreamToString() {
        if (xmlAsString == null) {
            try {
                xmlAsString = new XMLDocumentString(StAXUtils.readerToString(streamReader));
            } catch (XMLStreamException xse) {
                throw new IllegalStateException(xse);
            } finally {
                streamReader = null;
                streamIsValid = false;
            }
        }
    }
}
