package com.util.concurrent;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestAppendOnlyArrayList {
	@Test
	public void testLocal() {
		AppendOnlyArrayList<String> ss = new AppendOnlyArrayList<String>();
		ss.add("ss");
		Assert.assertTrue("ss".equals(ss.get(0)));
		ss.set(0, "sss");
		Assert.assertTrue("sss".equals(ss.get(0)));
	}
	
	static AtomicInteger count = new AtomicInteger(0);
	
	@Test
	public void testMutiThread() throws InterruptedException {
		int size = 10000000;
		AppendOnlyArrayList<Integer> ss = new AppendOnlyArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			ss.add(i);
		}
		ExecutorService service = Executors.newFixedThreadPool(6);
		service.execute(new set1(ss));
		service.execute(new set2(ss));
		service.execute(new read1(ss));
		service.execute(new read2(ss));
		service.execute(new write1(ss));
		service.execute(new write2(ss));
		while(count.intValue() != 6) {
			Thread.sleep(100);
		}
	}
	
	static class read1 implements Runnable {

		AppendOnlyArrayList<Integer> ss;
		
		read1(AppendOnlyArrayList<Integer> ss) {
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

		AppendOnlyArrayList<Integer> ss;
		
		read2(AppendOnlyArrayList<Integer> ss) {
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
	
	static class write1 implements Runnable {

		AppendOnlyArrayList<Integer> ss;
		
		write1(AppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		
		public void run() {
			for (int i = 10; i < 15; i++) {
				ss.add(i);
			}
			count.incrementAndGet();
		}
		
	}
	
	static class write2 implements Runnable {
		AppendOnlyArrayList<Integer> ss;
		
		write2(AppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		public void run() {
			for (int i = 15; i < 20; i++) {
				ss.add(i);
			}
			count.incrementAndGet();
		}
		
		
	}
	
	static class set1 implements Runnable {
		AppendOnlyArrayList<Integer> ss;
		
		set1(AppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		public void run() {
			int i;
			for (i = 0; i < ss.size(); i++) {
				ss.set(i, i);
			}
			System.out.println("s1: " + i);
			count.incrementAndGet();
		}
		
	}
	
	static class set2 implements Runnable {
		AppendOnlyArrayList<Integer> ss;
		
		set2(AppendOnlyArrayList<Integer> ss) {
			this.ss = ss;
		}
		public void run() {
			Random rand = new Random();
			int i;
			for (i = 0; i < ss.size(); i++) {
				int index = rand.nextInt(ss.size());
				ss.set(index, index);
			}
			System.out.println("s2: " + i);
			count.incrementAndGet();
		}
		
	}
}
