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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

/**
 * Echo REST service
 */
@Path("/")
public class EchoService
{
    @GET
    @Path("echo/{something}")
    @Produces("text/plain")
    public String echo(@PathParam("something") String something)
    {
        logger.info("echo '{}'", something);
        return something;
    }

    @GET
    @Path("echooo/{something}/{delayms}")
    @Produces("text/plain")
    public String delayedEcho(@PathParam("something") String something, @PathParam("delayms") int delayms) throws InterruptedException
    {
        logger.debug("delayedEcho sleeping '{}'ms", delayms);
        if (delayms > 0) {
            synchronized(Thread.currentThread()) {
                Thread.currentThread().wait(delayms);
            }
        }
        logger.info("delayedEcho '{}' after {}ms", something, delayms);
        return something;
    }

    @GET
    @Path("ECHO/{something}")
    @Produces("text/plain")
    public String expensiveEcho(@PathParam("something") String something)
    {
        logger.debug("about to expensiveEcho '{}'", something);
        for (double val = 100000000; val > 0; val--) {
            Math.atan(Math.sqrt(Math.pow(val, 10)));
        }
        logger.info("expensiveEcho '{}'", something);
        return something;
    }

    private static final Logger logger = LoggerFactory.getLogger(EchoService.class);
}
