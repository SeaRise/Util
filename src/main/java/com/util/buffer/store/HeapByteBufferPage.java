package com.util.buffer.store;

import java.lang.reflect.Field;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.HeapByteBuffer;

/*
 * Thread unsafe
 * 
 * 使用位图来分配内存
 * */
public class HeapByteBufferPage extends AbstractByteBufferPage implements ByteBufferStore {
	
	private final int address;
	
	private final byte[] byteArray;
	
	public HeapByteBufferPage(int chunkSize, int chunkCount) {
		this(chunkSize, chunkCount, new HeapByteBuffer(chunkSize*chunkCount));
	}
	
	HeapByteBufferPage(int chunkSize, int chunkCount, ByteBuffer buf) {
		super(chunkSize, chunkCount, buf);
		address = ((HeapByteBuffer)buf).address();
		try {
			byteArray = (byte[]) BYTE_ARRAY_FIELD.get(buf);
		} catch (Exception e) {
			throw new RuntimeException("HeapByteBufferPage init error");
		} 
	}

	@Override
	protected boolean ensureBelong(ByteBuffer byteBuffer) {
		try {
			return BYTE_ARRAY_FIELD.get(byteBuffer) == byteArray;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("HeapByteBufferPage using error");
		} 
	}

	@Override
	protected int getRecycleStartChunk(ByteBuffer byteBuffer) {
		return (((HeapByteBuffer)byteBuffer).address()-address)/chunkSize;
	}

	private static final Field BYTE_ARRAY_FIELD ;
	
	static {
		try {
			BYTE_ARRAY_FIELD = HeapByteBuffer.class.getDeclaredField("byteBuffer");
			BYTE_ARRAY_FIELD.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("init HeapByteBufferPage class error");
		} 
	}
}
