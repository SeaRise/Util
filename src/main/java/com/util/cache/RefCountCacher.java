package com.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 一个引用计数法的缓存框架.
 * */
public class RefCountCacher<K, V> implements Cacher<K, V> {
	
	private final ConcurrentHashMap<K, RefHolder<V>> refs = 
			new ConcurrentHashMap<K, RefHolder<V>>();
	
	private final CacherOptions<K, V> options;
	
	//count的操作在锁块中,所以无需其他修饰符
	private int count = 0;
	
	private final ReentrantLock lock;
	
	private static final long TIME_WAIT = 10;
	
	public RefCountCacher(final CacherOptions<K, V> options) {
		this.options = options;
		lock = new ReentrantLock();
	}
	
	/**
	 * 状态变化
	 * null == ref                   		  --> ref != null && ref.isGetting(start get) --> 
	 * ref != null && !ref.isGetting(end get) --> null == ref(realse)
	 * */
	public V get(K key) {
		RefHolder<V> ref;
		for (;;) {
			ref = refs.get(key);
			if (null != ref && ref.isGetting) {
				//资源正在被其他线程获取
				//这里不用两次判断,反正判错了也是continue
				doWhenGetting();
				continue;
			} else if (null != ref) {
				//资源在缓存中
				//直接返回资源
				try {
					//这里加锁是为了和release阻塞.
					lock.lock();
					ref = refs.get(key);
					//两次判断
					//这个条件在第二次判断会不成立的原因只会是realease了.ref == null
					if (null != ref) {
						ref.refCount.incrementAndGet();
						return ref.value;
					}
					//continue,下次循环get
					continue;
				} finally {
					lock.unlock();
				}
			} else {
				//资源不在缓存中,也没有线程正在获取资源
				final int maxHandles = options.getMaxHandles();
				try {
					lock.lock();
					//缓存资源数目已达上限.
					if (maxHandles > 0 && count >= maxHandles) {
						return null;
					}
					ref = refs.get(key);
					//这里再判断一次是否资源在缓存中或正在被获取,两次判断
					//因为在进入if段到进入else分支的时候,有可能正好其他线程从没有getting到正在getting.
					if (ref != null) {
						continue;
					}
					//提前占坑
					count++;
					ref = new RefHolder<V>(true);
					refs.put(key, ref);
				} finally {
					lock.unlock();
				}
				
				//options.get本身应该是线程安全的.这里并发执行是为了防止lock加锁粒度太大
				//能走到这一步的只可能是一个线程
				V cache = options.get(key);
				if (null == cache) {
					throw new RuntimeException("options get cache happen error");
				}
				ref.value = cache;
				ref.refCount = new AtomicInteger(1);
				ref.isGetting = false;
				return cache;
			}
		}
	}
	
	private void doWhenGetting() {
		try {
			TimeUnit.MILLISECONDS.sleep(TIME_WAIT);
		} catch (InterruptedException e) {
			throw new RuntimeException("time wait error");
		}
	}

	public void release(K key) {
		RefHolder<V> ref = refs.get(key);
		//这里只判断refCount == 0是因为refCount不可能 < 0
		if (0 == ref.refCount.decrementAndGet()) {
			try {
				lock.lock();
				//两次判断
				if (ref.refCount.get() > 0) {
					return;
				}
				//release可以不是线程安全的.
				options.release(key);
				refs.remove(key);
				count--;
			} finally {
				lock.unlock();
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
		private volatile boolean isGetting;
		private volatile V value;
		
		private RefHolder(boolean isGetting) {
			this.isGetting = isGetting;
		}
	}

}
