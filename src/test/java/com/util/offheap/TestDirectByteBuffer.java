package com.util.offheap;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectByteBuffer {
	
	@Test
	public void testPutAndGet() {
		 ByteBuffer b = new DirectByteBuffer(50);
		 String s = "test";
		 byte[] bb = s.getBytes();
		 b.put(0, bb, 0, bb.length);
		 b.get(0, bb, 0, bb.length);
		 Assert.assertTrue(s.equals(new String(bb)));
		 
		 byte [] bbb = new byte[50];
		 b.put(0, bbb, 0, bbb.length);
		 b.get(0, bbb, 0, bbb.length);
	}
	
	@Test
	public void TestSlice() {
		ByteBuffer b1 = new DirectByteBuffer(50);
		ByteBuffer b2 = b1.slice(5, 15);
		Assert.assertTrue(b2.capacity() == 10);
		Assert.assertTrue(b1.capacity() == 50);
		
		String s = "test";
		byte[] bb = s.getBytes();
		b1.put(5, bb, 0, bb.length);
		b2.get(0, bb, 0, bb.length);
		Assert.assertTrue(s.equals(new String(bb)));
	}
}
