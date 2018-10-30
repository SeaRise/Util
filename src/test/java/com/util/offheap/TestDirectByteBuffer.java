package com.util.offheap;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectByteBuffer {
	
	@Test
	public void test() {
		 DirectByteBuffer b = new DirectByteBuffer(50);
		 String s = "test";
		 byte[] bb = s.getBytes();
		 b.put(0, bb, 0, bb.length);
		 b.get(0, bb, 0, bb.length);
		 Assert.assertTrue(s.equals(new String(bb)));
	}
}
