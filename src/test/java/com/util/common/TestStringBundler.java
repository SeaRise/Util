package com.util.common;

import org.junit.Assert;

import org.junit.Test;

import com.util.common.StringBundler;

/*
 * 只有在append进去的String的数量或大小大的情况下
 * StringBundler才比StringBuilder快
 * 鸡肋
 * */
public class TestStringBundler {
	
	static String test = null;
	
	static int num = 1000000;
	
	static {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append("0123456789");
		}
		test = sb.toString();
	}
	
	@Test
	public void test() {
		StringBundler bunlder = new StringBundler(num);
		for (int i = 0; i < num; i++) {
			bunlder.append("0123456789");
		}
		String result = bunlder.toString();
		Assert.assertTrue(result.equals(test));
		
		bunlder = new StringBundler(num);
		for (int i = 0; i < num; i++) {
			bunlder.append("0123456789");
		}
		result = bunlder.toString();
		Assert.assertTrue(result.equals(test));
	}
	
	@Test
	public void testStringBuilder() {
		StringBuilder bunlder = new StringBuilder(num);
		for (int i = 0; i < num; i++) {
			bunlder.append("0123456789");
		}
		String result = bunlder.toString();
		Assert.assertTrue(result.equals(test));
		
		bunlder = new StringBuilder(num);
		for (int i = 0; i < num; i++) {
			bunlder.append("0123456789");
		}
		result = bunlder.toString();
		Assert.assertTrue(result.equals(test));
	}
}
