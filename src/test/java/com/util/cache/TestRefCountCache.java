package com.util.cache;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestRefCountCache {
	
	private static int maxHandles = 30000;
	
	private static int threadNum = 1000;
	
	private static int cacheNum = 30000;
	
	private static AtomicInteger count = new AtomicInteger(0);
	
	private RefCountCacher<Integer, AtomicBoolean> cacher = 
			new RefCountCacher<Integer, AtomicBoolean>(
					new TestOptions<Integer, AtomicBoolean>());
	
	@Test
	public void test() throws InterruptedException {
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			service.execute(new Do());
		}
		while(count.intValue() != cacheNum*threadNum) {
			Thread.sleep(100);
		}
		Assert.assertTrue(0 == cacher.count());
		
	}
	
	class Do implements Runnable {
		
		public void run() {
			Random rand = new Random();
			for (int i = 0; i < cacheNum; i++) {
				int c = rand.nextInt(cacheNum);
				Assert.assertTrue(cacher.get(c).get());
				Thread.yield();
				cacher.release(c);
				count.incrementAndGet();
			}
		}
		
	}
	
	static class TestOptions<K, V> implements CacherOptions<K, V> {
		
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
}
