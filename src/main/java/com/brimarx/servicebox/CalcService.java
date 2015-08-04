package com.brimarx.servicebox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

@Path("/")
public class CalcService {
    public CalcService(/*Backend be*/) {
        this.be = EmbededServer.be; // TODO: fix this ugly hack and have proper injection setup
        //this.be = be;
    }

    @OPTIONS
    public String healthcheck() {
        return "up";
    }

    @GET
    @Path("add/{a}/{b}")
    @Produces("text/plain")
    public int add(@PathParam("a") int a, @PathParam("b") int b)
    {
        int c = a+b;
        logger.info("{}+{}={}", a, b, c);
        return c;
    }

    @GET
    @Path("sum/{id}/{value}")
    @Produces("text/plain")
    public long sum(@PathParam("id") String id, @PathParam("value") int value)
    {
        long sum = be.addAndGet(id, value);
        logger.info("sum for {} is {}", id, sum);
        return sum;
    }

    private Backend be;
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
}
