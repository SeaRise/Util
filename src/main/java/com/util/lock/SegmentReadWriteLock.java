package com.util.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SegmentReadWriteLock {
	
	private static final int DEFAULT_LOCK_LEVEL = 4;
	
	private final ReadWriteLock[] locks;
	
	public SegmentReadWriteLock() {
		this(DEFAULT_LOCK_LEVEL);
	}
	
	public SegmentReadWriteLock(int lockLevel) {
		if (!(lockLevel >= 1 && lockLevel <= 11)) {
            throw new IllegalArgumentException("lock level must be in {1..11}");
        }
        
        int lockSize = (int) Math.pow(2, lockLevel);
		locks = new ReentrantReadWriteLock[lockSize];
		initLocks();
	}
	
	private void initLocks() {
		for (int i = 0; i < locks.length; i++) {
			locks[i] = new ReentrantReadWriteLock();
		}
	}

	public Lock readLock(int index) {
		return getLock(index).readLock();
	}
	
	public Lock writeLock(int index) {
		return getLock(index).writeLock();
	}
	
	private ReadWriteLock getLock(int index) {
		return locks[index&(locks.length-1)];
	}
}
