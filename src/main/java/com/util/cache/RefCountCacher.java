package com.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.util.lock.SegmentLock;

public class RefCountCacher<K, V> implements Cacher<K, V> {
	
	private final ConcurrentHashMap<K, RefHolder<V>> refs = 
			new ConcurrentHashMap<K, RefHolder<V>>();
	
	private final CacherOptions<K, V> options;
	
	private final AtomicInteger count = new AtomicInteger(0);
	
	private final SegmentLock lock;
	
	private static final long TIME_WAIT = 10;
	
	public RefCountCacher(final CacherOptions<K, V> options) {
		this.options = options;
		lock = new SegmentLock();
	}
	
	public RefCountCacher(final CacherOptions<K, V> options, int concurrentLevel) {
		this.options = options;
		lock = new SegmentLock(concurrentLevel);
	}

	public V get(K key) {
		RefHolder<V> ref;
		for (;;) {
			ref = refs.get(key);
			if (null != ref && ref.isGetting.get()) {
				doWhenGetting();
				continue;
			} else if (null != ref && ref.isGetting.get()) {
				return doReturnCache(ref, key);
			} else {
				final int maxHandles = options.getMaxHandles();
				if (maxHandles > 0 && count.get() >= maxHandles) {
					return null;
				}
				try {
					lock.lock(key.hashCode());
					ref = refs.get(key);
					if (ref != null) {
						continue;
					}
					count.incrementAndGet();
					ref = new RefHolder<V>(new AtomicBoolean(true));
					refs.put(key, ref);
				} finally {
					lock.unlock(key.hashCode());
				}
				
				V cache = options.get(key);
				if (null == cache) {
					count.decrementAndGet();
					refs.remove(key);
					return null;
				}
				ref.value = cache;
				ref.refCount = new AtomicInteger(1);
				ref.isGetting.set(false);
				return cache;
			}
		}
	}
	
	private void doWhenGetting() {
		try {
			Thread.sleep(TIME_WAIT);
		} catch (InterruptedException e) {
			throw new RuntimeException("time wait error");
		}
	}
	
	private V doReturnCache(RefHolder<V> ref, K key) {
		try {
			ref.refCount.incrementAndGet();
			return ref.value;
		} finally {
			lock.unlock(key.hashCode());
		}
	}

	public void release(K key) {
		RefHolder<V> ref = refs.get(key);
		if (0 == ref.refCount.decrementAndGet()) {
			try {
				lock.lock(key.hashCode());
				if (0 != ref.refCount.get()) {
					return;
				}
				options.release(key);
				refs.remove(key);
			} finally {
				lock.unlock(key.hashCode());
			}
			
		}
	}

	public void close() {
		options.close();
		refs.clear();
	}
	
	@SuppressWarnings("hiding")
	private class RefHolder<V> {
		private volatile AtomicInteger refCount;
		private volatile AtomicBoolean isGetting;
		private volatile V value;
		
		private RefHolder(AtomicBoolean isGetting) {
			this.isGetting = isGetting;
		}
	}

}
