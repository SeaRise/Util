package com.util.buffer.store;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.DirectByteBuffer;

/*
 * Thread unsafe
 * 
 * 使用位图来分配内存
 * */
public class DirectByteBufferPage extends AbstractByteBufferPage implements ByteBufferStore {
	
	private final long address;
	
	public DirectByteBufferPage(final int chunkSize, final int chunkCount) {
		this(chunkSize, chunkCount, new DirectByteBuffer(chunkSize*chunkCount));
	}
	
	DirectByteBufferPage(int chunkSize, int chunkCount, ByteBuffer buf) {
		super(chunkSize, chunkCount, buf);
        this.address = ((DirectByteBuffer)buf).address();
	}

	@Override
	protected boolean ensureBelong(ByteBuffer byteBuffer) {
		long bufferAddress = ((DirectByteBuffer)byteBuffer).address();
		return !(bufferAddress < address || bufferAddress >= address + capacity());
	}
	
	@Override
	protected int getRecycleStartChunk(ByteBuffer byteBuffer) {
		return (int) ((((DirectByteBuffer)byteBuffer).address()-address)/chunkSize);
	}
}
