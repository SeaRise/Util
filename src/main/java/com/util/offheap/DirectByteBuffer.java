package com.util.offheap;

import com.util.common.Common;

/*
 * 堆外内存bytebuffer
 * thread unsafe
 * **/
@SuppressWarnings("restriction")
public class DirectByteBuffer implements ByteBuffer {
    
    private final long address;
    
    private final int capacity;
    
    private final java.nio.ByteBuffer byteBuffer;
    
	private static final sun.misc.Unsafe UNSAFE = Common.getUnsafe();
    
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    
    private static final long ARRAY_BASE_OFFSET = (long) UNSAFE.arrayBaseOffset(byte[].class);
    
    public DirectByteBuffer(int capacity) {
    	this(java.nio.ByteBuffer.allocateDirect(capacity));
    }
    
    private DirectByteBuffer(java.nio.ByteBuffer buf) {
    	this.byteBuffer = buf;
    	this.capacity = buf.capacity();
    	address = ((sun.nio.ch.DirectBuffer)buf).address();
    }
    
    
    public void get(int position, byte[] destination, int offset, int length) {
    	check(position, destination, offset, length);
        copyToArray(getPosition(position), destination, ARRAY_BASE_OFFSET, offset, length);
    }
    

    public void put(int position, byte[] source, int offset, int length) {
    	check(position, source, offset, length);
        copyFromArray(source, ARRAY_BASE_OFFSET, offset, getPosition(position), length);
    }
    
    /*
     * 检查越界
     * */
    private void check(int position, byte[] b, int offset, int length) {
    	if (position < 0 || offset < 0 || length < 0) {
    		throw new IllegalArgumentException("illegal argument");
    	}
    	
    	if ((position+length) > capacity) {
    		throw new IllegalArgumentException("out of buffer capacity");
    	}
    	
    	if ((offset+length) > b.length) {
    		throw new IllegalArgumentException("out of array capacity");
    	}
    }

    private long getPosition(int position) {
        return address + position;
    }
    

    private static void copyToArray(long sourceAddress, Object destination, long destinationBaseOffset,
            long destinationPosition, long length) {
        // Calculate the distance from the beginning of the object up until a
        // destinationPoint
        long offset = destinationBaseOffset + destinationPosition;
        while (length > 0) {
            // Copy as much as copy threshold
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            // Copy from bytebuffer to destination
            UNSAFE.copyMemory(null, sourceAddress, destination, offset, size);
            length -= size;
            sourceAddress += size;
            offset += size;
        }
    }
    

    private static void copyFromArray(Object source, long sourceBaseOffset, long sourcePosition,
            long destinationAddress, long length) {
        // Calculate the distance from the beginning of the object up until a
        // sourcePoint
        long offset = sourceBaseOffset + sourcePosition;
        while (length > 0) {
            // Copy as much as copy threshold
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            // Copy from source to bytebuffer
            UNSAFE.copyMemory(source, offset, null, destinationAddress, size);
            length -= size;
            offset += size;
            destinationAddress += size;
        }
    }
    
    public ByteBuffer slice(int start, int end) {
    	if (start < 0 || end < 0 || start > end || 
    			start >= capacity || end > capacity) {
    		throw new IllegalArgumentException("illegal argument");
    	}
    	
    	byteBuffer.limit(end);
    	byteBuffer.position(start);
    	return new DirectByteBuffer(byteBuffer.slice());
    } 
    
    public int capacity() {
    	return capacity;
    }
    
    public long address() {
    	return address;
    }

    public void free() {
        try {
            java.lang.reflect.Field cleanerField = byteBuffer.getClass().getDeclaredField("cleaner");
            cleanerField.setAccessible(true);
            // Cleaner can force freeing of native memory by clean method
            sun.misc.Cleaner cleaner = (sun.misc.Cleaner) cleanerField.get(byteBuffer);
            cleaner.clean();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
