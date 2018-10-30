package com.util.offheap;

@SuppressWarnings("restriction")
public class DirectByteBuffer implements ByteBuffer {
    
    private long address;
    
    private java.nio.ByteBuffer byteBuffer;
    
    
	private static final sun.misc.Unsafe UNSAFE = getUnsafe();
    
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    
    private static final long ARRAY_BASE_OFFSET = (long) UNSAFE.arrayBaseOffset(byte[].class);
    
    public DirectByteBuffer(int capacity) {
        byteBuffer = java.nio.ByteBuffer.allocateDirect(capacity);
        java.lang.reflect.Method method;
        try {
            // Get the actual address by calling address method
            method = byteBuffer.getClass().getDeclaredMethod("address");
            method.setAccessible(true);
            address = (Long) method.invoke(byteBuffer);
        } catch (Exception e) {
            throw new Error(e);
        }
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
    	if ((position+length) > byteBuffer.capacity()) {
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
    

    private static sun.misc.Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (sun.misc.Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
}
