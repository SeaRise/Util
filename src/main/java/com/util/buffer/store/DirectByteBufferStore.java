package com.util.buffer.store;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.DirectByteBuffer;

/*
 * thread safe
 * 
 * 使用多个byteBufferPage来分配内存
 * **/
public class DirectByteBufferStore extends AbstractByteBufferStore implements ByteBufferStore {
	
	public DirectByteBufferStore(int chunkSize, int chunkCount, int bufferSize) {
		super(chunkSize, chunkCount, bufferSize);
	}

	@Override
	protected ByteBuffer initCentralBuffer(int size) {
		return new DirectByteBuffer(size);
	}

	@Override
	protected ByteBufferStore initPage(int chunkSize, int chunkCount,
			ByteBuffer buf) {
		return new DirectByteBufferPage(chunkSize, chunkCount, buf);
	}

}
