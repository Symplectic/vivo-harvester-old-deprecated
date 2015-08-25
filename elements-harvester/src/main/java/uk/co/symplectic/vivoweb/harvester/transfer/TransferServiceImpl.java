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
import uk.co.symplectic.utils.ExecutorServiceUtils;
import uk.co.symplectic.vivoweb.harvester.jena.JenaWrapper;

import java.io.File;
import java.util.concurrent.Callable;

final public class TransferServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private static final ExecutorServiceUtils.ExecutorServiceWrapper wrapper = ExecutorServiceUtils.newFixedThreadPool("TransferService");

    private static final Transfer transferCmd = new Transfer();

    private TransferServiceImpl() { }

    static void transfer(JenaWrapper outputStore, File transferredRdf, File translatedRdf) {
        wrapper.submit(new TransferHandler(outputStore, transferredRdf, translatedRdf));
    }

    static class TransferHandler implements Callable<Boolean> {
        private JenaWrapper outputStore;

        private File transferredRdf;
        private File translatedRdf;

        TransferHandler(JenaWrapper outputStore, File transferredRdf, File translatedRdf) {
            this.outputStore = outputStore;
            this.transferredRdf = transferredRdf;
            this.translatedRdf = translatedRdf;
        }

        @Override
        public Boolean call() throws Exception {
            return transferCmd.transfer(outputStore, transferredRdf, translatedRdf);
        }
    }


    static void shutdown() {
        wrapper.shutdown();
    }
}
