package com.util.concurrent;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestDAppendOnlyArrayList {
	@Test
	public void testLocal() {
		DAppendOnlyArrayList<String> ss = new DAppendOnlyArrayList<String>();
		ss.append("ss");
		Assert.assertTrue("ss".equals(ss.get(0)));
		ss.set(0, "sss");
		Assert.assertTrue("sss".equals(ss.get(0)));
		//System.out.println("cap: " + ss.capacity());
	}
	
	static AtomicInteger count = new AtomicInteger(0);
	static int addSize = 300000;
	static int threadNum = 100;
	@Test
	public void testMutiThread() throws InterruptedException {
		int size = 100000;
		int c = threadNum*3;
		DAppendOnlyArrayList<Integer> ss = new DAppendOnlyArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			ss.append(i);
		}
		ExecutorService service = Executors.newFixedThreadPool(c);
		for (int i = 0; i < threadNum; i++) {
			service.execute(new read1(ss));
			service.execute(new read2(ss));
			service.execute(new write(ss));
		}
		
		while(count.intValue() != c) {
			Thread.sleep(100);
		}
	}
	
	static class read1 implements Runnable {

		DAppendOnlyArrayList<Integer> ss;
		
		read1(DAppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		
		public void run() {
			int i;
			for (i = 0; i < ss.size(); i++) {
				Assert.assertNotNull(ss.get(i));
			}
			System.out.println("r1: " + i);
			count.incrementAndGet();
		}
		
	}
	
	static class read2 implements Runnable {

		DAppendOnlyArrayList<Integer> ss;
		
		read2(DAppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		
		public void run() {
			Random rand = new Random();
			int i;
			for (i = 0; i < ss.size(); i++) {
				Assert.assertNotNull(ss.get(rand.nextInt(ss.size())));
			}
			System.out.println("r2: " + i);
			count.incrementAndGet();
		}
		
	}
	
	static class write implements Runnable {
		DAppendOnlyArrayList<Integer> ss;
		
		write(DAppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		public void run() {
			for (int i = 0; i < addSize; i++) {
				ss.append(i);
			}
			System.out.println("w: ");
			count.incrementAndGet();
		}
		
		
	}
}
