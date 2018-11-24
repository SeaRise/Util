package com.util.buffer.bytebuffer;


public interface DataBuffer extends ByteBuffer, DataOperator {
	
	int getReadPosition();
	
	int getWritePosition();
	
}
