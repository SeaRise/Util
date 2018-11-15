package com.util.cache;

/*
 * 缓存
 * */
public interface Cacher<K, V> {
	
	V get(K key);
	
	void release(K key);
	
	void close();
}
