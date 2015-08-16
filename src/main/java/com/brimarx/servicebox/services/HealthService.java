package com.brimarx.servicebox.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Random;

/**
 * Created by vincent on 16/08/15.
 */
@Path("/")
public class HealthService {
    @GET
    @Path("check")
    @Produces("text/plain")
    public String check()
    {
        return "up";
    }

    @GET
    @Path("check/{percentage}")
    @Produces("text/plain")
    public String checkOrFail(@PathParam("percentage") double percentage)
    {
        double f = rand.nextDouble();
        logger.info("check with percentage={} and random={}", percentage, f);
        if (f > percentage) throw new IllegalStateException("health-check failed on purposed for testing");
        else return "up";
    }

    private static final Random rand = new Random(System.currentTimeMillis() * Runtime.getRuntime().freeMemory());
    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);
}
