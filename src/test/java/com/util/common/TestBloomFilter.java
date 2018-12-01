package com.util.common;

import org.junit.Assert;

import org.junit.Test;

import com.util.common.hash.BloomFilter;

public class TestBloomFilter {
	
	@Test
	public void test() {
		BloomFilter<String> filter = new BloomFilter<String>(10, 5);
		filter.put("test1");
		filter.put("test2");
		Assert.assertTrue(filter.mightContain("test1"));
		Assert.assertTrue(filter.mightContain("test2"));
		Assert.assertFalse(filter.mightContain("test3"));
	}	
}
