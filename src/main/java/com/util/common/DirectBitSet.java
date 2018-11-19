package com.util.common;

import com.util.buffer.bytebuffer.DataBuffer;
import com.util.buffer.bytebuffer.DirectByteBuffer;

/*
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
	
	private final static int ADDRESS_BITS_PER_BTYE = 3;
	
	private final static int BIT_INDEX_MASK = 7;
	
	private static int byteIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_BTYE;
    }
	
	private final DataBuffer buf;
	private final int nbits;
	
	public DirectBitSet(int nbits) {
		this.nbits = nbits;
		this.buf = new DataBuffer(new DirectByteBuffer(byteIndex(nbits-1)+1));
	}
	
	public void set(int bitIndex) {
		checkBitIndex(bitIndex);
		
		int byteIndex = byteIndex(bitIndex);
		byte b = buf.readByte(byteIndex);
		b |= (1L << (bitIndex & BIT_INDEX_MASK));
		buf.writeByte(byteIndex, b);
	}
	
	public void clear(int bitIndex) {
		checkBitIndex(bitIndex);
		
		int byteIndex = byteIndex(bitIndex);
		byte b = buf.readByte(byteIndex);
		b &= ~(1L << (bitIndex & BIT_INDEX_MASK));
		buf.writeByte(byteIndex, b);
	}
	
	public boolean get(int bitIndex) {
		checkBitIndex(bitIndex);
		
		int byteIndex = byteIndex(bitIndex);
		byte b = buf.readByte(byteIndex);
		return (b & (1L << (bitIndex & BIT_INDEX_MASK))) != 0;
	}
	
	private void checkBitIndex(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= nbits) {
			throw new IndexOutOfBoundsException("bitIndex out of bounds: " + bitIndex);
		}
	}
}
