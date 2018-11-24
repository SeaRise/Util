package com.util.buffer.bytebuffer;

import java.lang.reflect.Field;

import com.util.common.Common;

public class DataBufferBuilder {
	
	public static DataBuffer build(ByteBuffer buf) {
		if (buf instanceof DirectByteBuffer || buf instanceof HeapByteBuffer) {
			return new InlineDataBuffer(buf);
		} else {
			return new OutlineDataBuffer(buf);
		}
	}
	
	
	private static abstract class AbstractDataBuffer implements ByteBuffer {
		protected final ByteBuffer byteBuffer;
		/**
		 * 读写位置参考值,
		 * 每次read后readPosition = position+dataOffset
		 * 每次write后writePosition = position+dataOffset
		 * 方便顺序读取和顺序写入,可以不用
		 * */
		private int readPosition = 0;
		private int writePosition = 0;
		
		public AbstractDataBuffer(ByteBuffer byteBuffer) {
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

		public abstract ByteBuffer slice(int start, int end);

		public void free() {
			byteBuffer.free();
		}
		
		public int getReadPosition() {
			return readPosition;
		}

		public int getWritePosition() {
			return writePosition;
		}
		
		protected void setReadPosition(int read) {
			readPosition = read;
		}

		protected void setWritePosition(int write) {
			writePosition = write;
		}
		
		abstract public byte readByte(int position);
		abstract public void writeByte(int position, byte value);
		
		abstract public boolean readBoolean(int position);
		abstract public void writeBoolean(int position, boolean value);
		
		abstract public short readShort(int position);
		abstract public void writeShort(int position, short value);
		
		abstract public char readChar(int position);
		abstract public void writeChar(int position, char value);
		
		abstract public int readInt(int position);
		abstract public void writeInt(int position, int value);
		
		abstract public long readLong(int position);
		abstract public void writeLong(int position, long value);
		
		abstract public float readFloat(int position);
		abstract public void writeFloat(int position, float value);
		
		abstract public double readDouble(int position);
		abstract public void writeDouble(int position, double value);
		
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
			setReadPosition(position+4+length);
		    return new String(stringBuffer, 0, length);
		}
				
		public final void writeString(int position, String value) {
			byte[] b = value.getBytes();
			//stringBuffer没b大,就干脆用b,反正b也是要回收的,废物利用.
			stringBuffer = stringBuffer.length < b.length ? b : stringBuffer;
					
			writeInt(position, b.length);
			put(position+4, b, 0, b.length);
			setWritePosition(position+4+b.length);
		}
	}
	
	/*
	 * thread unsafe
	 * 
	 * 可以修饰ByteBuffer,提供基本类型和String读写的功能
	 * 实现方式参考了DataInputStream和DataOutputStream
	 * 
	 * */
	private static class OutlineDataBuffer extends AbstractDataBuffer implements DataBuffer {

		public OutlineDataBuffer(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
		
		//基本类型公用buffer
		private final byte[] buffer = new byte[8];
		
		//byte
		public final byte readByte(int position) {
			byteBuffer.get(position, buffer, 0, 1);
			setReadPosition(position+1);
	        return buffer[0];
	    }
		
		public final void writeByte(int position, byte value) {
			buffer[0] = value;
			byteBuffer.put(position, buffer, 0, 1);
			setWritePosition(position+1);
	    }
		
		//boolean
		public final boolean readBoolean(int position) {
			byteBuffer.get(position, buffer, 0, 1);
			setReadPosition(position+1);
	        return (buffer[0] != 0);
	    }
		
		public final void writeBoolean(int position, boolean value) {
			buffer[0] = (byte) (value ? 1 : 0);
			byteBuffer.put(position, buffer, 0, 1);
			setWritePosition(position+1);
	    }
		
		//short
		public final short readShort(int position) {
			byteBuffer.get(position, buffer, 0, 2);
			setReadPosition(position+2);
	        return (short)((buffer[0] << 8) + (buffer[1] << 0));
	    }
		
		public final void writeShort(int position, short value) {
			buffer[0] = (byte) ((value >>> 8) & 0xFF);
			buffer[1] = (byte) ((value >>> 0) & 0xFF);
	        byteBuffer.put(position, buffer, 0, 2);
	        setWritePosition(position+2);
	    }
		
		//char
		public final char readChar(int position) {
			byteBuffer.get(position, buffer, 0, 2);
			setReadPosition(position+2);
	        return (char)((buffer[0] << 8) + (buffer[1] << 0));
	    }
		
		public final void writeChar(int position, char value) {
			buffer[0] = (byte) ((value >>> 8) & 0xFF);
			buffer[1] = (byte) ((value >>> 0) & 0xFF);
	        byteBuffer.put(position, buffer, 0, 2);
	        setWritePosition(position+2);
	    }
		
		//int
		public final int readInt(int position) {
			byteBuffer.get(position, buffer, 0, 4);
			setReadPosition(position+4);
	        return ((buffer[0] << 24) + (buffer[1] << 16) + (buffer[2] << 8) + (buffer[3] << 0));
	    }
		
		public final void writeInt(int position, int value) {
			buffer[0] = (byte) ((value >>> 24) & 0xFF);
			buffer[1] = (byte) ((value >>> 16) & 0xFF);
			buffer[2] = (byte) ((value >>> 8) & 0xFF);
			buffer[3] = (byte) ((value >>> 0) & 0xFF);
	        byteBuffer.put(position, buffer, 0, 4);
	        setWritePosition(position+4);
	    }
		
		//long
		public final long readLong(int position) {
			byteBuffer.get(position, buffer, 0, 8);
			setReadPosition(position+8);
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
	        setWritePosition(position+8);
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

		@Override
		public ByteBuffer slice(int start, int end) {
			return new OutlineDataBuffer(byteBuffer.slice(start, end));
		}
	}
	
	/*
	 * thread unsafe
	 * 
	 * 用unsafe来减少内存拷贝,速度比OutlineDataBuffer快
	 * 
	 * */
	@SuppressWarnings("restriction")
	private static class InlineDataBuffer extends AbstractDataBuffer implements DataBuffer {

		private final Object baseObj;
		
		private final long address;
		
		private static final sun.misc.Unsafe unsafe = Common.getUnsafe();
		
		public InlineDataBuffer(ByteBuffer byteBuffer) {
			super(byteBuffer);
			if (byteBuffer instanceof DirectByteBuffer) {
				baseObj = null;
				address = ((DirectByteBuffer)byteBuffer).address();
			} else {//HeapByteBuffer
				try {
					Field field = HeapByteBuffer.class.getDeclaredField("byteBuffer");
					field.setAccessible(true);
					baseObj = field.get(byteBuffer);
					address = unsafe.arrayBaseOffset(byte[].class)+
							((HeapByteBuffer)byteBuffer).address();
				} catch (Exception e) {
					throw new RuntimeException("InlineDataBuffer init HeapByteBuffer error");
				} 
			}
		}

		public byte readByte(int position) {
			setReadPosition(position+1);
			return unsafe.getByte(baseObj, getPosition(position));
		}

		public void writeByte(int position, byte value) {
			setWritePosition(position+1);
			unsafe.putByte(baseObj, getPosition(position), value);
		}

		public boolean readBoolean(int position) {
			setReadPosition(position+1);
			return unsafe.getBoolean(baseObj, getPosition(position));
		}

		public void writeBoolean(int position, boolean value) {
			setWritePosition(position+1);
			unsafe.putBoolean(baseObj, getPosition(position), value);
		}

		public short readShort(int position) {
			setReadPosition(position+2);
			return unsafe.getShort(baseObj, getPosition(position));
		}

		public void writeShort(int position, short value) {
			setWritePosition(position+2);
			unsafe.putShort(baseObj, getPosition(position), value);
		}

		public char readChar(int position) {
			setReadPosition(position+2);
			return unsafe.getChar(baseObj, getPosition(position));
		}

		public void writeChar(int position, char value) {
			setWritePosition(position+2);
			unsafe.putChar(baseObj, getPosition(position), value);
		}

		public int readInt(int position) {
			setReadPosition(position+4);
			return unsafe.getInt(baseObj, getPosition(position));
		}

		public void writeInt(int position, int value) {
			setWritePosition(position+4);
			unsafe.putInt(baseObj, getPosition(position), value);
		}

		public long readLong(int position) {
			setReadPosition(position+8);
			return unsafe.getLong(baseObj, getPosition(position));
		}

		public void writeLong(int position, long value) {
			setWritePosition(position+8);
			unsafe.putLong(baseObj, getPosition(position), value);
		}

		public float readFloat(int position) {
			setReadPosition(position+4);
			return unsafe.getFloat(baseObj, getPosition(position));
		}

		public void writeFloat(int position, float value) {
			setWritePosition(position+4);
			unsafe.putFloat(baseObj, getPosition(position), value);
		}

		public double readDouble(int position) {
			setReadPosition(position+8);
			return unsafe.getDouble(baseObj, getPosition(position));
		}

		public void writeDouble(int position, double value) {
			setWritePosition(position+8);
			unsafe.putDouble(baseObj, getPosition(position), value);
		}

		@Override
		public ByteBuffer slice(int start, int end) {
			return new InlineDataBuffer(byteBuffer.slice(start, end));
		}
		
		private long getPosition(int position) {
			return address+position;
		}
	}
}
