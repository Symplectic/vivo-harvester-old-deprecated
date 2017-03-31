/*******************************************************************************
 * Copyright (c) 2012 Symplectic. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import javax.xml.transform.Source;
import java.io.IOException;

public interface TranslationSource {
    public Source source();

    public void release() throws IOException;

    public String description();
}
