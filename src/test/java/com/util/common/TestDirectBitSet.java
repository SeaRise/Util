package com.util.common;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectBitSet {
	
	@Test
	public void test() {
		int num = 500000;
		DirectBitSet set = new DirectBitSet(num);
		//BitSet set = new BitSet(num);
		for (int i = 0; i < num; i++) {
			Assert.assertFalse(set.get(i));
			set.set(i);
			Assert.assertTrue(set.get(i));
		}
		for (int i = 0; i < num; i++) {
			Assert.assertTrue(set.get(i));
			set.clear(i);
			Assert.assertFalse(set.get(i));
		}
	}
}
