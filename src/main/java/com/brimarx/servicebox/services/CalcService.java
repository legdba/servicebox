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
import com.brimarx.servicebox.model.SumResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/calc")
public class CalcService {

    public static void setBackend(Backend be) { backend = be; } // TODO: fix this ugly hack and have proper injection setup

    @GET
    @Path("sum/{id}/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public SumResult sum(@PathParam("id") String id, @PathParam("value") int value)
    {
        long sum = CalcService.backend.addAndGet(id, value);
        logger.info("new sum for {} is {}", id, sum);
        return new SumResult(id, sum);
    }

    private static Backend backend;
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
}
