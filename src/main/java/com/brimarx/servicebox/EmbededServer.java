package com.brimarx.servicebox;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EmbededServer
{
    public static void main(String[] args)
    {
        // Define default config
        int httpPort = 8080;
        String appLogLevel  = "info";
        String rootLogLevel = "warn";
        String logbackConfig = null;
        String beEndpoint = "127.0.0.1";

        // Create CLI parsing options
        Options options = new Options();
        Option helpOpt           = new Option("h", "help",            false, "display help"); options.addOption(helpOpt);
        Option httpPortOpt       = new Option("p", "port",            true,  "HTTP server port number; default to " + httpPort); options.addOption(httpPortOpt);
        Option appLogLevelOpt    = new Option("l", "loglevel",        true,  "application log level: debug, info, warn or error; default to " + appLogLevel); options.addOption(appLogLevelOpt);
        Option rootLogLevelOpt   = new Option(null,"srvloglevel",     true,  "http server log level: debug, info, warn or error; default to " + appLogLevel); options.addOption(rootLogLevelOpt);
        Option logbackConfigOpt  = new Option(null,"logconfig",       true,  "logback config file; disables other log options; uses internal preset config and CLI options by default"); options.addOption(logbackConfigOpt);
        Option beEndpointOpt     = new Option(null,"be-endpoint",     true,  "backend connectivity string; defaults to " + beEndpoint); options.addOption(beEndpointOpt);

        // Parse options
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.err.println(e.getMessage());
            (new HelpFormatter()).printHelp(EmbededServer.class.getName(), options);
            System.exit(1);
        }

        try
        {
            // Set options as per CLI options set
            if (cmd.hasOption(helpOpt.getLongOpt())) { (new HelpFormatter()).printHelp(EmbededServer.class.getName(), options); System.exit(0); }
            if (cmd.hasOption(logbackConfigOpt.getLongOpt())) logbackConfig = cmd.getOptionValue(logbackConfigOpt.getLongOpt());
            httpPort = Integer.parseInt(cmd.getOptionValue(httpPortOpt.getLongOpt(), Integer.toString(httpPort)));
            appLogLevel = cmd.getOptionValue(appLogLevelOpt.getLongOpt(), appLogLevel);
            rootLogLevel = cmd.getOptionValue(rootLogLevelOpt.getLongOpt(), rootLogLevel);
            beEndpoint = cmd.getOptionValue(beEndpointOpt.getValue(), beEndpoint);

            // Override logback default config with set options
            if (logbackConfig == null)
            {
                System.setProperty("LOGLEVEL_APP", appLogLevel);
                System.setProperty("LOGLEVEL_SRV", rootLogLevel);
            }
            else
            {
                initLogsFrom(logbackConfig);
            }
            logger = LoggerFactory.getLogger(EmbededServer.class);

            // We are done with init, now we can setup and start the server itself
            logger.info("server starting...");
            logger.info("info level enabled");
            logger.debug("debug level enabled");

            // Set backend connection
            be = new CassandraBackend(beEndpoint);

            // Expose the resources/webdav directory static content with / as a basedir
            ResourceHandler rh = new ResourceHandler();
            rh.setDirectoriesListed(true);rh.setWelcomeFiles(new String[]{"index.html"});
            java.net.URL webappResource = EmbededServer.class.getClassLoader().getResource("webapp");
            if (webappResource == null) throw new IllegalStateException("webapp resource not found");
            String webDir = webappResource.toExternalForm();
            rh.setResourceBase(webDir);

            // Create server
            final Server server = new Server(httpPort);
            ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            context.addServlet(createJAXRSServletHolder(CalcService.class, 1), "/calc/*");
            context.addServlet(createJAXRSServletHolder(LeakService.class, 2), "/leak/*");
            context.addServlet(createJAXRSServletHolder(EchoService.class, 3), "/echo/*");

            // Add both our JAX-RS service and static content to be served by the server
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { rh, context, new DefaultHandler() });
            server.setHandler(handlers);

            // Set graceful shutdown limited to 1sec
            server.setStopAtShutdown(true);
            server.setStopTimeout(1000);

            // Run the server
            server.start();
            logger.warn("server started; serving requests on port {} ...", httpPort);
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    try
                    {
                        logger.warn("SIGTERM, stopping server");
                        server.stop();
                    }
                    catch (Exception e)
                    {
                        logger.error("server stop failed", e);
                    }
                }
            });
            server.join();
            logger.warn("server stopped");
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }

    private static void initLogsFrom(String file) throws JoranException
    {
        File fn = new File(file);
        LoggerContext context  = (LoggerContext)LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(fn);
    }

    // Create the embeded service with JAX-RS interface enabled
    private static ServletHolder createJAXRSServletHolder(Class clazz, int order) {
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("jersey.config.server.provider.classnames", clazz.getCanonicalName());
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        sh.setInitOrder(order);
        return sh;
    }

    public static Backend be;
    private static Logger logger = null; // don't init statically to avoid slf4j init to occur before command line is read an log options set
}
