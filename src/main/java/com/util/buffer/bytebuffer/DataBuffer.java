package com.util.buffer.bytebuffer;




/*
 * thread unsafe
 * 多线程使用必须加synchronized
 * 
 * 可以修饰ByteBuffer,提供基本类型和String读写的功能
 * 实现方式参考了DataInputStream和DataOutputStream
 * 
 * 发现让stringBuffer和buffer合一的时候,string的读写就会出问题
 * 找到原因了,string写的时候会写入length
 * b.length > stringBuffer.length的时候,stringBuffer = b (b = string.getBytes)
 * 如果stringBuffer和buffer合一,write length的时候会污染b
 * */
public class DataBuffer implements ByteBuffer, DataOperator {

	private final ByteBuffer byteBuffer;
	
	/**
	 * 读写位置参考值,
	 * 每次read后readPosition = position+dataOffset
	 * 每次write后writePosition = position+dataOffset
	 * 方便顺序读取和顺序写入,可以不用
	 * */
	private int readPosition = 0;
	private int writePosition = 0;
	
	public DataBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}
	
	public void get(int position, byte[] destination, int offset, int length) {
		byteBuffer.get(position, destination, offset, length);
	}

	public void put(int position, byte[] source, int offset, int length) {
		byteBuffer.put(position, source, offset, length);
	}

	public int capacity() {
		return byteBuffer.capacity();
	}

	public ByteBuffer slice(int start, int end) {
		return new DataBuffer(byteBuffer.slice(start, end));
	}

	public void free() {
		byteBuffer.free();
		stringBuffer = null;
	}
	
	//基本类型公用buffer
	private final byte[] buffer = new byte[8];
	
	//byte
	public final byte readByte(int position) {
		byteBuffer.get(position, buffer, 0, 1);
		readPosition = position+1;
        return buffer[0];
    }
	
	public final void writeByte(int position, byte value) {
		buffer[0] = value;
		byteBuffer.put(position, buffer, 0, 1);
		writePosition = position+1;
    }
	
	//boolean
	public final boolean readBoolean(int position) {
		byteBuffer.get(position, buffer, 0, 1);
		readPosition = position+1;
        return (buffer[0] != 0);
    }
	
	public final void writeBoolean(int position, boolean value) {
		buffer[0] = (byte) (value ? 1 : 0);
		byteBuffer.put(position, buffer, 0, 1);
		writePosition = position+1;
    }
	
	//short
	public final short readShort(int position) {
		byteBuffer.get(position, buffer, 0, 2);
		readPosition = position+2;
        return (short)((buffer[0] << 8) + (buffer[1] << 0));
    }
	
	public final void writeShort(int position, short value) {
		buffer[0] = (byte) ((value >>> 8) & 0xFF);
		buffer[1] = (byte) ((value >>> 0) & 0xFF);
        byteBuffer.put(position, buffer, 0, 2);
        writePosition = position+2;
    }
	
	//char
	public final char readChar(int position) {
		byteBuffer.get(position, buffer, 0, 2);
		readPosition = position+2;
        return (char)((buffer[0] << 8) + (buffer[1] << 0));
    }
	
	public final void writeChar(int position, char value) {
		buffer[0] = (byte) ((value >>> 8) & 0xFF);
		buffer[1] = (byte) ((value >>> 0) & 0xFF);
        byteBuffer.put(position, buffer, 0, 2);
        writePosition = position+2;
    }
	
	//int
	public final int readInt(int position) {
		byteBuffer.get(position, buffer, 0, 4);
		readPosition = position+4;
        return ((buffer[0] << 24) + (buffer[1] << 16) + (buffer[2] << 8) + (buffer[3] << 0));
    }
	
	public final void writeInt(int position, int value) {
		buffer[0] = (byte) ((value >>> 24) & 0xFF);
		buffer[1] = (byte) ((value >>> 16) & 0xFF);
		buffer[2] = (byte) ((value >>> 8) & 0xFF);
		buffer[3] = (byte) ((value >>> 0) & 0xFF);
        byteBuffer.put(position, buffer, 0, 4);
        writePosition = position+4;
    }
	
	//long
	public final long readLong(int position) {
		byteBuffer.get(position, buffer, 0, 8);
		readPosition = position+8;
        return (((long)buffer[0] << 56) +
                ((long)(buffer[1] & 255) << 48) +
                ((long)(buffer[2] & 255) << 40) +
                ((long)(buffer[3] & 255) << 32) +
                ((long)(buffer[4] & 255) << 24) +
                ((buffer[5] & 255) << 16) +
                ((buffer[6] & 255) <<  8) +
                ((buffer[7] & 255) <<  0));
    }
	
	public final void writeLong(int position, long value) {
		buffer[0] = (byte) ((value >>> 56) & 0xFF);
		buffer[1] = (byte) ((value >>> 48) & 0xFF);
		buffer[2] = (byte) ((value >>> 40) & 0xFF);
		buffer[3] = (byte) ((value >>> 32) & 0xFF);
		buffer[4] = (byte) ((value >>> 24) & 0xFF);
		buffer[5] = (byte) ((value >>> 16) & 0xFF);
		buffer[6] = (byte) ((value >>> 8) & 0xFF);
		buffer[7] = (byte) ((value >>> 0) & 0xFF);
        byteBuffer.put(position, buffer, 0, 8);
        writePosition = position+8;
    }
	
	// float
	// 由于精度问题,读写很可能会有不一致问题,不推荐使用float
	// 这个是实现有问题
	// 不知道为啥读写的值相差四倍
	@Deprecated
	public final float readFloat(int position) {
        return Float.intBitsToFloat(readInt(position));
    }
	@Deprecated
	public final void writeFloat(int position, float value) {
        writeInt(position, Float.floatToIntBits(value));
    }
	
	//double
	public final double readDouble(int position) {
        return Double.longBitsToDouble(readLong(position));
    }
	
	public final void writeDouble(int position, double value) {
		writeLong(position, Double.doubleToLongBits(value));
    }
	
	//string专用buffer
	byte[] stringBuffer = new byte[0];
	
	//string
	public final String readString(int position) {
		int length = readInt(position);
		//stringBuffer不够大,扩容,扩容1.5倍防止反复扩容发生
		//不过扩容应该不会发生,因为writeString过程已经把stringBuffer替换成大的数组
		stringBuffer = stringBuffer.length < length ? 
				new byte[length + (length >> 1)] : stringBuffer;
		
		get(position+4, stringBuffer, 0, length);
		readPosition = position+4+length;
        return new String(stringBuffer, 0, length);
    }
	
	public final void writeString(int position, String value) {
		byte[] b = value.getBytes();
		//stringBuffer没b大,就干脆用b,反正b也是要回收的,废物利用.
		stringBuffer = stringBuffer.length < b.length ? b : stringBuffer;
		
		writeInt(position, b.length);
		put(position+4, b, 0, b.length);
		writePosition = position+4+b.length;
    }
	
	public int getReadPosition() {
		return readPosition;
	}

	public int getWritePosition() {
		return writePosition;
	}
}
