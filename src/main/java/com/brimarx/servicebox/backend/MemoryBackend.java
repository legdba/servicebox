package com.brimarx.servicebox.backend;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vincent on 04/08/15.
 */
public class MemoryBackend implements Backend {
    public long addAndGet(String id, long value) {
        long sum = 0;
        synchronized(sums) {
            if (sums.containsKey(id)) sum = sums.get(id);
            sum += value;
            sums.put(id, sum);
        }
        return sum;
    }

    private final Map<String, Long> sums = new HashMap<>();
}
