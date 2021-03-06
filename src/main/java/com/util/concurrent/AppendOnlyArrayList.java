package com.util.concurrent;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import com.util.common.Common;

/**
 * AppendOnlyArrayList只允许追加和修改元素,不支持删除元素
 * 实现方式类似ConcurrentHashMap
 * */
@SuppressWarnings("restriction")
public class AppendOnlyArrayList<E> {
	
	private volatile transient Object[] elementData;
	
	private static final Object[] EMPTY_ELEMENTDATA = {};
	
	private static final int DEFAULT_CAPACITY = 10;
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	private volatile int size = 0;
	
	private final ReentrantLock lock = new ReentrantLock();
	
	public AppendOnlyArrayList(int initialCapacity) {
		initElementData(initialCapacity);
    }
	
	public AppendOnlyArrayList(int initialCapacity, int setLockLevel) {
		initElementData(initialCapacity);
    }
	
	private void initElementData(int initialCapacity) {
		if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        this.elementData = new Object[initialCapacity];
	}
	
	public AppendOnlyArrayList() {
        this.elementData = EMPTY_ELEMENTDATA;
    }
	
	public void append(E e) {
		try {
			lock.lock();
			ensureCapacityInternal(size + 1);
			UNSAFE.putOrderedObject(elementData, ((long)size() << OSHIFT) + OBASE, e);
			size++;
		} finally {
			lock.unlock();
		}
	}
	
	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        if (minCapacity - elementData.length > 0) {
        	grow(minCapacity);
        }
    }
	
	private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
	
	private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
	
	public void set(int index, E e) {
		rangeCheck(index);
		
		try {
			lock.lock();
			UNSAFE.putOrderedObject(elementData, ((long)index << OSHIFT) + OBASE, e);
		} finally {
			lock.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	public E get(int index) {
		rangeCheck(index);
		
		long u = (index << OSHIFT) + OBASE;
		final Object o = UNSAFE.getObjectVolatile(elementData, u);
		if (o != null) {
			return (E) o;
		}
		
		return null;
	}
	
	public boolean contains(E e) {
        return indexOf(e) >= 0;
    }
	
	public int indexOf(E e) {
        if (e == null) {
            for (int i = 0; i < size(); i++)
                if (null == get(i))
                    return i;
        } else {
            for (int i = 0; i < size(); i++)
                if (e.equals(get(i)))
                    return i;
        }
        return -1;
    }
	
	public int size() {
		return size;
	}
	
	public void clear() {
		try {
			lock.lock();
			size = 0;
		} finally {
			lock.unlock();
		}
	}
	
	private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
	
	// Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long OBASE;
    private static final int OSHIFT;

    static {
        int os;
        try {
            UNSAFE = Common.getUnsafe();
            @SuppressWarnings("rawtypes")
			Class oc = Object[].class;
            OBASE = UNSAFE.arrayBaseOffset(oc);
            os = UNSAFE.arrayIndexScale(oc);
        } catch (Exception e) {
            throw new Error(e);
        }
        if ((os & (os-1)) != 0)
            throw new Error("data type scale not a power of two");
        OSHIFT = 31 - Integer.numberOfLeadingZeros(os);
    }
}
