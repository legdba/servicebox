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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Path("/env")
@Api(value = "/env", description = "Display REST server environment")
public class EnvService {
    @GET
    @Path("/vars")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return all server ENV variables", notes = "Return variable as a map of string:string", response = Map.class)
    public Map<String,String> vars() {
        logger.info("returning env");
        return System.getenv();
    }

    @GET
    @Path("/vars/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return the server ENV value for variable {name}", notes = "Return as a map of string:string", response = Map.class)
    public Map<String,String> var(@PathParam("name") String name) {
        String val = System.getenv().get(name);
        if (val == null) throw new NotFoundException(name);
        Map<String,String> map = new HashMap<>();
        map.put(name, val);
        logger.info("returning env({})", name);
        return map;
    }

    @GET
    @Path("/hostname")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return the server hostname", notes = "Return InetAddress.getLocalHost().getHostName() value", response = Map.class)
    public Map<String,String> hostanme() throws UnknownHostException {
        Map<String,String> map = new HashMap<String,String>();
        map.put("hostname", InetAddress.getLocalHost().getHostName());
        logger.info("returning hostname");
        return map;
    }

    @GET
    @Path("/pid")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return JVM PID", notes = "Return JVM PID", response = Message.class)
    public Message pid() {
        String jmxCtx = ManagementFactory.getRuntimeMXBean().getName();
        int offset = jmxCtx.indexOf('@');
        if (offset > 0) {
            return new Message(jmxCtx.substring(0, offset));
        } else {
            return new Message(jmxCtx);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(EnvService.class);
}
