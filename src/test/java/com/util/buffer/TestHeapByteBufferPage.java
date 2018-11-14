package com.util.buffer;

import org.junit.Assert;
import org.junit.Test;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.HeapByteBuffer;
import com.util.buffer.store.HeapByteBufferPage;

public class TestHeapByteBufferPage {
	private HeapByteBufferPage page = new HeapByteBufferPage(8, 10);
	
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
		
		Assert.assertFalse(page.free(new HeapByteBuffer(5)));
	}
}