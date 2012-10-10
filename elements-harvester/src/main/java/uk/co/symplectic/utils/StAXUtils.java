/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.utils;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;
import java.io.Writer;
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

    public static String readerToString(XMLStreamReader xsr) throws XMLStreamException {
        StringWriter sw = new StringWriter();
        readerToWriter(xsr, sw);
        return sw.toString();
    }

    public static String readerToString(XMLStreamReader xsr, String rootElement, String docEncoding, String docVersion) throws XMLStreamException {
        StringWriter sw = new StringWriter();
        readerToWriter(xsr, sw, rootElement, docEncoding, docVersion);
        return sw.toString();
    }

    public static void fragmentToWriter(XMLStreamReader xsr, Writer writer) throws XMLStreamException {
        fragmentToWriter(xsr, writer, null, null, null);
    }

    public static void fragmentToWriter(XMLStreamReader xsr, Writer writer, String rootElement, String docEncoding, String docVersion) throws XMLStreamException {
        if (xsr instanceof XMLStreamFragmentReader) {
            readerToWriter(xsr, writer, rootElement, docEncoding, docVersion);
        } else {
            XMLStreamFragmentReader xsfr = new XMLStreamFragmentReader(xsr);
            readerToWriter(xsfr, writer, rootElement, docEncoding, docVersion);
        }
    }

    public static void readerToWriter(XMLStreamReader xsr, Writer writer) throws XMLStreamException {
        readerToWriter(xsr, writer, null, null, null);
    }

    public static void readerToWriter(XMLStreamReader xsr, Writer writer, String rootElement, String docEncoding, String docVersion) throws XMLStreamException {
        if (xsr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalStateException();
        }

        XMLStreamWriter xsw = StAXUtils.getXmlOutputFactory().createXMLStreamWriter(writer);

        if (rootElement != null) {
            xsw.writeStartDocument(docEncoding, docVersion);
            xsw.writeStartElement(rootElement);
            xsw.writeNamespace("", "http://www.symplectic.co.uk/vivo/");
            xsw.writeNamespace("api", "http://www.symplectic.co.uk/publications/api");
        }

        while (xsr.hasNext()) {
            switch (xsr.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xsr.getPrefix() != null && !StringUtils.isEmpty(xsr.getNamespaceURI())) {
                        xsw.writeStartElement(xsr.getPrefix(), xsr.getLocalName(), xsr.getNamespaceURI());
                    } else {
                        xsw.writeStartElement(xsr.getLocalName());
                    }

                    for (int nsIdx = 0; nsIdx < xsr.getNamespaceCount(); nsIdx++) {
                        xsw.writeNamespace(xsr.getNamespacePrefix(nsIdx), xsr.getNamespaceURI(nsIdx));
                    }

                    for (int attIdx = 0; attIdx < xsr.getAttributeCount(); attIdx++) {
                        if (xsr.getAttributeNamespace(attIdx) != null) {
                            xsw.writeAttribute(xsr.getAttributeNamespace(attIdx), xsr.getAttributeLocalName(attIdx), xsr.getAttributeValue(attIdx));
                        } else {
                            xsw.writeAttribute(xsr.getAttributeLocalName(attIdx), xsr.getAttributeValue(attIdx));
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    xsw.writeEndElement();
                    break;

                case XMLEvent.SPACE:
                case XMLEvent.CHARACTERS:
                    char[] characters = xsr.getTextCharacters();
                    if (characters != null) {
                        xsw.writeCharacters(characters, xsr.getTextStart(), xsr.getTextLength());
                    }
                    break;

                case XMLEvent.PROCESSING_INSTRUCTION:
                    xsw.writeProcessingInstruction(xsr.getPITarget(), xsr.getPIData());
                    break;

                case XMLEvent.CDATA:
                    String cdata = xsr.getText();
                    if (cdata != null) {
                        xsw.writeCData(cdata);
                    }
                    break;

                case XMLEvent.COMMENT:
                    String comment = xsr.getText();
                    if (comment != null) {
                        xsw.writeComment(comment);
                    }
                    break;

                case XMLEvent.ENTITY_REFERENCE:
                    String localName = xsr.getLocalName();
                    if (localName != null) {
                        xsw.writeEntityRef(localName);
                    }
                    break;
            }

            xsr.next();
        }

        if (rootElement != null) {
            xsw.writeEndElement();
            xsw.writeEndDocument();
        }
    }

    private static XMLInputFactory getXmlInputFactory() {
        if (xmlInputFactory == null) {
            synchronized (StAXUtils.class) {
                if (xmlInputFactory == null) {
                    xmlInputFactory = XMLInputFactory.newFactory();
                }
            }
        }

        return xmlInputFactory;
    }

    private static XMLOutputFactory getXmlOutputFactory() {
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
