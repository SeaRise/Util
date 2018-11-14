package com.util.buffer.store;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.HeapByteBuffer;

public class HeapByteBufferStore extends AbstractByteBufferStore implements ByteBufferStore {

	public HeapByteBufferStore(int chunkSize, int chunkCount, int bufferSize) {
		super(chunkSize, chunkCount, bufferSize);
	}

	@Override
	protected ByteBuffer initCentralBuffer(int size) {
		return new HeapByteBuffer(size);
	}

	@Override
	protected ByteBufferStore initPage(int chunkSize, int chunkCount,
			ByteBuffer buf) {
		return new HeapByteBufferPage(chunkSize, chunkCount, buf);
	}

}
