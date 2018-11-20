package com.util.common;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectBitSet {
	
	@Test
	public void test() {
		int num = Integer.MAX_VALUE;
		DirectBitSet set = new DirectBitSet(num);//49.165
		//BitSet set = new BitSet(num); 13.138
		for (int i = 0; i < num; i++) {
			Assert.assertFalse(set.get(i));
			set.set(i);
			Assert.assertTrue(set.get(i));
		}
	}
}
