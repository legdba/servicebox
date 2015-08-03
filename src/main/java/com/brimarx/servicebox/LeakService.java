package com.brimarx.servicebox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Path("/")
public class LeakService {
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

    private static final AtomicLong total = new AtomicLong();
    private static final List<ByteBuffer> leaks = new LinkedList<>();
    private static final Logger logger = LoggerFactory.getLogger(LeakService.class);
}
