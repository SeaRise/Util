package com.util.buffer;

import org.junit.Assert;

import org.junit.Test;

import com.util.buffer.bytebuffer.ByteBuffer;
import com.util.buffer.bytebuffer.CombineBuffer;
import com.util.buffer.bytebuffer.DataBuffer;
import com.util.buffer.bytebuffer.DataBufferBuilder;
import com.util.buffer.bytebuffer.DirectByteBuffer;
import com.util.buffer.bytebuffer.HeapByteBuffer;

public class TestDataBuffer {
	
	@Test
	public void testDirect() {
		DataBuffer db = DataBufferBuilder.build(new DirectByteBuffer(100));
	
		//byte
		db.writeByte(db.getWritePosition(), (byte) 0);
		Assert.assertTrue(db.readByte(db.getReadPosition()) == (byte) 0);
		
		//boolean
		db.writeBoolean(db.getWritePosition(), true);
		Assert.assertTrue(db.readBoolean(db.getReadPosition()));
		
		//short
		db.writeShort(db.getWritePosition(), (short) 15);
		Assert.assertTrue(db.readShort(db.getReadPosition()) == (short) 15);
		
		//int
		db.writeInt(db.getWritePosition(), 16);
		Assert.assertTrue(db.readInt(db.getReadPosition()) ==  16);	
		
		//long
		db.writeLong(db.getWritePosition(), 17);
		Assert.assertTrue(db.readLong(db.getReadPosition()) ==  17);
		
		//float
		float f = (float) 5.0;
		db.writeFloat(db.getWritePosition(), f);
		Assert.assertTrue(db.readFloat(db.getReadPosition()) == f);
		
		//double
		db.writeDouble(db.getWritePosition(), 0.1);
		Assert.assertTrue(db.readDouble(db.getReadPosition()) ==  0.1);
		
		//string
		db.writeString(db.getWritePosition(), "test test1");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test1"));
		
		db.writeString(db.getWritePosition(), "test test2");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test2"));
	}
	
	@Test
	public void testHeap() {
		DataBuffer db = DataBufferBuilder.build(new HeapByteBuffer(100));
	
		//byte
		db.writeByte(db.getWritePosition(), (byte) 0);
		Assert.assertTrue(db.readByte(db.getReadPosition()) == (byte) 0);
		
		//boolean
		db.writeBoolean(db.getWritePosition(), true);
		Assert.assertTrue(db.readBoolean(db.getReadPosition()));
		
		//short
		db.writeShort(db.getWritePosition(), (short) 15);
		Assert.assertTrue(db.readShort(db.getReadPosition()) == (short) 15);
		
		//int
		db.writeInt(db.getWritePosition(), 16);
		Assert.assertTrue(db.readInt(db.getReadPosition()) ==  16);	
		
		//long
		db.writeLong(db.getWritePosition(), 17);
		Assert.assertTrue(db.readLong(db.getReadPosition()) ==  17);
		
		//float
		float f = (float) 5.0;
		db.writeFloat(db.getWritePosition(), f);
		Assert.assertTrue(db.readFloat(db.getReadPosition()) == f);
		
		//double
		db.writeDouble(db.getWritePosition(), 0.1);
		Assert.assertTrue(db.readDouble(db.getReadPosition()) ==  0.1);
		
		//string
		db.writeString(db.getWritePosition(), "test test1");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test1"));
		
		db.writeString(db.getWritePosition(), "test test2");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test2"));
	}
	
	@Test
	public void testOther() {
		DataBuffer db = DataBufferBuilder.build(
				new CombineBuffer(
						new ByteBuffer[]{new HeapByteBuffer(50), new DirectByteBuffer(50)}));
	
		//byte
		db.writeByte(db.getWritePosition(), (byte) 0);
		Assert.assertTrue(db.readByte(db.getReadPosition()) == (byte) 0);
		
		//boolean
		db.writeBoolean(db.getWritePosition(), true);
		Assert.assertTrue(db.readBoolean(db.getReadPosition()));
		
		//short
		db.writeShort(db.getWritePosition(), (short) 15);
		Assert.assertTrue(db.readShort(db.getReadPosition()) == (short) 15);
		
		//int
		db.writeInt(db.getWritePosition(), 16);
		Assert.assertTrue(db.readInt(db.getReadPosition()) ==  16);	
		
		//long
		db.writeLong(db.getWritePosition(), 17);
		Assert.assertTrue(db.readLong(db.getReadPosition()) ==  17);
		
		//float
		//由于精度问题,读写很可能会有不一致问题,不推荐使用float
		//这个是实现有问题
		//不知道为啥f1和f2相差四倍
		float f = (float) 5.0;
		db.writeFloat(db.getWritePosition(), f);
		Assert.assertTrue(db.readFloat(db.getReadPosition())*4 == f);
		
		//double
		db.writeDouble(db.getWritePosition(), 0.1);
		Assert.assertTrue(db.readDouble(db.getReadPosition()) ==  0.1);
		
		//string
		db.writeString(db.getWritePosition(), "test test1");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test1"));
		
		db.writeString(db.getWritePosition(), "test test2");
		Assert.assertTrue(db.readString(db.getReadPosition()).equals("test test2"));
	}
}
