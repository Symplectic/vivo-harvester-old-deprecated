/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public final class ExecutorServiceUtils {
    private ExecutorServiceUtils() {
    }

    public static long getCompletedTaskCount(ExecutorService service) {
        if (service instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)service).getCompletedTaskCount();
        }

        return -1;
    }

    public static long getTaskCount(ExecutorService service) {
        if (service instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)service).getTaskCount();
        }

        return -1;
    }
}
