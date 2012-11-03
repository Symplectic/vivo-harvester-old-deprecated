/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.stream.XMLStreamException;

public interface XMLStreamObserver {
    // Notify that stream processing is about to start
    public void preProcessing() throws XMLStreamException;

    // Notify that stream processing has finished
    public void postProcessing() throws XMLStreamException;

    // Observe each stream event in detail
    // Note that the reader proxy prevents use of methods that change the stream state
    public void observeEvent(int eventType, XMLStreamReaderProxy readerProxy) throws XMLStreamException;

    // Observer a start element, in simple form
    public void observeStartElement(XMLElement element);

    // Observer an end element, in simple form, with any text of the element
    public void observeEndElement(XMLElement element, String elementText);
}
