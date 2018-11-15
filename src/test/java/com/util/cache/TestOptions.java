package com.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class TestOptions<K, V> implements CacherOptions<K, V> {
	
	TestOptions(int handles) {
		this.maxHandles = handles;
	}
	
	private final int maxHandles;
	
	private ConcurrentHashMap<K, AtomicBoolean> m = 
			new ConcurrentHashMap<K, AtomicBoolean>();
	
	@SuppressWarnings("unchecked")
	public V get(K key) {
		AtomicBoolean b = new  AtomicBoolean(true);
		m.put(key, new  AtomicBoolean(true));
		return (V) b;
	}

	public void release(K key) {
		m.get(key).set(false);
	}

	public int getMaxHandles() {
		return maxHandles;
	}

	public void close() {}
}