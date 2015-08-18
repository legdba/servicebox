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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by vincent on 16/08/15.
 */
@Path("/env")
public class EnvService {
    @GET
    @Path("/vars")
    @Produces("text/plain")
    public String vars() {
        Map<String,String> vars = System.getenv();
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Map.Entry<String,String> e : vars.entrySet()) {
            if (first) first = false;
            else sb.append("\r\n");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    @GET
    @Path("/vars/{name}")
    @Produces("text/plain")
    public String var(@PathParam("name") String name) {
        String val = System.getenv().get(name);
        if (val == null) throw new NotFoundException(name);
        return val;
    }

    @GET
    @Path("/hostname")
    @Produces("text/plain")
    public String hostanme() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    private static final Logger logger = LoggerFactory.getLogger(EnvService.class);
}
