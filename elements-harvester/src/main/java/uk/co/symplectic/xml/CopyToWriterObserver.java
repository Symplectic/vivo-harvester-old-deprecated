/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;

public class CopyToWriterObserver implements XMLStreamObserver {
    private Writer writer = null;
    private XMLStreamWriter xsw = null;
    private String rootElement = null;
    private String docEncoding = null;
    private String docVersion = null;

    public CopyToWriterObserver(Writer writer) {
        this.writer = writer;
    }

    public CopyToWriterObserver(Writer writer, String rootElement, String docEncoding, String docVersion) {
        this.writer = writer;
        this.rootElement = rootElement;
        this.docEncoding = docEncoding;
        this.docVersion = docVersion;
    }

    public CopyToWriterObserver(XMLStreamWriter xmlStreamWriter) {
        this.xsw = xmlStreamWriter;
    }

    public CopyToWriterObserver(XMLStreamWriter xmlStreamWriter, String rootElement, String docEncoding, String docVersion) {
        this.xsw = xmlStreamWriter;
        this.rootElement = rootElement;
        this.docEncoding = docEncoding;
        this.docVersion = docVersion;
    }

    @Override
    public void preProcessing() throws XMLStreamException {
        if (writer != null && xsw == null) {
            xsw = StAXUtils.getXMLOutputFactory().createXMLStreamWriter(writer);
        }

        if (rootElement != null) {
            xsw.writeStartDocument(docEncoding, docVersion);
            xsw.writeStartElement(rootElement);
            xsw.writeNamespace("", "http://www.symplectic.co.uk/vivo/");
            xsw.writeNamespace("api", "http://www.symplectic.co.uk/publications/api");
        }
    }

    @Override
    public void postProcessing() throws XMLStreamException {
        if (rootElement != null) {
            xsw.writeEndElement();
            xsw.writeEndDocument();
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
    public void observeStartElement(XMLElement element) {
    }

    @Override
    public void observeEndElement(XMLElement element, String elementText) {
    }
}
