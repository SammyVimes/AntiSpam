package com.danilov.smsfirewall;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Cache<K, V> implements Map<K, V>{
	
	public static final long DEFAULT_TTL = 6000;
	public static final long DEFAULT_TTC = 60000;
	
	private long lastUT = 0; 
	
	private long ttl;
	private long ttc;
	
	private final Object mMonitor = new Object();
	
	private Map<K, CacheValue<K, V>> mMap;
	
	public Cache() {
		this(DEFAULT_TTL, DEFAULT_TTC);
	}
	
	public Cache(final long ttl, final long ttc) {
		mMap = new HashMap<K, CacheValue<K, V>>();
		this.ttl = ttl;
		this.ttc = ttc;
	}
	
	public V put(final K key, final V data) {
		if (needToClean()) {
			clean();
		}
		CacheValue<K, V> cacheValue = new CacheValue<K, V>(key, data, ttl);
		mMap.put(key, cacheValue);
		return data;
	}

	@Override
	public V get(Object key) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (needToClean()) {
			clean();
		}
		CacheValue<K, V> cacheValue = mMap.get(key);
		if (cacheValue == null) {
			return null;
		}
		cacheValue.hit();
		return cacheValue.getData();
	}
	
	private class CacheValue<K, V> {
		
		private long lastHitTime = 0;
		private long ttl;
		
		private K key;
		private V data;
		
		public CacheValue(final K key, final V data, final long ttl) {
			this.key = key;
			this.data = data;
			lastHitTime = new Date().getTime();
		}
		
		public V getData() {
			return data;
		}
		
		public K getKey() {
			return key;
		}
		
		public boolean isExpired() {
			return (new Date()).getTime() > lastHitTime + ttl;
		}
		
		public void hit() {
			lastHitTime = new Date().getTime();
		}
		
	}
	
	private boolean needToClean() {
		return (new Date()).getTime() > lastUT + ttc;
	}

	private void clean() {
		long curTime = (new Date()).getTime();
		synchronized (mMonitor) {
			Set<K> keySet = mMap.keySet();
			for (K key : keySet) {
				CacheValue<K, V> value = mMap.get(key);
				if (value.isExpired()) {
					mMap.remove(key);
				}
			}
		}
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
