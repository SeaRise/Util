package com.util.common.hash;

import com.google.common.hash.Hashing;

/*
 * thread safe
 * 
 * 堆外内存的布隆过滤器
 * */
public class BloomFilter<T> {
	
	private final DirectBitSet bits;
	
	private final int numHashFunctions;
	
	private final int mask;
	
	public BloomFilter(int bitsLevel, int numHashFunctions) {
		if (!(bitsLevel >= 1 && bitsLevel <= 31)) {
            throw new IllegalArgumentException("bits level must be in {1..31}");
        }
        
		mask = (int) Math.pow(2, bitsLevel)-1;
        bits = new DirectBitSet(mask+1);
		this.numHashFunctions = numHashFunctions;
	}
	
	public boolean mightContain(T object) {
		long hash64 = Hashing.murmur3_128().hashInt(object.hashCode()).asLong();
		int hash1 = (int) hash64;
	    int hash2 = (int) (hash64 >>> 32);
	    for (int i = 1; i <= numHashFunctions; i++) {
	    	int combinedHash = hash1 + (i * hash2);
	        if (combinedHash < 0) {
	          combinedHash = ~combinedHash;
	        }
	        if (!bits.get(combinedHash & mask)) {
	          return false;
	        }
	    }
	    return true;
	}
	
	public void put(T object) {
		long hash64 = Hashing.murmur3_128().hashInt(object.hashCode()).asLong();
		int hash1 = (int) hash64;
	    int hash2 = (int) (hash64 >>> 32);
	    for (int i = 1; i <= numHashFunctions; i++) {
	    	int combinedHash = hash1 + (i * hash2);
	        if (combinedHash < 0) {
	        	combinedHash = ~combinedHash;
	        }
	        bits.set(combinedHash & mask);
	    }
	}
}
