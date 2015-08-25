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
        if (Runtime.getRuntime().maxMemory() > 102400) {
            cache = new Cache("cachingService", 0, false, true, 0, 0);
            cache.getCacheConfiguration().setMaxBytesLocalHeap(Runtime.getRuntime().maxMemory() - 102400);
            cacheManager.addCache(cache);
        }
    }

    public static void put(String key, String value) {
        if (cache != null) {
            cache.put(new Element(key, value));
        }
    }

    public static String get(String key) {
        if (cache != null) {
            Element element = cache.get(key);
            if (element != null) {
                return (String)element.getObjectValue();
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
