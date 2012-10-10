/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.junit.Test;

import javax.xml.transform.Templates;
import java.io.*;

public class TranslationServiceTest {
    @Test
    public void testTranslate() throws Exception {
/**
 * Disabled until test data is created
 */
/*
        TranslationService service = new TranslationService();

        InputStream xslStream = getClass().getResourceAsStream("/symplectic-to-vivo.datamap.xsl");
        Templates templates = service.compileSource(xslStream);

        File rawRecordsDir = new File(getClass().getResource("/raw-records").toURI());

        if (rawRecordsDir.isDirectory()) {
            while (true) {
                for (File rawRecord : rawRecordsDir.listFiles()) {
                    service.translate(new BufferedInputStream(new FileInputStream(rawRecord)), new ByteArrayOutputStream(), templates);
                }
            }
        }
*/
    }
}
