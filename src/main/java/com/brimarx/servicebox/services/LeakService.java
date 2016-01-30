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

import com.brimarx.servicebox.model.RetainedHeap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Path("/leak")
@Api(value = "/leak", description = "Force a Heap leak")
public class LeakService {
    @GET
    @Path("/{size}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Leak {size} bytes", notes = "Leak {size} bytes and return the total number of cummulated leaked bytes. Example: curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/leak/1024", response = RetainedHeap.class)
    public RetainedHeap leak(@PathParam("size") int size)
    {
        leaks.add(ByteBuffer.allocate(size));
        long l = total.addAndGet(size);
        logger.info("leaked/total: {}/{} bytes", size, l);
        return new RetainedHeap(l);
    }

    @GET
    @Path("/free")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Free all leaked references", notes = "All leaked references will be subject to GC. curl -i -H 'Accept: application/json' http://192.168.59.103:8080/api/v2/leak/free", response = RetainedHeap.class)
    public RetainedHeap free()
    {
        leaks.clear();
        total.set(0);
        logger.info("released retained references");
        return new RetainedHeap(0);
    }

    private static final AtomicLong total = new AtomicLong();
    private static final List<ByteBuffer> leaks = new LinkedList<>();
    private static final Logger logger = LoggerFactory.getLogger(LeakService.class);
}
