/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.xml;

import javax.xml.stream.XMLStreamException;

public interface XMLStreamObserver {
    /**
     * Notify that stream processing is about to start
     */
    public void preProcessing();

    /**
     * Notify that stream processing has finished
     */
    public void postProcessing();

    /**
     * Observe each stream event in detail
     * Note that the reader proxy prevents use of methods that change the stream state
     */
    public void observeEvent(int eventType, XMLStreamReaderProxy readerProxy) throws XMLStreamException;

    /**
     * Simple observer of an element.
     * This is a lazy observer - it will be called only after it is determined what the element contains
     * - if there are child elements:
     *      eg. <element><child /></element>
     *      it will be called at the start of child element, with elementText = null
     * - if there are no child elements:
     *      eg. <element>elementText</element>
     *      it will be called at the end element, along with the elementText
     *      (an empty string denotes an empty element
     *
     * In all cases, the attributes of the element will be passed
     */
    public void observeElement(XMLElement element, String elementText);
}
