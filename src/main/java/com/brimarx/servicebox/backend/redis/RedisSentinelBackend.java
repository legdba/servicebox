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
package com.brimarx.servicebox.backend.redis;

import com.brimarx.servicebox.backend.Backend;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisSentinelAsyncConnection;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

public class RedisSentinelBackend implements Backend {

    public RedisSentinelBackend(String cfg) {
        logger.info("connecting to {}", cfg);
        redisClient = RedisClient.create(cfg);
        cnx = redisClient.connect();

        logger.info("testing backend with a sum('0', 0) request...");
        addAndGet("0", 0);
        logger.info("backend test passed");
    }

    @Override
    public long addAndGet(String id, long value) {
        StringBuilder k = new StringBuilder();
        k.append("servicebox:calc:sum:").append(id);
        long newValue = cnx.sync().incrby(k.toString(), value);
        return newValue;
    }

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> cnx;

    private static final Logger logger = LoggerFactory.getLogger(RedisSentinelBackend.class);
}
