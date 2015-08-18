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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

@Path("/calc")
public class CalcService {
    public static void setBackend(Backend be) { backend = be; } // TODO: fix this ugly hack and have proper injection setup

    @OPTIONS
    public String healthcheck() {
        return "up";
    }

    @GET
    @Path("add/{a}/{b}")
    @Produces("text/plain")
    public int add(@PathParam("a") int a, @PathParam("b") int b)
    {
        int c = a+b;
        logger.info("{}+{}={}", a, b, c);
        return c;
    }

    @GET
    @Path("sum/{id}/{value}")
    @Produces("text/plain")
    public long sum(@PathParam("id") String id, @PathParam("value") int value)
    {
        long sum = backend.addAndGet(id, value);
        logger.info("sum for {} is {}", id, sum);
        return sum;
    }

    private static Backend backend;
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
}
