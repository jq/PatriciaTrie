package com.jeraff.patricia;

import com.jeraff.patricia.handler.rest.ApiHandler;
import com.jeraff.patricia.handler.web.WebHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

public class PatriciaServer {
    public static void main(String[] args) throws Exception {
        final PatriciaTrie<String, String> patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        final Server server = new Server();

        final SelectChannelConnector connector0 = new SelectChannelConnector();
        connector0.setPort(8666);
        connector0.setMaxIdleTime(3000);
        connector0.setRequestHeaderSize(8192);
        connector0.setAcceptors(20);
        connector0.setAcceptQueueSize(100);
        connector0.setLowResourcesMaxIdleTime(5000);
        connector0.setLowResourcesConnections(100);

        final ContextHandler apiHandler = new ContextHandler(ApiHandler.CONTEXT_PATH);
        apiHandler.setResourceBase(".");
        apiHandler.setHandler(new ApiHandler(patriciaTrie));
        apiHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        final ContextHandler webHandler = new ContextHandler(WebHandler.CONTEXT_PATH);
        webHandler.setResourceBase(".");
        webHandler.setHandler(new WebHandler(patriciaTrie));
        webHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{apiHandler, webHandler});

        server.setConnectors(new Connector[]{connector0});
        server.setHandler(contexts);
        server.start();
        server.join();
    }
}
