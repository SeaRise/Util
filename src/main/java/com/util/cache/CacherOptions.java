package com.util.cache;

public interface CacherOptions<K, V> {
	// 当key不在缓存中时, 则调用该函数取得对应资源.
	// 该函数必须要是并发安全的.
	V get(K key);
	
	// 释放资源的行为.
	void release(K key);
	
	//最大资源数
	int getMaxHandles();
	
	//关闭
	void close();
}
