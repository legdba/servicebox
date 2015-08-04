package com.brimarx.servicebox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Path("/")
public class LeakService {
    @OPTIONS
    public String healthcheck() {
        return "up";
    }

    @GET
    @Path("/{size}")
    @Produces("text/plain")
    public String leak(@PathParam("size") int size)
    {
        leaks.add(ByteBuffer.allocate(size));
        long l = total.addAndGet(size);
        logger.info("leaked/total: {}/{} bytes", size, l);
        return String.format("leaked %d bytes", size);
    }

    @GET
    @Path("/free")
    @Produces("text/plain")
    public String free()
    {
        leaks.clear();
        total.set(0);
        logger.info("released retained references");
        return "flushed leaked references";
    }

    private static final AtomicLong total = new AtomicLong();
    private static final List<ByteBuffer> leaks = new LinkedList<>();
    private static final Logger logger = LoggerFactory.getLogger(LeakService.class);
}
