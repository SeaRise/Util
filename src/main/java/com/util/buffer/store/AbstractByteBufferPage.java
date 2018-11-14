package com.util.buffer.store;

import java.util.BitSet;

import com.util.buffer.bytebuffer.ByteBuffer;

/*
 * Thread unsafe
 * 
 * 使用位图来分配内存
 * */
abstract class AbstractByteBufferPage implements ByteBufferStore {
	
	protected final int chunkSize;
	
	protected final int chunkCount;
	
	private final BitSet allocateTrack;
	
	protected final ByteBuffer pageBuf;
	
	private int freeMemory;
	
	AbstractByteBufferPage(final int chunkSize, final int chunkCount, final ByteBuffer buf) {
		if (buf.capacity() != chunkSize*chunkCount) {
			throw new IllegalArgumentException("byte buffer capacity error!");
		}
		this.chunkSize = chunkSize;
		this.chunkCount = chunkCount;
		this.pageBuf = buf;
		this.allocateTrack = new BitSet(chunkCount);
        this.freeMemory = pageBuf.capacity();
	}
	
	public ByteBuffer allocate(int size) {
		//do not allocate
		if (0 == size || size > freeMemory) {
			return null;
		}
		
		int theChunkCount = 0 == (size % chunkSize) ? 
				size / chunkSize : size / chunkSize + 1;
		int startChunk = -1;
		int continueCount = 0;
		for (int i = allocateTrack.nextClearBit(0); i < chunkCount;) {
			if (false == allocateTrack.get(i)) {
				if (-1 == startChunk) {
					startChunk = i;
					continueCount = 1;
					if (1 == theChunkCount) {
						break;
					}
				} else {
					if (theChunkCount == ++continueCount) {
						break;
					}
				}
				i++;
			} else {
				startChunk = -1;
				continueCount = 0;
				i = allocateTrack.nextClearBit(i);
			}
		}
			
		if (continueCount == theChunkCount) {
			markChunksUsed(startChunk, theChunkCount);
			freeMemory -= theChunkCount * chunkSize;
			return returnSlice(startChunk, theChunkCount);
		} else {
			return null;
		}
	}
	
	private ByteBuffer returnSlice(int startChunk, int theChunkCount) {
		int offsetStart = startChunk * chunkSize;
		int offsetEnd = offsetStart + theChunkCount * chunkSize;
		return pageBuf.slice(offsetStart, offsetEnd);
	}

	//需要实现ensureBelong和getRecycleStartChunk
	public boolean free(ByteBuffer byteBuffer) {
		if (null == byteBuffer) {
			return false;
		}
		
		if (!ensureBelong(byteBuffer)) {
			return false;
		}
		
		//recycle
		int startChunk = getRecycleStartChunk(byteBuffer);
		int theChunkCount = byteBuffer.capacity() / chunkSize;
		markChunksUnused(startChunk, theChunkCount);
		freeMemory += byteBuffer.capacity();
		return true;
	}
	
	//do allocate by this page ? 
	protected abstract boolean ensureBelong(ByteBuffer byteBuffer);
	
	//获取回收buffer的startChunk
	protected abstract int getRecycleStartChunk(ByteBuffer byteBuffer);
	
	public int freeMemory() {
		return freeMemory;
	}
	
	public int capacity() {
		return pageBuf.capacity();
	}
	
	private void markChunksUsed(int startChunk, int theChunkCount) {
		allocateTrack.set(startChunk, startChunk+theChunkCount);
	}
	
	private void markChunksUnused(int startChunk, int theChunkCount) {
		allocateTrack.clear(startChunk, startChunk+theChunkCount);
	}

	public void close() {
		pageBuf.free();
	}
	
	
	
}