/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLStreamFragmentReader;

import javax.xml.stream.XMLStreamException;
import java.util.List;

public interface ElementsAPIFeedRelationshipStreamHandler extends ElementsAPIFeedRelationshipHandler {
    public void handle(List<XMLAttribute> attributes, XMLStreamFragmentReader relationshipReader, String docEncoding, String docVersion) throws XMLStreamException;
}
