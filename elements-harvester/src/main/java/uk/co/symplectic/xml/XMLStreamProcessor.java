/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class XMLStreamProcessor {
    public void process(XMLStreamReader xsr, XMLStreamObserver... observers) throws XMLStreamException {
        if (xsr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalStateException();
        }

        XMLElement currentElement = null;
        StringBuilder elementTextBuilder = null;

        for (XMLStreamObserver observer : observers) {
            if (observer != null) {
                observer.preProcessing();
            }
        }

        while (xsr.hasNext()) {
            for (XMLStreamObserver observer : observers) {
                if (observer != null) {
                    observer.observeEvent(xsr.getEventType(), new XMLStreamReaderProxy(xsr));
                }
            }

            switch (xsr.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (currentElement != null) {
                        for (XMLStreamObserver observer : observers) {
                            if (observer != null) {
                                observer.observeElement(currentElement, null);
                            }
                        }
                    }

                    currentElement = XMLElement.create(xsr);
                    elementTextBuilder = new StringBuilder();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (currentElement != null) {
                        String elementText = elementTextBuilder != null ? elementTextBuilder.toString() : null;

                        for (XMLStreamObserver observer : observers) {
                            if (observer != null) {
                                observer.observeElement(currentElement, elementText);
                            }
                        }
                    }

                    currentElement = null;
                    elementTextBuilder = null;
                    break;

                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.ENTITY_REFERENCE:
                    if (elementTextBuilder != null) {
                        elementTextBuilder.append(xsr.getText());
                    }
                    break;

                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                case XMLStreamConstants.COMMENT:
                    // skip these
                    break;
            }

            xsr.next();
        }

        for (XMLStreamObserver observer : observers) {
            if (observer != null) {
                observer.postProcessing();
            }
        }
    }
}
