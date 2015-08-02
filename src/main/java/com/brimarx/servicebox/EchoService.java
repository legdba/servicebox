package com.brimarx.servicebox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
    @Path("echooo/{something}")
    @Produces("text/plain")
    public String delayedEcho(@PathParam("something") String something) throws InterruptedException
    {
        logger.debug("about to delayedEcho '{}'", something);
        synchronized(Thread.currentThread())
        {
            Thread.currentThread().wait(2000);
        }
        logger.info("delayedEcho '{}'", something);
        return something;
    }

    @GET
    @Path("ECHO/{something}")
    @Produces("text/plain")
    public String expensiveEcho(@PathParam("something") String something) throws InterruptedException
    {
        logger.debug("about to expensiveEcho '{}'", something);
        for (double val = 100000000; val > 0; val--)
        {
            Math.atan(Math.sqrt(Math.pow(val, 10)));
        }
        logger.info("expensiveEcho '{}'", something);
        return something;
    }

    private static final Logger logger = LoggerFactory.getLogger(EchoService.class);
}
