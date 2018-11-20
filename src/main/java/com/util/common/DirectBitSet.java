package com.util.common;


/* Thread safe
 * 堆外内存位图
 * 
 * 应该算是失败的作品,
 * 速度和BitSet相比慢好多
 * 大部分的开销应该是在堆外内存读写数据上
 * 
 * 原先的想法是将BitSet放在堆内,GC开销会大一些
 * 所以把BitSet放在堆外,不收GC管理,减小GC开销
 * 但是如果两个在速度上差太多,那这个的意义就不大了.
 * */
public class DirectBitSet {
	
	private final static int ADDRESS_BITS_PER_WORD = 6;
	
	private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }
	
	private final DirectAtomicLongArray words;
	
	public DirectBitSet(int nbits) {
		words = new DirectAtomicLongArray(wordIndex(nbits-1) + 1);
	}
	
	public void set(int bitIndex) {
		if (get(bitIndex)) {
	        return;
	    }
		
		int wordsIndex = wordIndex(bitIndex);
		long mask = 1L << bitIndex;
		
		
		long oldValue;
	    long newValue;
	    do {
	        oldValue = words.get(wordsIndex);
	        newValue = oldValue | mask;
	    } while (!words.compareAndSet(wordsIndex, oldValue, newValue));
	}
	
	public boolean get(int bitIndex) {
		return (words.get(wordIndex(bitIndex)) & (1L << bitIndex)) != 0;
	}
}
