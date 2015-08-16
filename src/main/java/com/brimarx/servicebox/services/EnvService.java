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
@Path("/")
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
