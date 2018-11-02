package com.util.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 分段锁
 * */
public class SegmentLock {
	
	private static final int DEFAULT_LOCK_LEVEL = 4;
	
	private Lock[] locks = null;
	
	public SegmentLock() {
		this(DEFAULT_LOCK_LEVEL);
	}
	
	public SegmentLock(int lockLevel) {
		if (!(lockLevel >= 1 && lockLevel <= 11)) {
            throw new IllegalArgumentException("lock level must be in {1..11}");
        }
        
        int lockSize = (int) Math.pow(2, lockLevel);
		locks = new ReentrantLock[lockSize];
		initLocks();
	}
	
	private void initLocks() {
		for (int i = 0; i < locks.length; i++) {
			locks[i] = new ReentrantLock();
		}
	}

	public void lock(int index) {
		getLock(index).lock();
	}

	public void unlock(int index) {
		getLock(index).unlock();
	}
	
	private Lock getLock(int index) {
		return locks[index&(locks.length-1)];
	}
}
