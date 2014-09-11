/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.NoSuchElementException;

/**
 * Wrapper to an XMLStreamReader.
 *
 * When initialized with an XMLStreamReader which is already at a START_ELEMENT,
 * then this wrapper will process up to the corresponding END_ELEMENT.
 *
 * On reaching the END_ELEMENT, hasNext() will return false, and next()/nextTag() will behave
 * as if the end of the stream has been reached.
 *
 * The underlying XMLStreamReader is unaffected, and may be used to continue reading the document.
 *
 * This class is useful when you wish to parse a set fragment within a larger document, as you
 * no longer need to worry about keeping track of where the fragment ends.
 */
public final class XMLStreamFragmentReader implements XMLStreamReader {
    private XMLStreamReader reader;

    private String elementName;
    private String elementPrefix;
    private int currentDepth = 1;

    private boolean hasNext = true;

    public XMLStreamFragmentReader(XMLStreamReader reader) {
        this.reader = reader;

        if (!reader.isStartElement()) {
            throw new IllegalStateException();
        }

        elementName   = reader.getLocalName();
        elementPrefix = reader.getPrefix();
    }

    public XMLStreamReader getWrappedXMLStreamReader() {
        return reader;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return reader.getProperty(name);
    }

    @Override
    public int next() throws XMLStreamException {
        if (hasNext) {
            return processTags(reader.next());
        }

        throw new NoSuchElementException();
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        reader.require(type, namespaceURI, localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return reader.getElementText();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        if (hasNext) {
            return processTags(reader.nextTag());
        }

        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        if (hasNext) {
            if (currentDepth == 0 && reader.isEndElement()) {
                hasNext = false;
            } else if (!reader.hasNext()) {
                throw new XMLStreamException("Malformed XML stream");
            }
        }

        return hasNext;
    }

    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return reader.getNamespaceURI();
    }

    @Override
    public boolean isStartElement() {
        return reader.isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return reader.isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return reader.isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return reader.isCharacters();
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return reader.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    @Override
    public QName getAttributeName(int index) {
        return reader.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
        return reader.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(int index) {
        return reader.getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        return reader.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
        return reader.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(int index) {
        return reader.getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return reader.isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        return reader.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI(int index) {
        return reader.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }

    @Override
    public int getEventType() {
        return reader.getEventType();
    }

    @Override
    public String getText() {
        return reader.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return reader.getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public int getTextStart() {
        return reader.getTextStart();
    }

    @Override
    public int getTextLength() {
        return reader.getTextLength();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public boolean hasText() {
        return reader.hasText();
    }

    @Override
    public Location getLocation() {
        return reader.getLocation();
    }

    @Override
    public QName getName() {
        return reader.getName();
    }

    @Override
    public String getLocalName() {
        return reader.getLocalName();
    }

    @Override
    public boolean hasName() {
        return reader.hasName();
    }

    @Override
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return reader.getPrefix();
    }

    @Override
    public String getVersion() {
        return reader.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    @Override
    public String getPITarget() {
        return reader.getPITarget();
    }

    @Override
    public String getPIData() {
        return reader.getPIData();
    }

    private int processTags(int retValue) throws XMLStreamException {
        if (reader.isStartElement()) {
            currentDepth++;
        } else if (reader.isEndElement()) {
            currentDepth--;
            if (currentDepth == 0) {
                if (elementPrefix == null && reader.getPrefix() != null) {
                    throw new XMLStreamException();
                }

                if (elementPrefix != null && !elementPrefix.equals(reader.getPrefix())) {
                    throw new XMLStreamException();
                }

                if (!elementName.equals(reader.getLocalName())) {
                    throw new XMLStreamException();
                }
            }
        }

        return retValue;
    }
}
