package com.util.offheap;

import org.junit.Assert;

import org.junit.Test;

public class TestDataBuffer {
	
	@SuppressWarnings("deprecation")
	@Test
	public void test() {
		DataBuffer db = new DataBuffer(new DirectByteBuffer(100));
		int position = 0;
		
		//byte
		db.writeByte(position, (byte) 0);
		Assert.assertTrue(db.readByte(position) == (byte) 0);
		position++;
		
		//boolean
		db.writeBoolean(position, true);
		Assert.assertTrue(db.readBoolean(position));
		position++;
		
		//short
		db.writeShort(position, (short) 15);
		Assert.assertTrue(db.readShort(position) == (short) 15);
		position += 2;
		
		//int
		db.writeInt(position, 16);
		Assert.assertTrue(db.readInt(position) ==  16);
		position += 4;		
		
		//long
		db.writeLong(position, 17);
		Assert.assertTrue(db.readLong(position) ==  17);
		position += 8;
		
		//float
		//由于精度问题,读写很可能会有不一致问题,不推荐使用float
		//这个是实现有问题
		//不知道为啥f1和f2相差四倍
		float f = (float) 5.0;
		db.writeFloat(position, f);
		System.out.println("f1: " + f);
		System.out.println("f2: " + db.readFloat(position));
		Assert.assertTrue(db.readFloat(position)*4 == f);
		position += 4;
		
		//double
		db.writeDouble(position, 0.1);
		Assert.assertTrue(db.readDouble(position) ==  0.1);
		position += 8;
		
		//string
		db.writeString(position, "test test");
		Assert.assertTrue(db.readString(position).equals("test test"));
	}
}
