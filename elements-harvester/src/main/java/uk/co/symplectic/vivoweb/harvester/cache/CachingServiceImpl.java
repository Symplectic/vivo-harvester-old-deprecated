/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CachingServiceImpl {
    private static CacheManager cacheManager = CacheManager.create();
    private static Cache cache = null;

    private CachingServiceImpl() { }

    static {
        long maxBytes      = 1024 * 1024 * 1024; // 1 Gig
        long reservedBytes = 200  * 1024 * 1024; // 200 Meg

        // Ensure that the runtime thinks we have more than 200 Meg available
        if (Runtime.getRuntime().maxMemory() > reservedBytes) {
            cache = new Cache("cachingService", 0, false, true, 0, 0);

            // Ensure that the cache can't grow to configured memory - 200Meg, or 1 Gig total, whichever is less
            cache.getCacheConfiguration().setMaxBytesLocalHeap(Math.min(maxBytes, Runtime.getRuntime().maxMemory() - reservedBytes));
            cacheManager.addCache(cache);
        }
    }

    public static void put(String key, Object value) {
        if (cache != null) {
            cache.put(new Element(key, value));
        }
    }

    public static Object get(String key) {
        if (cache != null) {
            Element element = cache.get(key);
            if (element != null) {
                return element.getObjectValue();
            }
        }

        return null;
    }

    public static void remove(String key) {
        if (cache != null) {
            cache.remove(key);
        }
    }
}
