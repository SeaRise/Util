package com.util.cache;

public interface Cacher<K, V> {
	
	V get(K key);
	
	void release(K key);
	
	void close();
}
