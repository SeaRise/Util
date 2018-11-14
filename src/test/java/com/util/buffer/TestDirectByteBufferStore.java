package com.util.buffer;

import org.junit.Assert;
import org.junit.Test;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.store.DirectByteBufferStore;

public class TestDirectByteBufferStore {
	private DirectByteBufferStore store = new DirectByteBufferStore(8, 10, 5);
	
	@Test
	public void testAllocateAndFree() {
		ByteBuffer buf1 = store.allocate(80);
		ByteBuffer buf2 = store.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		Assert.assertTrue(store.free(buf1));
		Assert.assertTrue(store.free(buf2));
		
		buf1 = store.allocate(70);
		buf2 = store.allocate(1);
		Assert.assertNotNull(buf1);
		Assert.assertNotNull(buf2);
		Assert.assertTrue(store.free(buf1));
		Assert.assertTrue(store.free(buf2));
	}
}
