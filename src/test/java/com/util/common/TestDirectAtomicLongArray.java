package com.util.common;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectAtomicLongArray {
	@Test
	public void test() {
		DirectAtomicLongArray array = new DirectAtomicLongArray(100);
		array.set(5, 2L);
		Assert.assertEquals(2L, array.get(5));
		array.set(99, 3L);
		Assert.assertEquals(3L, array.get(99));
	}
}
