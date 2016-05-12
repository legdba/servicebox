/**
 ##############################################################
 # Licensed to the Apache Software Foundation (ASF) under one
 # or more contributor license agreements.  See the NOTICE file
 # distributed with this work for additional information
 # regarding copyright ownership.  The ASF licenses this file
 # to you under the Apache License, Version 2.0 (the
 # "License"); you may not use this file except in compliance
 # with the License.  You may obtain a copy of the License at
 #
 #   http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing,
 # software distributed under the License is distributed on an
 # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 # KIND, either express or implied.  See the License for the
 # specific language governing permissions and limitations
 # under the License.
 ##############################################################
 */
package com.brimarx.servicebox.backend;

import com.brimarx.servicebox.backend.cassandra.CassandraBackend;
import com.brimarx.servicebox.backend.cassandra.CassandraConfig;
import com.brimarx.servicebox.backend.redis.RedisClusterBackend;
import com.brimarx.servicebox.backend.redis.RedisSentinelBackend;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class BackendFactory {
    public static final String TYPE_MEMORY        = "memory";
    public static final String TYPE_CASSANDRA     = "cassandra";
    public static final String TYPE_REDISCLUSTER  = "redis-cluster";
    public static final String TYPE_REDISSENTINEL = "redis-sentinel";

    public static final String DEFAULT_BE_OPTS_CASSANDRA = "{\"contactPoints\":[\"localhost:9042\"]}";
    public static final String DEFAULT_BE_OPTS_REDIS_CLUSTER = "redis://localhost:6379";
    public static final String DEFAULT_BE_OPTS_REDIS_SENTINEL = "redis-sentinel://localhost:6379?sentinelMasterId=mymaster";

    public static Backend build(String type, String connectivity) {
        if      (TYPE_MEMORY.equalsIgnoreCase(type)) return buildMemory();
        else if (TYPE_CASSANDRA.equalsIgnoreCase(type)) return buildCassandra(connectivity);
        else if (TYPE_REDISCLUSTER.equalsIgnoreCase(type)) return buildRedisCluster(connectivity);
        else if (TYPE_REDISSENTINEL.equalsIgnoreCase(type)) return buildRedisSentinel(connectivity);
        throw new IllegalArgumentException("invalid backend type: " + type);
    }

    private static Backend buildMemory() {
        return new MemoryBackend();
    }

    private static Backend buildCassandra(String connectivity) {
        try {
            if (connectivity == null || connectivity.trim().isEmpty()) connectivity = DEFAULT_BE_OPTS_CASSANDRA;
            ObjectMapper om = new ObjectMapper();
            CassandraConfig cfg = om.readValue(connectivity, CassandraConfig.class);
            return new CassandraBackend(cfg);
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid backend connectivity '" + connectivity + "' : " + e, e);
        }
    }

    private static Backend buildRedisCluster(String connectivity) {
        if (connectivity == null || connectivity.trim().isEmpty()) connectivity = DEFAULT_BE_OPTS_REDIS_CLUSTER;
        return new RedisClusterBackend(connectivity);
    }

    private static Backend buildRedisSentinel(String connectivity) {
        if (connectivity == null || connectivity.trim().isEmpty()) connectivity = DEFAULT_BE_OPTS_REDIS_SENTINEL;
        return new RedisSentinelBackend(connectivity);
    }
}
