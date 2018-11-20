package com.util.common;


@SuppressWarnings("restriction")
public class DirectAtomicLongArray {
	private final long address;
	
	private final java.nio.ByteBuffer buf;
	
	private final int length;
	
	private static final int LONG_SHIFT = 3;
	
	private static final sun.misc.Unsafe UNSAFE = Common.getUnsafe();
	
	private final long byteOffset(int i) {
        return ((long) i << LONG_SHIFT) + address;
    }
	
	private long checkedByteOffset(int i) {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }
	
	public DirectAtomicLongArray(int length) {
		this.buf = java.nio.ByteBuffer.allocateDirect(length << LONG_SHIFT);
		address = ((sun.nio.ch.DirectBuffer)buf).address();
		this.length = length;
	}
	
	public final int length() {
        return length;
    }
	
	
	public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }
	
	private long getRaw(long offset) {
        return UNSAFE.getLongVolatile(null, offset);
    }
	
	public final void set(int i, long newValue) {
		UNSAFE.putLongVolatile(null, checkedByteOffset(i), newValue);
	}
	
	public final void lazySet(int i, long newValue) {
		UNSAFE.putOrderedLong(null, checkedByteOffset(i), newValue);
    }
	
	public final long getAndSet(int i, long newValue) {
        long offset = checkedByteOffset(i);
        while (true) {
            long current = getRaw(offset);
            if (compareAndSetRaw(offset, current, newValue))
                return current;
        }
    }
	
	public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }
	
	private boolean compareAndSetRaw(long offset, long expect, long update) {
		return UNSAFE.compareAndSwapLong(null, offset, expect, update);
	}
}
