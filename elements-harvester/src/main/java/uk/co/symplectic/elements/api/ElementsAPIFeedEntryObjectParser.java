/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import uk.co.symplectic.utils.StAXUtils;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

class ElementsAPIFeedEntryObjectParser implements ElementsFeedEntryParser {
    private ElementsAPIFeedObjectStreamHandler streamHandler;
    private boolean entryHasFullDetails;

    private String encoding = null;
    private String version = null;

    ElementsAPIFeedEntryObjectParser(ElementsAPIFeedObjectHandler handler, boolean hasFullDetails) {
        if (handler instanceof  ElementsAPIFeedObjectStreamHandler) {
            this.streamHandler = (ElementsAPIFeedObjectStreamHandler)handler;
        }

        this.entryHasFullDetails = hasFullDetails;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void parseEntry(XMLStreamFragmentReader entryReader) throws XMLStreamException {
        if (!entryReader.isStartElement() || !"entry".equals(entryReader.getLocalName())) {
            throw new XMLStreamException("Stream must be position at start tag, named 'entry'");
        }

        while (entryReader.hasNext()) {
            String prefix;
            String name;
            switch (entryReader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    prefix = entryReader.getPrefix();
                    name = entryReader.getLocalName();
                    if ("api".equals(prefix) && "object".equals(name)) {
                        List<XMLAttribute> attributes = StAXUtils.getAttributes(entryReader);

                        if (streamHandler != null) {
                            streamHandler.handle(attributes, new XMLStreamFragmentReader(entryReader), encoding, version);
                        } else if (entryHasFullDetails) {
                        } else {
                            // Extract URL for details
                        }
                    }
                    break;
            }

            entryReader.next();
        }
    }
}
