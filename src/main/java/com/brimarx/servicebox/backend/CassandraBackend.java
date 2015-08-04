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

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vincent on 04/08/15.
 */
// CREATE KEYSPACE calc WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 } AND DURABLE_WRITES = false ;
// CREATE TABLE calc.sum (id varchar, sum counter, PRIMARY KEY(id)) ;
// UPDATE calc.sum SET sum=sum+1 WHERE id='0' ;
// SELECT * FROM calc.sum WHERE id='0' ;
public class CassandraBackend implements Backend {
    public CassandraBackend(String contactPoint) {
        logger.info("connecting to cluster at '{}'", contactPoint);
        cluster = Cluster.builder().addContactPoint(contactPoint).build();
        session = cluster.connect("calc");

        logger.info("preparing statements");
        sumPS = session.prepare("UPDATE sum SET sum=sum+:value WHERE id=:id");
        getPS = session.prepare("SELECT * FROM calc.sum WHERE id=:id");
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

    private final Cluster cluster;
    private final Session session;
    private final PreparedStatement sumPS;
    private final PreparedStatement getPS;

    private static final Logger logger = LoggerFactory.getLogger(CassandraBackend.class);
}
