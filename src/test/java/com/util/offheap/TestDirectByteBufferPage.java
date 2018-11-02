package com.util.offheap;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectByteBufferPage {
	private DirectByteBufferPage page = new DirectByteBufferPage(8, 10);
	
	@Test
	public void testAllocateAndFree() {
		ByteBuffer buf1 = page.allocate(80);
		Assert.assertNotNull(buf1);
		ByteBuffer buf2 = page.allocate(1);
		Assert.assertNull(buf2);
		page.free(buf1);
		Assert.assertTrue(80 == page.freeMemory());
		page.free(buf2);
		Assert.assertTrue(80 == page.freeMemory());
		
		buf1 = page.allocate(70);
		buf2 = page.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		page.free(buf1);
		page.free(buf2);
		Assert.assertTrue(80 == page.freeMemory());
	}
}
