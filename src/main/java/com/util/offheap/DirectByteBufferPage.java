package com.util.offheap;

import java.util.BitSet;

/*
 * Thread unsafe
 * */
public class DirectByteBufferPage implements ByteBufferStore {
	
	private final int chunkSize;
	
	private final int chunkCount;
	
	private final BitSet allocateTrack;
	
	private final ByteBuffer pageBuf;
	
	private final long address;
	
	private int freeMemory;
	
	public DirectByteBufferPage(int chunkSize, int chunkCount) {
		this.chunkSize = chunkSize;
		this.chunkCount = chunkCount;
		this.pageBuf = new DirectByteBuffer(chunkSize*chunkCount);
		this.allocateTrack = new BitSet(chunkCount);
        address = ((DirectByteBuffer)pageBuf).address();
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
			int offsetStart = startChunk * chunkSize;
			int offsetEnd = offsetStart + theChunkCount * chunkSize;
			markChunksUsed(startChunk, theChunkCount);
			freeMemory -= theChunkCount * chunkSize;
			return pageBuf.slice(offsetStart, offsetEnd);
		} else {
			return null;
		}
	}

	public boolean free(ByteBuffer byteBuffer) {
		if (null == byteBuffer) {
			return false;
		}
		
		long bufferAddress = ((DirectByteBuffer)byteBuffer).address();
		int size = byteBuffer.capacity();
		
		//do not allocate by this page
		if (bufferAddress < address || bufferAddress >= address + capacity()) {
			return false;
		}
		
		//recycle
		int startChunk = (int) ((bufferAddress-address)/chunkSize);
		int theChunkCount = size / chunkSize;
		markChunksUnused(startChunk, theChunkCount);
		freeMemory += size;
		return true;
	}
	
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
