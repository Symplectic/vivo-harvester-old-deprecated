/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TranslationTask {
    private static final Logger log = LoggerFactory.getLogger(TranslationTask.class);
    Future<Boolean> translateResult;

    TranslationTask(Future<Boolean> translateResult) {
        this.translateResult = translateResult;
    }

    public boolean isDone() {
        return translateResult.isDone();
    }

    public boolean isCancelled() {
        return translateResult.isCancelled();
    }

    public Boolean checkResult() {
        return checkResult(50);
    }

    public Boolean checkResult(long timeout) {
        try {
            return translateResult.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            log.error("Execution problem whilst getting result", ee);
            throw new IllegalStateException(ee);
        } catch (InterruptedException ie) {
            log.error("Interrupted execution whilst getting result", ie);
            throw new IllegalStateException(ie);
        } catch (TimeoutException te) {
            log.trace("Timeout attempting to check for result", te);
            return null;
        }
    }

    public Boolean waitForResult() {
        try {
            return translateResult.get();
        } catch (ExecutionException ee) {
            log.error("Execution problem whilst getting result", ee);
            throw new IllegalStateException(ee);
        } catch (InterruptedException ie) {
            log.error("Interrupted execution whilst getting result", ie);
            throw new IllegalStateException(ie);
        }
    }

    public Future<Boolean> future() {
        return translateResult;
    }
}
