package com.sjl;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
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
        server = new Server();
        server.setThreadPool(createThreadPool());
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

    private SelectChannelConnector createConnector(Server server) {
        SelectChannelConnector _connector = new SelectChannelConnector();
        _connector.setPort(config.getPort());
        _connector.setHost(config.getHostInterface());
        return _connector;
    }

    private HandlerCollection createHandlers() {
        WebAppContext _ctx = new WebAppContext();
        _ctx.setContextPath("/");
        _ctx.setBaseResource(Resource.newClassPathResource("META-INF/webapp"));

        _ctx.setConfigurations(new Configuration[]{ new WebServerAnnotationConfiguration() });

        List<Handler> _handlers = new ArrayList<Handler>();
        _handlers.add(_ctx);

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