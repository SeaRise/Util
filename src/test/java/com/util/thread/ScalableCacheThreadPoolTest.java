package com.util.thread;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class ScalableCacheThreadPoolTest {
    @Test
    public void test() throws InterruptedException {
        Set<Integer> set = new HashSet<>();
        Executor executor = new ScalableCacheThreadPool("test", 10);
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executor.execute(() -> {
                synchronized (set) {
                    set.add(finalI);
                }
                latch.countDown();
            });
        }
        latch.await();

        Preconditions.checkArgument(set.size() == 100);
        for (int i = 0; i < 100; i++) {
            set.remove(i);
        }
        Preconditions.checkArgument(set.isEmpty());
    }
}
