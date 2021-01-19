package deacon.test.spb.util;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/************************************************************************************
 * @File name   :      CacheUtil.java
 *
 * @Author      :      Eric Wang
 *
 * @Date        :      2011-2-15
 *
 * @Copyright Notice: 
 * Copyright (c) 2015 SHECA, Inc. All  Rights Reserved.
 * This software is published under the terms of the SHECA Software
 * License version 1.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 * 
 * 
 * ----------------------------------------------------------------------------------
 * Date                             Who                 Version         Comments
 * 2011-2-15下午02:45:11            Eric Wang            1.0             Initial Version
 ************************************************************************************/

/**
 * Cache utility - utility class to load config file, and get cache manager
 */

@Component
public final class CacheUtil {
    private Logger log = LoggerFactory.getLogger(CacheUtil.class);
    
	private Cache cache = null;
	@Autowired
	@Qualifier("appEhCacheCacheManager")
	private EhCacheCacheManager manager;
	
	private String cacheName = null;

	@PostConstruct
	private void init() throws  Exception {
		manager.getCacheNames().stream().filter(k -> !StringUtils.isEmpty(k)).findFirst().ifPresent(k -> cacheName = k);
		log.debug("log from config file ehcahe name is {}", cacheName);
		Assert.notNull(cacheName, "cacheName must not be null");
		cache = manager.getCache(cacheName);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Object getCache(String key) {
		log.debug("调用缓存，key=：{} " , key);
        return cache.get(key) == null ? null : cache.get(key).get();
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putCache(String key, Object value) {
		log.info("放入缓存，key=： {}" , key);
		cache.put(key, value);
	}

	public void updateCache(String key, Object value) {
		log.debug("更新缓存，key=： {}" , key);
		cache.putIfAbsent(key, value);
	}

	/**
	 * Remove cache by key
	 * 
	 * @Date : 2011-3-25
	 * @param key key of the cache
	 */
	public void removeCache(String key) {
		log.debug("删除缓存，key=： {}" , key);
		cache.evict(key);
	}

	/**
	 * Check cache by key
	 * 
	 * @Date : 2011-3-25
	 * @param key key of the cache
	 * @return true if key exists
	 */
	public boolean containsCache(String key) {
		if (getCache(key) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Destroy the cache manager
	 * 
	 * @Date : 2011-3-25
	 */
	public void destroy() {
		manager.getCacheManager().shutdown();
	}

}
