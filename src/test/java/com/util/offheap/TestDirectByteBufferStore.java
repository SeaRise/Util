package com.util.offheap;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectByteBufferStore {
	private DirectByteBufferStore store = new DirectByteBufferStore(8, 10, 5);
	
	@Test
	public void testAllocateAndFree() {
		ByteBuffer buf1 = store.allocate(80);
		ByteBuffer buf2 = store.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		store.free(buf1);
		store.free(buf2);
		
		buf1 = store.allocate(70);
		buf2 = store.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		store.free(buf1);
		store.free(buf2);
	}
}
