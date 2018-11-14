package com.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 一个引用计数法的缓存框架.
 * 
 * 想尽可能去做无锁,但是发现代码逻辑上有问题
 * DRefCountCacher是安全的,不过加锁的地方很多.
 * */
public class RefCountCacher<K, V> implements Cacher<K, V> {
	
	// Logger
	private final static Logger LOGGER = Logger.getLogger(RefCountCacher.class.getName());
	
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
				//判断错误的可能是状态变化到了
				// - ref != null && !ref.isGetting(end get)
				// - null == ref
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
					//这个条件在第二次判断会不成立的原因可能是
					// - realease了.ref == null
					// - 其他线程正在getting,null != ref && ref.isGetting
					if (null != ref && !ref.isGetting) {
						ref.refCount.incrementAndGet();
						LOGGER.log(Level.INFO, "return: " + key + "  count: " + ref.refCount);
						return ref.value;
					}
					//ref已经被release了或这正在getting
					//continue,下次循环get
					continue;
				} finally {
					lock.unlock();
				}
			} else {
				//资源不在缓存中,也没有线程正在获取资源
				try {
					lock.lock();
					//缓存资源数目已达上限.
					if (options.getMaxHandles() > 0 && 
							count >= options.getMaxHandles()) {
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
					LOGGER.log(Level.INFO, "getting: " + key);
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
				ref.value = cache;
				ref.refCount = new AtomicInteger(1);
				ref.isGetting = false;
				LOGGER.log(Level.INFO, "had getten: " + key + "  count: " + ref.refCount);
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

	//根据引用计数法,应该不会重复release才对.
	//所以ref != null一定成立
	//但是测试结果发现了有ref == null
	public void release(K key) {
		//这里只判断refCount == 0是因为refCount不可能 < 0
		int c = refs.get(key).refCount.decrementAndGet();
		if (0 == c) {
			try {
				lock.lock();
				LOGGER.log(Level.INFO, "begin realease: " + key + "  count: " + c);
				//两次判断,在加锁之前,有线程又引用了key,那么引用数就>0
				if (null == refs.get(key)) {
					throw new RuntimeException("ref null key : " + key);
				}
				if (refs.get(key).refCount.get() > 0) {
					return;
				}
				//release可以不是线程安全的.
				refs.remove(key);
				count--;
				options.release(key);
				LOGGER.log(Level.INFO, "realease: " + key);
				return;
			} finally {
				lock.unlock();
			}
		}
		LOGGER.log(Level.INFO, "not get: " + key + "  count: " + c);
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
