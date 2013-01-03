package com.jeraff.patricia;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.handler.CoreHandler;
import com.jeraff.patricia.handler.IndexHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatriciaServer {
    protected static final Logger log = Logger.getLogger(PatriciaServer.class.getCanonicalName());

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.severe("Shutting down now!");
            }
        });

        final Server server = new Server();
        final Config config = Config.instance(System.getProperties());
        final SelectChannelConnector connector0 = new SelectChannelConnector();

        config.configConnector(connector0);
        server.setConnectors(new Connector[]{connector0});

        final ContextHandlerCollection contexts = getContexts(config);
        for (ContextHandler handler : (ContextHandler[]) contexts.getHandlers()) {
            final Handler h = handler.getHandler();
            if (h instanceof CoreHandler) {
                // TODO: launch bootstraps here...
            }
        }

        server.setHandler(contexts);
        server.start();
        server.join();
    }

    private static ContextHandlerCollection getContexts(Config config) {
        final List<ContextHandler> contextHandlers = new ArrayList<ContextHandler>();
        final List<Core> cores = config.getCores();

        for (Core core : cores) {
            final ContextHandler apiHandler = new ContextHandler(core.getPath());
            apiHandler.setResourceBase(".");
            apiHandler.setHandler(new CoreHandler(core, config));
            apiHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
            contextHandlers.add(apiHandler);
        }

        if (config.isIndexHandler()) {
            final ContextHandler indexHandler = new ContextHandler("/");
            indexHandler.setResourceBase(".");
            indexHandler.setHandler(new IndexHandler(config));
            indexHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
            contextHandlers.add(indexHandler);
        }

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        final ContextHandler[] handlers = contextHandlers.toArray(new ContextHandler[]{});
        contexts.setHandlers(handlers);

        if (log.isLoggable(Level.INFO)) {
            for (int i = 0; i < handlers.length; i++) {
                ContextHandler ch = handlers[i];
                log.log(Level.INFO, "Handler configured at path: {0}", ch.getContextPath());
            }
        }

        return contexts;
    }
}
