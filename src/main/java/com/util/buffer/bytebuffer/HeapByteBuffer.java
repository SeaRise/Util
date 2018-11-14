package com.util.buffer.bytebuffer;



/*
 * 堆内存bytebuffer
 * thread unsafe
 * **/
public class HeapByteBuffer implements ByteBuffer {

	private byte[] byteBuffer;
	
	/*
	 * HeapByteBuffer在byte[]中的起始偏移量
	 * */
	private final int address;
    
    private final int capacity;
	
	public HeapByteBuffer(int capacity) {
    	this(new byte[capacity]);
    }
    
    private HeapByteBuffer(byte[] buf) {
    	this(buf, 0, 0, buf.length);
    }
    
    //argument unchecked
    private HeapByteBuffer(byte[] buf,int address, int start, int end) {
    	this.byteBuffer = buf;
    	this.address = address+start;
    	this.capacity = end-start;
    }
	
	public void get(int position, byte[] destination, int offset, int length) {
		check(position, destination, offset, length);
		System.arraycopy(byteBuffer, getPosition(position), destination, offset, length);
	}

	public void put(int position, byte[] source, int offset, int length) {
		check(position, source, offset, length);
		System.arraycopy(source, offset, byteBuffer, getPosition(position), length);
	}

	public int capacity() {
		return capacity;
	}
	
	public int address() {
		return address;
	}

	public ByteBuffer slice(int start, int end) {
		if (start < 0 || end < 0 || start > end || 
    			start >= capacity || end > capacity) {
    		throw new IllegalArgumentException("illegal argument");
    	}
		
		return new HeapByteBuffer(this.byteBuffer, this.address, start, end);
	}
	
	private int getPosition(int position) {
        return address + position;
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

	public void free() {
		byteBuffer = null;
	}
	
}
