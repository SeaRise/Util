package com.util.offheap;

/*
 * thread unsafe
 * 可以组合多个byteBuffer,对外提供一个ByteBuffer的抽象
 * 使用slice()会抛错
 * */
public class CombineBuffer implements ByteBuffer {

	private final ByteBuffer[] buffers;
	
	private int capacity = 0;
	
	public CombineBuffer(ByteBuffer[] bs) {
		this.buffers = bs;
		for(ByteBuffer b : buffers) {
			capacity += b.capacity();
		}
	}
	/*
	 * ret[0] : startIndex
	 * ret[1] : startPosition
	 * 调用getStartIndexAndPosition的缓存,用来返回两个参数
	 * */
	private final int[] re = new int[2];
	
	public void get(int position, byte[] destination, int offset, int length) {
		check(position, destination, offset, length);
		getStartIndexAndPosition(re, position);
		
		for (int i = re[0]; i < buffers.length; i++) {
			int copyLength = buffers[i].capacity()-re[1];
			if (0 == length) {
				return;
			} else if (length < copyLength) {
				buffers[i].get(re[1], destination, offset, length);
				return;
			} else {
				buffers[i].get(re[1], destination, offset, copyLength);
				offset += copyLength;
				length -= copyLength;
				re[1] = 0;
			}
		}
	}
	
	public void put(int position, byte[] source, int offset, int length) {
		check(position, source, offset, length);
		getStartIndexAndPosition(re, position);
		
		for (int i = re[0]; i < buffers.length; i++) {
			int copyLength = buffers[i].capacity()-re[1];
			if (0 == length) {
				return;
			} else if (length < copyLength) {
				buffers[i].put(re[1], source, offset, length);
				return;
			} else {
				buffers[i].put(re[1], source, offset, copyLength);
				offset += copyLength;
				length -= copyLength;
				re[1] = 0;
			}
		}
	}
	
	//ret[0] : startIndex
	//ret[1] : startPosition
	private void getStartIndexAndPosition(int[] ret, int position) {
		ret[1] = 0;
		if (position < buffers[0].capacity()) {
			ret[0] = 0;
			ret[1] = position;
			return;
		}
		
		for (int i = 0; i < buffers.length; i++) {
			ret[1] += buffers[i].capacity();
			if (ret[1] > position) {
				ret[1] = position - (ret[1] - buffers[i].capacity());
				ret[0] = i;
				return;
			}
		}
		
		throw new IllegalArgumentException("out of buffer capacity");
	}
	
	/*
     * 检查越界
     * */
    private void check(int position, byte[] b, int offset, int length) {
    	if (position < 0 || offset < 0 || length < 0) {
    		throw new IllegalArgumentException("illegal argument");
    	}
    	
    	if ((position+length) > capacity()) {
    		throw new IllegalArgumentException("out of buffer capacity");
    	}
    	
    	if ((offset+length) > b.length) {
    		throw new IllegalArgumentException("out of array capacity");
    	}
    }

	public int capacity() {
		return capacity;
	}

	public ByteBuffer slice(int start, int end) {
		throw new RuntimeException("CombineBuffer can not slice!");
	}

	public void free() {
		for(ByteBuffer b : buffers) {
			b.free();
		}
	}

}
