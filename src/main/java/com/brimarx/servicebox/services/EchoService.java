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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Echo REST service
 */
@Path("/echo")
public class EchoService
{
    @GET
    @Path("/{message}")
    @Produces(MediaType.APPLICATION_JSON)
    public Message echo(@PathParam("message") String message)
    {
        logger.info("echo '{}'", message);
        return new Message(message);
    }

    @GET
    @Path("/{message}/{delayms : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
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
