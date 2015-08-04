package com.brimarx.servicebox.backend;

/**
 * Created by vincent on 04/08/15.
 */
public class BackendFactory {
    public static final String TYPE_MEMORY    = "memory";
    public static final String TYPE_CASSANDRA = "cassandra";

    public static Backend build(String type, String connectivity) {
        if      (TYPE_MEMORY.equalsIgnoreCase(type)) return buildMemory();
        else if (TYPE_CASSANDRA.equalsIgnoreCase(type)) return buildCassandra(connectivity);
        throw new IllegalArgumentException("invalid backend type: " + type);
    }

    private static Backend buildMemory() {
        return new MemoryBackend();
    }

    private static Backend buildCassandra(String connectivity) {
        return new CassandraBackend(connectivity);
    }
}
