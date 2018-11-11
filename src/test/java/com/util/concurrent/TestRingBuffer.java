package com.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestRingBuffer {
	
	private RingBuffer<Integer> b = new RingBuffer<Integer>(4);
	private int num = 50000000;
	private ExecutorService service = Executors.newFixedThreadPool(2);
	static AtomicInteger count = new AtomicInteger(0);
	
	@Test
	public void test() throws InterruptedException {
		service.execute(new Read());
		service.execute(new Write());
		while(count.intValue() != 2) {
			Thread.sleep(100);
		}
	}
	
	class Read implements Runnable {

		public void run() {
			int pre = -1;
			for (int i = 0; i < num; i++) {
				Integer inde;
				do {
					inde = b.get();
				} while(inde == null);
				Assert.assertTrue(pre+1 == inde.intValue());
				pre = inde.intValue();
			}
			count.incrementAndGet();
		}
		
	}
	
	class Write implements Runnable {

		public void run() {
			for (int i = 0; i < num; i++) {
				while(!b.put(i));
			}
			count.incrementAndGet();
		}
		
	}
}
