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
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RedisClusterBackend implements Backend {

    public static final int DEFAULT_PORT = 6379;

    public RedisClusterBackend(RedisConfig cfg) {
        logger.warn("{}", cfg);

        RedisURI.Builder builder = new RedisURI.Builder();
        for(InetSocketAddress isa : cfg.getContactPoints()) {
            if (isa.getPort() <= 0) isa = new InetSocketAddress(isa.getHostName(), DEFAULT_PORT);
            builder = builder.redis(isa.getHostName(), isa.getPort());
        }
        if (cfg.getPassword() != null) builder = builder.withPassword(cfg.getPassword());
        RedisURI uri = builder.build();
        cluster = RedisClusterClient.create(uri);
        cnx = cluster.connect();

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

    private RedisClusterClient cluster;
    private StatefulRedisClusterConnection<String,String> cnx;

    private static final Logger logger = LoggerFactory.getLogger(RedisClusterBackend.class);
}
