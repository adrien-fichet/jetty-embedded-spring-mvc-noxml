package com.sjl;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WebServer {

    public static interface WebContext {
        public File getWarPath();

        public String getContextPath();
    }

    private Server server;
    private WebServerConfig config;

    public WebServer(WebServerConfig aConfig) {
        config = aConfig;
    }

    public void start() throws Exception {
        server = new Server(createThreadPool());
        server.addConnector(createConnector(server));
        server.setHandler(createHandlers());
        server.setStopAtShutdown(true);
        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool _threadPool = new QueuedThreadPool();
        _threadPool.setName(config.getServerName());
        _threadPool.setMinThreads(config.getMinThreads());
        _threadPool.setMaxThreads(config.getMaxThreads());
        return _threadPool;
    }

    private ServerConnector createConnector(Server server) {
        ServerConnector _connector = new ServerConnector(server);
        _connector.setPort(config.getPort());
        _connector.setHost(config.getHostInterface());
        return _connector;
    }

    private HandlerCollection createHandlers() throws IOException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("com.sjl");

        WebAppContext contextHandler = new WebAppContext();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/");
        contextHandler.addEventListener(new ContextLoaderListener(context));
        contextHandler.setResourceBase(new ClassPathResource("META-INF/webapp").getURI().toString());

        List<Handler> _handlers = new ArrayList<Handler>();
        _handlers.add(contextHandler);

        HandlerList _contexts = new HandlerList();
        _contexts.setHandlers(_handlers.toArray(new Handler[0]));

        RequestLogHandler _log = new RequestLogHandler();
        _log.setRequestLog(createRequestLog());

        HandlerCollection _result = new HandlerCollection();
        _result.setHandlers(new Handler[]{_contexts, _log});

        return _result;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog _log = new NCSARequestLog();

        File _logPath = new File(config.getAccessLogDirectory() + "yyyy_mm_dd.request.log");
        _logPath.getParentFile().mkdirs();

        _log.setFilename(_logPath.getPath());
        _log.setRetainDays(30);
        _log.setExtended(false);
        _log.setAppend(true);
        _log.setLogTimeZone("UTC");
        _log.setLogLatency(true);
        return _log;
    }

}