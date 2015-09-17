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
package com.brimarx.servicebox;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.brimarx.servicebox.backend.BackendFactory;
import com.brimarx.servicebox.services.*;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

public class EmbededServer {
    public static void main(String[] args) {
        EmbededServer srv = new EmbededServer();
        try {
            JCommander jc = new JCommander(srv, args);
            if (srv.help) {
                jc.usage();
                System.exit(0);
            }
            srv.run();
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static final int    DEFAULT_HTTP_PORT         = 8080;
    private static final String DEFAULT_APP_LOG_LEVEL     = "info";
    private static final String DEFAULT_SRV_LOG_LEVEL     = "warn";
    private static final String DEFAULT_BE_OPTS_CASSANDRA = "127.0.0.1";

    @Parameter(names={"-h", "--help"}, description = "display help")
    private boolean help = false;

    @Parameter(names={"-p", "--port"}, description = "HTTP server port number; defaults to " + DEFAULT_HTTP_PORT)
    private int httpPort = DEFAULT_HTTP_PORT;

    @Parameter(names={"-l", "--log"}, description = "http server log level: debug, info, warn or error; defaults to  " + DEFAULT_APP_LOG_LEVEL)
    private String appLogLevel  = DEFAULT_APP_LOG_LEVEL;

    @Parameter(names={      "--logsrv"}, description = "application log level: debug, info, warn or error; defaults to  " + DEFAULT_SRV_LOG_LEVEL)
    private String rootLogLevel = DEFAULT_SRV_LOG_LEVEL;

    @Parameter(names={      "--srvloglevel"}, description = "logback config file; disables other log options; uses internal preset config and CLI options by default")
    private String logbackConfig = null;

    @Parameter(names={      "--be-type"}, description = "backend type; defaults to " + BackendFactory.TYPE_MEMORY + "; cassandra is supported as well and takes a node IP in the --be-endpoint param")
    private String beType     = BackendFactory.TYPE_MEMORY;

    @Parameter(names={      "--be-opts"}, description = "backend connectivity options; this depends on the --be-type value. 'memory' backend ignores this argument. 'cassandra' backend reads the cluster IP(s) there.")
    private String beEndpoint = null;

    @Parameter(names={      "--slowstart"}, description = "delay (in ms) before the server actually accept any connection, usefull to test that load-balancers are not including the service in pool before it is actually available; disabled by default")
    private int slowstart = 0;

    private void run() {
        try {
            initLogs();
            logger.info("logs initialized");
            logger.debug("debug enabled");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }

        try {
            initBackend();
            initJetty();
            slowstart();
            runJettyAndWait();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(2);
        }
    }

    private void initLogs() throws JoranException {
        // Override logback default config with set options
        if (logbackConfig == null) {
            System.setProperty("LOGLEVEL_APP", appLogLevel);
            System.setProperty("LOGLEVEL_SRV", rootLogLevel);
        } else {
            initLogsFrom(logbackConfig);
        }
        logger = LoggerFactory.getLogger(EmbededServer.class);
    }

    private void initLogsFrom(String file) throws JoranException {
        File fn = new File(file);
        LoggerContext context  = (LoggerContext)LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(fn);
    }

    private void initBackend() {
        if (BackendFactory.TYPE_CASSANDRA.equalsIgnoreCase(beType) && beEndpoint == null) beEndpoint= DEFAULT_BE_OPTS_CASSANDRA;
        CalcService.setBackend(BackendFactory.build(beType, beEndpoint));
    }

    private void initJetty() {
        // Create server
        server = new Server(httpPort);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Add Swagger
        initSwagger();

        // Add both our JAX-RS service and static content to be served by the server
        HandlerList handlers = new HandlerList();
        handlers.addHandler(buildSwaggerUI());
        handlers.addHandler(buildContext());
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        // Set graceful shutdown limited to 1sec
        server.setStopAtShutdown(true);
        server.setStopTimeout(1000);
    }

    private void runJettyAndWait() throws Exception {
        logger.info("server starting...");
        server.start();
        logger.warn("server started; serving requests on port {} ...", httpPort);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    logger.warn("SIGTERM, stopping server");
                    server.stop();
                } catch (Exception e) {
                    logger.error("server stop failed", e);
                }
            }
        });
        server.join();
        logger.warn("server stopped");
    }

    private void slowstart() throws InterruptedException {
        if (slowstart > 0) {
            logger.info("server start defered by {}ms (--slowstart option used)", slowstart);
            synchronized(this) {
                wait(slowstart);
            }
        }
    }

    private void initSwagger()
    {
        // This configures Swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion( "1.0.0" );
        beanConfig.setResourcePackage( EchoService.class.getPackage().getName() );
        beanConfig.setScan( true );
        beanConfig.setBasePath( "/" );
        beanConfig.setDescription( "REST server exposing test services" );
        beanConfig.setTitle( "ServiceBox-JAXRS" );
    }

    private ContextHandler buildContext()
    {
        ResourceConfig resourceConfig = new ResourceConfig();
        // io.swagger.jaxrs.listing loads up Swagger resources
        resourceConfig.packages(EchoService.class.getPackage().getName(), ApiListingResource.class.getPackage().getName());
        ServletContainer servletContainer = new ServletContainer( resourceConfig );
        ServletHolder sh = new ServletHolder( servletContainer );
        ServletContextHandler entityBrowserContext = new ServletContextHandler( ServletContextHandler.NO_SESSIONS );
        entityBrowserContext.setContextPath("/api/v2");
        entityBrowserContext.addServlet(sh, "/*");

        return entityBrowserContext;
    }

    private ContextHandler buildSwaggerUI()
    {
        try {
            final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
            swaggerUIResourceHandler.setResourceBase(EmbededServer.class.getClassLoader().getResource("swaggerui").toURI().toString());
            final ContextHandler swaggerUIContext = new ContextHandler();
            swaggerUIContext.setContextPath("/docs/");
            swaggerUIContext.setHandler(swaggerUIResourceHandler);
            return swaggerUIContext;
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private Server server;
    private static Logger logger = null; // don't init statically to avoid slf4j init to occur before command line is read an log options set
}
