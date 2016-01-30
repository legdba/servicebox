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
package com.brimarx.servicebox.services;


import com.brimarx.servicebox.backend.Backend;
import com.brimarx.servicebox.EmbededServer;
import com.brimarx.servicebox.model.FiboNthResult;
import com.brimarx.servicebox.model.Message;
import com.brimarx.servicebox.model.SumResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/calc")
@Api(value = "/calc", description = "Calculations")
public class CalcService {

    public static void setBackend(Backend be) { backend = be; } // TODO: fix this ugly hack and have proper injection setup

    @GET
    @Path("sum/{id}/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Statefull sum", notes = "Sum {value} to {id} counter in and return the new value. The data is stored in the instance memory by defaul( statefull) and can be set to Cassandra or Redis to emulate a stateless 12-factor behavior. Example: curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/calc/sum/0/1", response = SumResult.class)
    public SumResult sum(@PathParam("id") String id, @PathParam("value") int value)
    {
        long sum = CalcService.backend.addAndGet(id, value);
        logger.info("new sum for {} is {}", id, sum);
        return new SumResult(id, sum);
    }

    private long calcFiboNth(long num) {
        if (num > 2) {
            return calcFiboNth(num - 2) + calcFiboNth(num - 1);
        } else {
            return 1;
        }
    }

    @GET
    @Path("fibo-nth/{n}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Compute the n-th term of fibonacci", notes = "Compute the n-th term of fibonacci which is CPU intensive, expecially if {n} > 50. Example: curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/calc/fibo-nth/42", response = FiboNthResult.class)
    public FiboNthResult calcFiboNthRest(@PathParam("n") long n) {
        if (n > 0) {
            logger.info("calculating fibonacci Nth term for n={}", n);
            long x  = calcFiboNth(n);
            logger.info("calculated fibonacci Nth term for n={} : {}", n, x);
            return new FiboNthResult(n, x);
        } else {
            throw new javax.ws.rs.NotAcceptableException("n must be a positive integer");
        }
    }

    private static Backend backend;
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
}
