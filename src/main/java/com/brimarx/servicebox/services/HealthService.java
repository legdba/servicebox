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

import com.brimarx.servicebox.model.Message;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Random;

@Path("/health")
@Api(value = "/health", description = "Health check service")
public class HealthService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return a 'up' message", notes = "Return a 'up' message. Example: curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/health", response = Message.class)
    public Message check()
    {
        logger.info("health(100%) -> up");
        return new Message("up");
    }

    @GET
    @Path("/{percentage}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return a 'up' message {percentage}% time and an HTTP error 503 otherwise. The {percentage} is a float from 0 (0%) to 1 (100%).", notes = "Return a 'up' message with a {percent}% chance, return an error otherwise. Example: curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/health/0.5", response = Message.class)
    public Message checkOrFail(@PathParam("percentage") double percentage)
    {
        double f = rand.nextDouble();
        if (f > percentage) {
            logger.info("health({}%) -> down", percentage*100);
            throw new javax.ws.rs.ServiceUnavailableException("health-check failed on purpose for testing");
        }
        else {
            logger.info("health({}%) -> up", percentage*100);
            return new Message("up");
        }
    }

    private static final Random rand = new Random(System.currentTimeMillis());
    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);
}
