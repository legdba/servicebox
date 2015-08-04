package com.brimarx.servicebox;

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
