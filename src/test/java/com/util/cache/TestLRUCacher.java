package com.util.cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestLRUCacher {
	
	private static int maxHandles = 20000;
	
	private static int threadNum = 1000;
	
	private static int cacheNum = 30000;
	
	private static AtomicInteger count = new AtomicInteger(0);
	
	private LRUCacher<Integer, AtomicBoolean> cacher = 
			new LRUCacher<Integer, AtomicBoolean>(
					new TestOptions<Integer, AtomicBoolean>(maxHandles));
	
	@Test
	public void test() throws InterruptedException {
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			service.execute(new Do());
		}
		while(count.intValue() != cacheNum*threadNum) {
			Thread.sleep(100);
		}
		Assert.assertTrue(maxHandles == cacher.count());
		
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
}
