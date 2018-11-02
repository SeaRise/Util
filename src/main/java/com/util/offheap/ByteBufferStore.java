package com.util.offheap;

public interface ByteBufferStore {
	
	ByteBuffer allocate(int size);
	
	boolean free(ByteBuffer byteBuffer);
	
	void close();
}
