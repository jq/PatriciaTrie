package com.jeraff.patricia;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.data.JbdcBootstrap;
import com.jeraff.patricia.handler.ApiHandler;
import com.jeraff.patricia.handler.WebHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class PatriciaServer {
    protected static final Logger log = Logger.getLogger(PatriciaServer.class.getPackage().getName());

    public static void main(String[] args) throws Exception {
        final PatriciaTrie<String, String> patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        final Server server = new Server();
        final Config config = new Config(System.getProperties());

        jdbcBootstrap(patriciaTrie, config);

        final SelectChannelConnector connector0 = new SelectChannelConnector();
        config.configConnector(connector0);

        final List<ContextHandler> contextHandlers = new ArrayList<ContextHandler>();
        final Collection<Core> cores = config.getCores();

        for (Core core : cores) {
            final ContextHandler apiHandler = new ContextHandler(core.createPath(ApiHandler.CONTEXT_PATH));
            apiHandler.setResourceBase(".");
            apiHandler.setHandler(new ApiHandler(patriciaTrie, config));
            apiHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

            final ContextHandler webHandler = new ContextHandler(core.createPath(WebHandler.CONTEXT_PATH));
            webHandler.setResourceBase(".");
            webHandler.setHandler(new WebHandler(patriciaTrie, config));
            webHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        }

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(contextHandlers.toArray(new Handler[]{}));

        server.setConnectors(new Connector[]{connector0});
        server.setHandler(contexts);
        server.start();
        server.join();
    }

    private static void jdbcBootstrap(PatriciaTrie<String, String> patriciaTrie, Config config) {
        new JbdcBootstrap(patriciaTrie, config).run();
    }
}
