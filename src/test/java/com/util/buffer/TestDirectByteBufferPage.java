package com.util.buffer;

import org.junit.Assert;
import org.junit.Test;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.DirectByteBuffer;
import com.util.buffer.store.DirectByteBufferPage;

public class TestDirectByteBufferPage {
	private DirectByteBufferPage page = new DirectByteBufferPage(8, 10);
	
	@Test
	public void testAllocateAndFree() {
		ByteBuffer buf1 = page.allocate(80);
		Assert.assertNotNull(buf1);
		ByteBuffer buf2 = page.allocate(1);
		Assert.assertNull(buf2);
		Assert.assertTrue(page.free(buf1));
		Assert.assertTrue(80 == page.freeMemory());
		Assert.assertFalse(page.free(buf2));
		Assert.assertTrue(80 == page.freeMemory());
		
		buf1 = page.allocate(70);
		buf2 = page.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		Assert.assertTrue(page.free(buf1));
		Assert.assertTrue(page.free(buf2));
		Assert.assertTrue(80 == page.freeMemory());
		
		Assert.assertFalse(page.free(new DirectByteBuffer(5)));
	}
}
