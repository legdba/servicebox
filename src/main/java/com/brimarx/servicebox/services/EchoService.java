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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/echo")
@Api(value = "/echo", description = "Echo back received param")
public class EchoService
{
    @GET
    @Path("/{message}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return back message", notes = "Return back message", response = Message.class)
    public Message echo(@PathParam("message") String message)
    {
        logger.info("echo '{}'", message);
        return new Message(message);
    }

    // curl -X POST  -H "Accept: Application/json" -H "Content-Type: application/json" http://localhost:8080/api/v2/echo -d '{"message":"foo"}'
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return back message", notes = "Return back message", response = Message.class)
    public Message echo(Message message)
    {
        logger.info("echo '{}'", message.getMessage());
        return message;
    }

    @GET
    @Path("/{message}/{delayms : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return back message after {delayms} milliseconds", notes = "Return back message after {delayms} milliseconds", response = Message.class)
    public Message delayedEcho(@PathParam("message") String message, @PathParam("delayms") int delayms) throws InterruptedException
    {
        logger.debug("delaying echo by {} ms: {}", delayms, message);
        if (delayms > 0) {
            synchronized(Thread.currentThread()) {
                Thread.currentThread().wait(delayms);
            }
        }
        logger.debug("delayed echo by {} ms: {}", delayms, message);
        return new Message(message);
    }

    private static final Logger logger = LoggerFactory.getLogger(EchoService.class);
}
