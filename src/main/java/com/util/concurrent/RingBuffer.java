package com.util.concurrent;

import com.util.common.Common;

/*
 * 单线程读,单线程写
 * 不支持put null
 * 
 * 在数据量大的情况下可以发现,加了缓存行,速度明显加快
 * */
@SuppressWarnings("restriction")
public class RingBuffer<E> {
	
	private final static int DEFAULT_STORAGE_POWER = 10;
	private final int bufferSize;

	// 填充缓存行,
	@SuppressWarnings({ "unused" })
	private long p17, p18, p19, p20, p21, p22, p23;
	@SuppressWarnings("unused")
	private int p24;	
	
	// 读线程标志量
	private volatile int head = 0;
	
	// 填充缓存行,
	// 通常来讲，cache line是64Byte,所以head和tail之间填充7个long和1个int,
	// head和tail经常被两个线程修改,所以相邻head和tail的字段都用填充缓存行来隔开
	// 这样保证head和tail不在一个cache line上
	@SuppressWarnings({ "unused" })
	private long p1, p2, p3, p4, p5, p6, p7;
	@SuppressWarnings("unused")
	private int p8;
	
	// 写线程标志量
	private volatile int tail = 0;
	
	// 填充缓存行,
	@SuppressWarnings({ "unused" })
	private long p9, p10, p11, p12, p13, p14, p15;
	@SuppressWarnings("unused")
	private int p16;	
	
	private final Object[] buffer;
	
	
	public RingBuffer() {
		this(DEFAULT_STORAGE_POWER);
	}
	
	public RingBuffer(int storagePower) {
		if (!(storagePower >= 1 && storagePower <= 31)) {
            throw new IllegalArgumentException("storage power must be in {1..31}");
        }
		// 这里bufferSize采用2的次方
		// - 求余速度快,用index & (bufferSize-1)
		// - 更好利用多级缓存
		this.bufferSize = (int) Math.pow(2, storagePower);
		
		buffer = new Object[bufferSize];
	}
	
	private Boolean empty() {
		return head == tail;
	}
	
	//末尾位不写入,用来做full()的哨兵,
	private Boolean full() {
		return getIndex(tail + 1) == head;
	}
	
	public Boolean put(E e) {
		if (full() || null == e) {
			return false;
		}
		
		UNSAFE.putOrderedObject(buffer, ((long)tail << OSHIFT) + OBASE, e);
		tail = getIndex(tail + 1);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public E get() {
		if (empty()) {
			return null;
		}
		
		final Object o = UNSAFE.getObjectVolatile(buffer, (head << OSHIFT) + OBASE);
		head = getIndex(head + 1);
		return (E) o;
	}
	
	private int getIndex(int index) {
		return index & (bufferSize-1);
	}
	
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