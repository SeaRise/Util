package com.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 一个引用计数法的缓存框架.
 * */
public class DRefCountCacher<K, V> implements Cacher<K, V> {
	
	private final ConcurrentHashMap<K, RefHolder<V>> refs = 
			new ConcurrentHashMap<K, RefHolder<V>>();
	
	private final CacherOptions<K, V> options;
	
	//count的操作在锁块中,所以无需其他修饰符
	private int count = 0;
	
	private final ReentrantLock lock;
	
	public DRefCountCacher(final CacherOptions<K, V> options) {
		this.options = options;
		lock = new ReentrantLock();
	}
	
	public V get(K key) {
		RefHolder<V> ref;
		for (;;) {
			try {
				lock.lock();
				ref = refs.get(key);
				if (null != ref && ref.isGetting) {
					continue;
				} else if (null != ref) {
					//资源在缓存中
					//直接返回资源
					ref.refCount++;
					return ref.value;
				} else {
					//资源不在缓存中,也没有线程正在获取资源
					//缓存资源数目已达上限.
					if (options.getMaxHandles() > 0 && 
							count >= options.getMaxHandles()) {
						return null;
					}
					//提前占坑
					count++;
					ref = new RefHolder<V>(true);
					refs.put(key, ref);
				}
			} finally {
				lock.unlock();
			}
			//options.get本身应该是线程安全的.这里并发执行是为了防止lock加锁粒度太大
			//不可能会有多个对相同key操作的线程走到这里
			//一个key只会有一个线程走到这里
			//options.get的线程安全要求只要求不同key的操作线程安全就行
			V cache = options.get(key);
			if (null == cache) {
				throw new RuntimeException("options get cache happen error");
			}
			try {
				lock.lock();
				ref.value = cache;
				ref.refCount = 1;
				ref.isGetting = false;
				return cache;	
			} finally {
				lock.unlock();
			}
			
		}
	}

	public void release(K key) {
		try {
			lock.lock();
			if (0 == --refs.get(key).refCount) {
				//LOGGER.log(Level.INFO, "begin realease: " + key + "  count: " + c);
				//两次判断,在加锁之前,有线程又引用了key,那么引用数就>0
				if (null == refs.get(key)) {
					throw new RuntimeException("ref null key : " + key);
				}
				//release可以不是线程安全的.
				refs.remove(key);
				count--;
				options.release(key);
				return;
			}
		} finally {
			lock.unlock();
		}		
	}

	public void close() {
		options.close();
		refs.clear();
	}
	
	@SuppressWarnings("hiding")
	private class RefHolder<V> {
		private volatile int refCount;
		private volatile boolean isGetting;
		private volatile V value;
		
		private RefHolder(boolean isGetting) {
			this.isGetting = isGetting;
		}
	}

}