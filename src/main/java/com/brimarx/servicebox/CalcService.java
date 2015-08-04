package com.brimarx.servicebox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/")
public class CalcService {
    @GET
    @Path("add/{a}/{b}")
    @Produces("text/plain")
    public int add(@PathParam("a") int a, @PathParam("b") int b)
    {
        int c = a+b;
        logger.info("{}+{}={}", a, b, c);
        return c;
    }

    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
}
