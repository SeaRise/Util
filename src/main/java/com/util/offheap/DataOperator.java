package com.util.offheap;

//提供基本类型和string读写能力
public interface DataOperator {
	//byte
	byte readByte(int position);
		
	void writeByte(int position, byte value);
		
	//boolean
	boolean readBoolean(int position);
		
	void writeBoolean(int position, boolean value);
		
	//short
	short readShort(int position);
		
	void writeShort(int position, short value);
		
	//char
	char readChar(int position);
		
	void writeChar(int position, char value);
		
	//int
	int readInt(int position);
		
	void writeInt(int position, int value);
		
	//long
	long readLong(int position);
		
	void writeLong(int position, long value);
		
	//float
	float readFloat(int position);
		
	void writeFloat(int position, float value);
		
	//double
	double readDouble(int position);
		
	void writeDouble(int position, double value);
		
	//string
	String readString(int position);
		
	void writeString(int position, String value);
}
