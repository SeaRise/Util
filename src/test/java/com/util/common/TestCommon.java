package com.util.common;

import org.junit.Assert;
import org.junit.Test;

public class TestCommon {
	
static String test = null;
	
	static int num = 5000000;
	
	static {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append("0123456789");
		}
		test = sb.toString();
	}
	
	@Test
	public void test() {
		StringBuilder sb = Common.stringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append("0123456789");
		}
		String result = sb.toString();
		Assert.assertTrue(result.equals(test));
		
		sb = Common.stringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append("0123456789");
		}
		result = sb.toString();
		Assert.assertTrue(result.equals(test));
	}
}
