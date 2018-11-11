package com.util.concurrent;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.util.common.Common;
import com.util.lock.SegmentLock;

/**
 * DAppendOnlyArrayList只允许追加和修改元素,不支持删除元素
 * 实现方式类似ConcurrentHashMap
 * 在AppendOnlyArrayList之上做的改进
 * 这种实现方式是失败的,add的元素不能被get立即可见,但是为什么会这样,想不通.
 * */
@SuppressWarnings("restriction")
public class DAppendOnlyArrayList<E> {
	
	private volatile transient Object[] elementData;
	
	private static final Object[] EMPTY_ELEMENTDATA = {};
	
	private static final int DEFAULT_CAPACITY = 10;
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	private final AtomicInteger size = new AtomicInteger(0);
	
	//add的时候的竞争字段
	private final AtomicInteger lastIndex = new AtomicInteger(0);
	
	/**原本打算用UNSAFE.getIntVolatile(elementData, OBASE+ARRAY_LENGTH_OFFSET);
	 * 但是发现会报错,说object[]这个类里没有length字段,也没找到数组对象的内存布局情况,所以就另外用一个值来保存capacity了
	 * */
	private volatile int capacity = 0;
	
	private final SegmentLock setLock;
	
	//写锁用来扩容
	//读锁用来和扩容过程阻塞
	private final ReentrantReadWriteLock addLock = new ReentrantReadWriteLock();
	
	public DAppendOnlyArrayList(int initialCapacity) {
		initElementData(initialCapacity);
        setLock = new SegmentLock();
    }
	
	public DAppendOnlyArrayList(int initialCapacity, int setLockLevel) {
		initElementData(initialCapacity);
        setLock = new SegmentLock(setLockLevel);
    }
	
	private void initElementData(int initialCapacity) {
		if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        this.elementData = new Object[initialCapacity];
        capacity = initialCapacity;
	}
	
	public DAppendOnlyArrayList() {
        this.elementData = EMPTY_ELEMENTDATA;
        setLock = new SegmentLock();
    }
	
	public void append(E e) {
		try {
			int index = lastIndex.incrementAndGet()-1;
			ensureCapacityInternal(lastIndex.get());
			// 这里为什么可以只加读锁,是因为
			// - ensureCapacityInternal保证了elementData的容量足够,所以只用竞争lastIndex这个字段
			// - 可以避免数组越界，看ensureCapacityInternal的注释
			addLock.readLock().lock();
			UNSAFE.putOrderedObject(elementData, ((long)index << OSHIFT) + OBASE, e);
		} finally {
			addLock.readLock().unlock();
		}
		size.incrementAndGet();
	}
	
	// 扩容条件是minCapacity >= capacity
	// 即是size >= capacity-1
	// 为何多出一位的空间,是为了防止A线程刚扩容完,结果其他线程把空间全部占完了，结果发生了数组越界。
	// 多出一位的空间,其他线程最多一直add,最少也会留下一位的空间,那么A线程可以利用这一位的空间,不会发生数组越界问题。
	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        if (minCapacity - capacity >= 0) {
        	try {
        		//两段检查
        		addLock.writeLock().lock();
        		if (minCapacity - capacity >= 0) {
        			grow(minCapacity);
        		}
        	} finally {
        		addLock.writeLock().unlock();
        	}
        }
    }
	
	private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = capacity;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        // 这里不会发生elementData被赋值成未初始化的object[]对象的情况
        // 因为elementData被voliate修饰了.防止指令重排序
        elementData = Arrays.copyOf(elementData, newCapacity);
        capacity = newCapacity;
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
			addLock.readLock().lock();
			setLock.lock(index);
			UNSAFE.putOrderedObject(elementData, ((long)index << OSHIFT) + OBASE, e);
		} finally {
			setLock.unlock(index);
			addLock.readLock().unlock();
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
		return size.get();
	}
	
	public int capacity() {
		return capacity;
	}
	
	private void rangeCheck(int index) {
        if (index >= size())
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



