package com.util.offheap;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectByteBufferStore implements ByteBufferStore {
	
	private final Random random;
	
	private final DirectByteBufferPage[] imstores;
	
	private final ConcurrentHashMap<ByteBuffer, ByteBufferStore> storeMap = 
			new ConcurrentHashMap<ByteBuffer, ByteBufferStore>();
	
	private final int storeSize;
	
	//异步执行free的线程池,单线程排队进行就可以了,没必要多线程释放
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public DirectByteBufferStore(final int chunkSize, final int chunkCount, final int bufferSize) {
		random = new Random();
		imstores = new DirectByteBufferPage[bufferSize];
		storeSize = chunkSize*chunkCount;
		//整块分配堆外内存,目的是为了更好利用分级缓存机制
		ByteBuffer centralBuffer = new DirectByteBuffer(storeSize*bufferSize);
		for (int i = 0; i < bufferSize; i++) {
			imstores[i] = new DirectByteBufferPage(chunkSize, chunkCount, 
					centralBuffer.slice(i*storeSize, i*storeSize+storeSize));
		}
	}
	
	public ByteBuffer allocate(final int size) {
		//超过分配大小限制
		if (size > storeSize) {
			return null;
		}
		
		//随机选一个分配store分配
		int index = getIndex();
		ByteBuffer buf = doAllocate(index, size);
		if (null == buf) {
			//随机分配不成功,遍历分配
			for (index = 0; index < imstores.length; index++) {
				buf = doAllocate(index, size);
				if (null == buf) {
					continue;
				}
			}
		}
		if (index < imstores.length) {
			storeMap.put(buf, imstores[index]);
		}
		
		return buf;
	}
	
	//thread safe
	private ByteBuffer doAllocate(final int bufferIndex, final int size) {
		synchronized (imstores[bufferIndex]) {
			return imstores[bufferIndex].allocate(size);
		}
	}
	
	private int getIndex() {
		return random.nextInt(imstores.length);
	}

	public boolean free(final ByteBuffer byteBuffer) {
		ByteBufferStore store = storeMap.remove(byteBuffer);
		if (null == store) {
			return false;
		}
		//异步执行free
		executor.execute(new AsynFreeRunner(store, byteBuffer));
		return true;
		
	}

	public void close() {
		storeMap.clear();
		for (ByteBufferStore store : imstores) {
			store.close();
		}
	}
	
	//异步free执行类
	private class AsynFreeRunner implements Runnable {

		private final ByteBufferStore store;
		
		private final ByteBuffer byteBuffer;
		
		private AsynFreeRunner(ByteBufferStore store, ByteBuffer byteBuffer) {
			this.store = store;
			this.byteBuffer = byteBuffer;
		}
		
		public void run() {
			synchronized (store) {
				store.free(byteBuffer);
			}
		}
	}

}
