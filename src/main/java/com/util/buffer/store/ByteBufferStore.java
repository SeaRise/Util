package com.util.buffer.store;

import com.util.buffer.bytebuffer.ByteBuffer;

public interface ByteBufferStore {
	
	ByteBuffer allocate(int size);
	
	boolean free(ByteBuffer byteBuffer);
	
	void close();
	
	int freeMemory();
}
