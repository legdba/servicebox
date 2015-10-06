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
package com.brimarx.servicebox.backend.cassandra;

import com.brimarx.servicebox.backend.Backend;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

// CREATE KEYSPACE calc WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 } AND DURABLE_WRITES = false ;
// CREATE TABLE calc.sum (id varchar, sum counter, PRIMARY KEY(id)) ;
// UPDATE calc.sum SET sum=sum+1 WHERE id='0' ;
// SELECT * FROM calc.sum WHERE id='0' ;
// --be-type cassandra --be-opts '{"contactPoints":["52.88.93.64","52.89.85.132","52.89.133.153"], "authProvider":{"type":"PlainTextAuthProvider", "username":"username", "password":"password"}, "loadBalancingPolicy":{"type":"DCAwareRoundRobinPolicy", "localDC":"AWS_VPC_US_WEST_2"}}'
public class CassandraBackend implements Backend {

    public static final int DEFAULT_PORT = 9042;
    public static final String AUTHPROVIDER_PLAINTEXT = "PlainTextAuthProvider";
    public static final String LOADBALANCINGPOLICY_DCAWAREROUNDROBIN = "DCAwareRoundRobinPolicy";

    public CassandraBackend(CassandraConfig cfg) {
        Cluster.Builder builder = Cluster.builder();
        builder = withContactPoints(builder, cfg);
        builder = withAuthProvider(builder, cfg);
        builder = withLoadBalancingPolicy(builder, cfg);
        cluster = builder.build();

        logger.info("connecting to cluster at '{}'", cfg);
        session = cluster.connect("calc");

        logger.debug("preparing statements...");
        sumPS = session.prepare("UPDATE sum SET sum=sum+:value WHERE id=:id");
        getPS = session.prepare("SELECT * FROM calc.sum WHERE id=:id");

        logger.info("testing backend with a sum('0', 0) request");
        addAndGet("0", 0);
        logger.info("backend test passed");
    }

    public long addAndGet(String id, long value) {
        BoundStatement sumBS = new BoundStatement(sumPS);
        sumBS.setString("id", id);
        sumBS.setLong("value", value);
        session.execute(sumBS);

        BoundStatement getBS = new BoundStatement(getPS);
        getBS.setString("id", id);
        ResultSet results = session.execute(getBS);
        long sum = 0;
        for (Row row : results) {
            long v = row.getLong("sum");
            sum += v;
        }
        return sum;
    }

    @Override
    public String toString() {
        return session.toString();
    }

    private static Cluster.Builder withContactPoints(Cluster.Builder builder, CassandraConfig cfg) {
        Collection<InetSocketAddress> isas = new LinkedList<>();
        for (InetSocketAddress isa  : cfg.getContactPoints()) {
            if (isa.getPort() <= 0) isa = new InetSocketAddress(isa.getHostName(), DEFAULT_PORT);
            isas.add(isa);
        }
        builder.addContactPointsWithPorts(isas);
        return builder;
    }

    private static Cluster.Builder withAuthProvider(Cluster.Builder builder, CassandraConfig cfg) {
        if (cfg.getAuthProvider() != null) {
            if (AUTHPROVIDER_PLAINTEXT.equalsIgnoreCase(cfg.getAuthProvider().getType())) {
                String username = cfg.getAuthProvider().getUsername();
                String password = cfg.getAuthProvider().getPassword();
                return builder.withAuthProvider(new PlainTextAuthProvider(username, password));
            } else {
                throw new IllegalArgumentException("invalid Cassandra authProvider: " + cfg.getAuthProvider().getType());
            }
        } else {
            return builder;
        }
    }

    private static Cluster.Builder withLoadBalancingPolicy(Cluster.Builder builder, CassandraConfig cfg) {
        if (cfg.getLoadBalancingPolicy() != null) {
            if (LOADBALANCINGPOLICY_DCAWAREROUNDROBIN.equalsIgnoreCase(cfg.getLoadBalancingPolicy().getType())) {
                return builder.withLoadBalancingPolicy(new DCAwareRoundRobinPolicy(cfg.getLoadBalancingPolicy().getLocalDC()));
            } else {
                throw new IllegalArgumentException("invalid Cassandra loadBalancingPolicy: " + cfg.getLoadBalancingPolicy().getType());
            }
        } else {
            return builder;
        }
    }

    private final Cluster cluster;
    private final Session session;
    private final PreparedStatement sumPS;
    private final PreparedStatement getPS;

    private static final Logger logger = LoggerFactory.getLogger(CassandraBackend.class);
}
