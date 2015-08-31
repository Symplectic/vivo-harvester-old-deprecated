/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Statistics {
    private static Map<String, Counts> counts = new HashMap<String, Counts>();

    public static final String RELATIONSHIPS = "relationships";

    private Statistics() {}

    public static void print(PrintStream out) {
        if (out != null && counts.size() > 0) {
            String[] report = new String[counts.size()];
            int reportLine = 0;
            for (Map.Entry<String, Counts> entry : counts.entrySet()) {
                String category = entry.getKey();
                Counts catCount = entry.getValue();

                report[reportLine++] = String.format("%-25s|%,15d|%,15d|%,15d|%,15d|%,15d", category, catCount.modified, catCount.deleted, catCount.added, catCount.updated, catCount.removed);
            }

            Arrays.sort(report);

            out.println();
            out.println("Category                 | API - Modified|  API - Deleted|          Added|        Updated|        Removed");
            out.println("-------------------------+---------------+---------------+---------------+---------------+---------------");
            for (String line : report) {
                out.println(line);
            }

            out.println();
        }
    }

    public static synchronized void modified(String category) {
        getCountsFor(category).modified++;

    }

    public static synchronized void deleted(String category) {
        getCountsFor(category).deleted++;

    }

    public static synchronized void added(String category) {
        getCountsFor(category).added++;
    }

    public static synchronized void updated(String category) {
        getCountsFor(category).updated++;
    }

    public static synchronized void removed(String category) {
        getCountsFor(category).removed++;
    }

    public static synchronized void register(String category) {
        if (counts.get(category) == null) {
            counts.put(category, new Counts());
        }
    }

    private static Counts getCountsFor(String category) {
        Counts c = counts.get(category);
        if (c == null) {
            c = new Counts();
            counts.put(category, c);
        }
        return c;
    }

    private static class Counts {
        long modified;
        long deleted;

        long added;
        long updated;
        long removed;
    }
}
