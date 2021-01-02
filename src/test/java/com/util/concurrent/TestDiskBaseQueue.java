package com.util.concurrent;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestDiskBaseQueue {

    @Test
    public void test() throws Exception {
        try (DiskBaseQueue<Integer> queue = new DiskBaseQueue<>()) {
            Set<Integer> set = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                queue.offer(i);
                set.add(i);
            }
            Preconditions.checkArgument(set.size() == 100);
            for (int i = 0; i < 100; i++) {
                set.remove(i);
            }
            Preconditions.checkArgument(set.isEmpty());
        }
    }
}
