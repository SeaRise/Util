package com.util.cache;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 一个lru替换策略的缓存框架.
 * thread safe
 * */
public class LRUCacher<K, V> implements Cacher<K, V> {

	private final CacherOptions<K, V> options;
	
	private int count = 0;
	
	private final HashMap<K, LRUHolder<V>> lruMap = new HashMap<K, LRUHolder<V>>();
	
	//add tail
	//remove head
	private final ConcurrentLinkedQueue<K> lruList = new ConcurrentLinkedQueue<K>();
	
	private final ReentrantLock lock;
	
	public LRUCacher(final CacherOptions<K, V> options) {
		this.options = options;
		lock = new ReentrantLock();
	}
	
	public V get(K key) {
		LRUHolder<V> holder;
		for (;;) {
			lock.lock();
			holder = lruMap.get(key);
			if (null == holder) {
				if (options.getMaxHandles() > 0 && count == options.getMaxHandles()) {
					if (lruList.isEmpty()) {
						lock.unlock();
						Thread.yield();
						continue;
					}
					K removeKey = lruList.remove();
					lruMap.remove(removeKey);
					options.release(removeKey);
					count--;
				}
				count++;
				holder = new LRUHolder<V>();
				lruMap.put(key, holder);
				lock.unlock();
				break;
			} else if (holder.isGetting) {
				lock.unlock();
				Thread.yield();
				continue;
			} else {
				lock.unlock();
				lruList.remove();
				lruList.add(key);
				return holder.value;
			}
		}
		
		V cache = options.get(key);
		if (null == cache) {
			throw new RuntimeException("options get cache happen error");
		}
		holder.value = cache;
		holder.isGetting = false;
		lruList.add(key);
		return cache;	
	}

	//不起作用
	public void release(K key) {}

	public void close() {
		options.close();
		lruMap.clear();
		lruList.clear();
	}
	
	@SuppressWarnings("hiding")
	private class LRUHolder<V> {
		private volatile boolean isGetting;
		private volatile V value;
		
		private LRUHolder() {
			this.isGetting = true;
		}
	}

	public int count() {
		try {
			lock.lock();
			return count;
		} finally {
			lock.unlock();
		}
	}
	
}
