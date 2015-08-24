/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.repo.JenaConnect;

import java.io.File;
import java.util.concurrent.Future;

public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    //private TransferServiceConfig config = new TransferServiceConfig();

    public void transfer(JenaConnect outputStore, File rdfToRemove, File rdfToLoad) {
        TransferServiceImpl.transfer(outputStore, rdfToRemove, rdfToLoad);
    }

    public static void shutdown() {
        TransferServiceImpl.shutdown();
    }
}
