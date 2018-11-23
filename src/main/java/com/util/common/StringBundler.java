package com.util.common;

import java.lang.reflect.Constructor;

/*
 * 鸡肋作品,只有在append的数量超大的情况大才有有意义
 * 不然就比StringBuilder慢
 * */
public class StringBundler {
	
	private final static Constructor<String> stringConstructor;
	static {
		try {
			stringConstructor = String.class.getDeclaredConstructor(char[].class, boolean.class);
			stringConstructor.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("StringBundler class init error!");
		} 
	}
	
	private static String constructString(char[] value) {
		try {
			return stringConstructor.newInstance(value, true);
		} catch (Exception e) {
			throw new RuntimeException("construct string error!");
		} 
	}
	
	private final String[] strsHolder;
	private int length = 0;
	private int index = 0;
	
	public StringBundler(int strNum) {
		strsHolder = new String[strNum];
	}
	
	public StringBundler append(String str) {
		if (str == null) str = "null";
        strsHolder[index++] = str;
        length += str.length();
        return this;
    }
	
	@Override
	public String toString() {
		char[] value = new char[length];
		int begin = 0;
		for (String str : strsHolder) {
			str.getChars(0, str.length(), value, begin);
			begin += str.length();
		}
		return constructString(value);
	}
}
