package com.util.buffer;

import org.junit.Assert;
import org.junit.Test;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.CombineBuffer;
import com.util.buffer.bytebuffer.DirectByteBuffer;

public class TestCombineBuffer {
	
	@Test
	public void test() {
		ByteBuffer[] b = new ByteBuffer[5];
		for (int i = 0; i < 5; i++) {
			b[i] = new DirectByteBuffer(i+5);
		}
		
		ByteBuffer cb = new CombineBuffer(b);
		
		System.out.println(cb.capacity());
		Assert.assertTrue(cb.capacity() == 35);
		
		String s = "test test";
		byte[] bb = s.getBytes();
		cb.put(0, bb, 0, bb.length);
		cb.get(0, bb, 0, bb.length);
		Assert.assertTrue(s.equals(new String(bb)));
		
		byte [] bbb = new byte[35];
		bbb[0] = 1;
		bbb[bbb.length-1] = 1;
		cb.put(0, bbb, 0, bbb.length);
		byte[] bbbb = new byte[35];
		cb.get(0, bbbb, 0, bbbb.length);
		Assert.assertArrayEquals(bbb, bbbb);
	}
	
}
