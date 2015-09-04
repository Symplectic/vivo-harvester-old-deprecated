/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.utils;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public final class ExecutorServiceUtils {
    private static final Map<String, Integer> maxProcessorsPerPool = new HashMap<String, Integer>();

    private ExecutorServiceUtils() {
    }

    public static void setMaxProcessorsForPool(String poolName, int size) {
        maxProcessorsPerPool.put(poolName.toLowerCase(), size);
    }

    public static long getCompletedTaskCount(ExecutorService service) {
        if (service instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)service).getCompletedTaskCount();
        }

        return -1;
    }

    public static long getQueueSize(ExecutorService service) {
        if (service instanceof ThreadPoolExecutor){
            ThreadPoolExecutor tpe = (ThreadPoolExecutor)service;
//            return tpe.getTaskCount() - tpe.getCompletedTaskCount()
            return tpe.getQueue().size();
        }

        return -1;
    }

    public static long getTaskCount(ExecutorService service) {
        if (service instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)service).getTaskCount();
        }

        return -1;
    }

    public static ExecutorServiceWrapper newFixedThreadPool(String poolName) {
        int poolSize = Runtime.getRuntime().availableProcessors();

        int maxPoolSize = -1;
        if (!StringUtils.isEmpty(poolName)) {
            Integer maxPoolSizeObject = maxProcessorsPerPool.get(poolName.toLowerCase());
            if (maxPoolSizeObject != null) {
                maxPoolSize = maxPoolSizeObject;
            }
        }

        if (maxPoolSize > 0 && maxPoolSize < poolSize) {
            poolSize = maxPoolSize;
        }

        /**
         * Uses a ThreadFactory to create Daemon threads.
         *
         * By doing so, when the program exits the main() method - and regardless of whether
         * System.exit() has been called - Java will not treat the active threads as blocking
         * the termination.
         *
         * This means that the shutdown hook (which is added below) will be run, causing the
         * graceful termination of the ExecutorService, and the running tasks.
         *
         * Without Daemon threads, the program will not terminate, nor will the shutdown hooks be called
         * unless System.exit is called explicitly.
         */
        ExecutorService service = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });

        // newFixedThreadPool never returns null, so we don't need to check

        ExecutorServiceWrapper wrapper = new ExecutorServiceWrapper(service, poolName);

        /**
         * Shutdown hook to gracefully terminate the ExecutorService. Gives any existing tasks a chance to
         * complete before forcing termination.
         */
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(wrapper));

        return wrapper;
    }

    static void shutdown(ExecutorServiceWrapper wrapper) {
        ExecutorService service = wrapper.service;
        ExecutorShutdownParams params = wrapper.shutdownParams;

        if (params == null) {
            params = new ExecutorShutdownParams();
        }

        service.shutdown();
        try {
            int stalledCount = 0;
            long lastCompletedTasks = 0;
            while (!service.awaitTermination(params.getShutdownWaitCycleInSecs(), TimeUnit.SECONDS)) {
                long completedTasks = ExecutorServiceUtils.getCompletedTaskCount(service);
                if (completedTasks > -1 && completedTasks == lastCompletedTasks) {
                    System.err.println("Waiting for shutdown of translation service. Completed " + completedTasks + " tasks out of " + ExecutorServiceUtils.getTaskCount(service));
                    stalledCount++;

                    if (stalledCount > params.getMaxStalledShutdownCycles()) {
                        System.err.println("Waited " + params.getShutdownStalledWaitTimeInSecs() + " seconds without progress. Abandoning.");
                        service.shutdownNow();
                        if (!service.awaitTermination(params.getShutdownWaitCycleInSecs(), TimeUnit.SECONDS)) {
                            break;
                        }
                    }
                } else {
                    stalledCount = 0;
                }
                lastCompletedTasks = completedTasks;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("[" + wrapper.poolName + "] Queue had max size of: " + wrapper.maxQueueCount);
        }
    }

    private static class ShutdownHook extends Thread {
        private ExecutorServiceWrapper wrapper;

        ShutdownHook(ExecutorServiceWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void run() {
            wrapper.shutdown();
        }
    }

    public static class ExecutorServiceWrapper {
        private ExecutorService service;
        private ExecutorShutdownParams shutdownParams = new ExecutorShutdownParams();
        private boolean shutdownCalled = false;
        private long maxQueueCount = -1;
        private String poolName = null;

        ExecutorServiceWrapper(ExecutorService service, String poolName) {
            this.service = service;
            this.poolName = poolName;
        }

        public <T> Future<T> submit(Callable<T> task) {
            try {
                return service.submit(task);
            } finally {
                maxQueueCount = Math.max(maxQueueCount, ExecutorServiceUtils.getQueueSize(service));
            }
        }

        public ExecutorShutdownParams shutdownParams() {
            return shutdownParams;
        }

        public long getQueueSize() {
            return ExecutorServiceUtils.getQueueSize(service);
        }

        public void shutdown() {
            if (!shutdownCalled) {
                shutdownCalled = true;
                ExecutorServiceUtils.shutdown(this);
            }
        }
    }

    public static class ExecutorShutdownParams {
        private int shutdownStalledWaitTimeInSecs = 300; /* 5 minutes */
        private int shutdownWaitCycleInSecs = 30;
        private int maxStalledShutdownCycles = shutdownStalledWaitTimeInSecs / shutdownWaitCycleInSecs;

        public ExecutorShutdownParams() {}

        public ExecutorShutdownParams(int shutdownStalledWaitTimeInSecs) {
            this.shutdownStalledWaitTimeInSecs = shutdownStalledWaitTimeInSecs;
            maxStalledShutdownCycles = shutdownStalledWaitTimeInSecs / shutdownWaitCycleInSecs;
        }

        public ExecutorShutdownParams(int shutdownStalledWaitTimeInSecs, int shutdownWaitCycleInSecs) {
            this.shutdownStalledWaitTimeInSecs = shutdownStalledWaitTimeInSecs;
            this.shutdownWaitCycleInSecs = shutdownWaitCycleInSecs;
            maxStalledShutdownCycles = shutdownStalledWaitTimeInSecs / shutdownWaitCycleInSecs;
        }

        public int getMaxStalledShutdownCycles() {
            return maxStalledShutdownCycles;
        }

        public int getShutdownStalledWaitTimeInSecs() {
            return shutdownStalledWaitTimeInSecs;
        }

        public int getShutdownWaitCycleInSecs() {
            return shutdownWaitCycleInSecs;
        }

        public void setShutdownStalledWaitTimeInSecs(int secs) {
            shutdownStalledWaitTimeInSecs = secs;
            maxStalledShutdownCycles = shutdownStalledWaitTimeInSecs / shutdownWaitCycleInSecs;
        }

        public void setShutdownWaitCycleInSecs(int secs) {
            shutdownWaitCycleInSecs = secs;
            maxStalledShutdownCycles = shutdownStalledWaitTimeInSecs / shutdownWaitCycleInSecs;
        }
    }
}
