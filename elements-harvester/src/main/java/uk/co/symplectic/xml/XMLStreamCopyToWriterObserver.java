/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMLStreamCopyToWriterObserver implements XMLStreamObserver {
    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(XMLStreamCopyToWriterObserver.class);

    private Writer writer = null;
    private XMLStreamWriter xsw = null;
    private String rootElement = null;
    private String docEncoding = null;
    private String docVersion = null;

    private List<XMLNamespace> namespaces = null;

    public XMLStreamCopyToWriterObserver(Writer writer) {
        this.writer = writer;
    }

    public XMLStreamCopyToWriterObserver(Writer writer, String rootElement, String docEncoding, String docVersion, List<XMLNamespace> namespaces) {
        this.writer = writer;
        this.rootElement = rootElement;
        this.docEncoding = docEncoding;
        this.docVersion = docVersion;
        if (namespaces != null) {
            this.namespaces = new ArrayList<XMLNamespace>(namespaces.size());
            for (XMLNamespace namespace : namespaces) {
                if (namespace != null) {
                    this.namespaces.add(new XMLNamespace(namespace.getPrefix(), namespace.getURI()));
                }
            }
        }
    }

    public XMLStreamCopyToWriterObserver(XMLStreamWriter xmlStreamWriter) {
        this.xsw = xmlStreamWriter;
    }

    public XMLStreamCopyToWriterObserver(XMLStreamWriter xmlStreamWriter, String rootElement, String docEncoding, String docVersion) {
        this.xsw = xmlStreamWriter;
        this.rootElement = rootElement;
        this.docEncoding = docEncoding;
        this.docVersion = docVersion;
    }

    @Override
    public void preProcessing() {
        try {
            if (writer != null && xsw == null) {
                xsw = StAXUtils.getXMLOutputFactory().createXMLStreamWriter(writer);
            }
        } catch (XMLStreamException xse) {
            log.error("Unable to create XMLStreamWriter", xse);
        }

        try {
            if (xsw != null && rootElement != null) {
                xsw.writeStartDocument(docEncoding, docVersion);
                xsw.writeStartElement(rootElement);
                if (namespaces != null) {
                    for (XMLNamespace namespace : namespaces) {
                        if (namespace != null) {
                            xsw.writeNamespace(namespace.getPrefix(), namespace.getURI());
                        }
                    }
                }
            }
        } catch (XMLStreamException xse) {
            log.error("Unable to write start of document", xse);
        }
    }

    @Override
    public void postProcessing() {
        try {
            if (rootElement != null) {
                xsw.writeEndElement();
                xsw.writeEndDocument();
            }
        } catch (XMLStreamException xse) {
            log.error("Unable to write end of document", xse);
        }
    }

    @Override
    public void observeEvent(int eventType, XMLStreamReaderProxy readerProxy) throws XMLStreamException {
        switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
                if (readerProxy.getPrefix() != null && !StringUtils.isEmpty(readerProxy.getNamespaceURI())) {
                    xsw.writeStartElement(readerProxy.getPrefix(), readerProxy.getLocalName(), readerProxy.getNamespaceURI());
                } else {
                    xsw.writeStartElement(readerProxy.getLocalName());
                }

                for (int nsIdx = 0; nsIdx < readerProxy.getNamespaceCount(); nsIdx++) {
                    xsw.writeNamespace(readerProxy.getNamespacePrefix(nsIdx), readerProxy.getNamespaceURI(nsIdx));
                }

                for (int attIdx = 0; attIdx < readerProxy.getAttributeCount(); attIdx++) {
                    if (readerProxy.getAttributeNamespace(attIdx) != null) {
                        xsw.writeAttribute(readerProxy.getAttributeNamespace(attIdx), readerProxy.getAttributeLocalName(attIdx), readerProxy.getAttributeValue(attIdx));
                    } else {
                        xsw.writeAttribute(readerProxy.getAttributeLocalName(attIdx), readerProxy.getAttributeValue(attIdx));
                    }
                }
                break;

            case XMLStreamConstants.END_ELEMENT:
                xsw.writeEndElement();
                break;

            case XMLEvent.SPACE:
            case XMLEvent.CHARACTERS:
                char[] characters = readerProxy.getTextCharacters();
                if (characters != null) {
                    xsw.writeCharacters(characters, readerProxy.getTextStart(), readerProxy.getTextLength());
                }
                break;

            case XMLEvent.PROCESSING_INSTRUCTION:
                xsw.writeProcessingInstruction(readerProxy.getPITarget(), readerProxy.getPIData());
                break;

            case XMLEvent.CDATA:
                String cdata = readerProxy.getText();
                if (cdata != null) {
                    xsw.writeCData(cdata);
                }
                break;

            case XMLEvent.COMMENT:
                String comment = readerProxy.getText();
                if (comment != null) {
                    xsw.writeComment(comment);
                }
                break;

            case XMLEvent.ENTITY_REFERENCE:
                String localName = readerProxy.getLocalName();
                if (localName != null) {
                    xsw.writeEntityRef(localName);
                }
                break;
        }
    }

    @Override
    public void observeElement(XMLElement element, String elementText) {
    }
}
