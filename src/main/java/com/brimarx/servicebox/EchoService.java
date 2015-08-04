package com.brimarx.servicebox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

/**
 * Echo REST service
 */
@Path("/")
public class EchoService
{
    @OPTIONS
    public String healthcheck() {
        return "up";
    }

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
