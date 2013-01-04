package com.jeraff.patricia.server;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.handler.ConfigHandler;
import com.jeraff.patricia.server.handler.CoreHandler;
import com.jeraff.patricia.server.handler.IndexHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
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

        final List<Future> bootstrapFutures = new ArrayList<Future>();
        final ContextHandlerCollection contexts = getContexts(config);
        final ExecutorService pool = Executors.newFixedThreadPool(contexts.getHandlers().length);

        for (ContextHandler handler : (ContextHandler[]) contexts.getHandlers()) {
            if (handler.getHandler() instanceof CoreHandler) {
                final CoreHandler coreHandler = (CoreHandler) handler.getHandler();
                final FutureTask bootstrapFuture = coreHandler.getBootstrapFuture();
                if (bootstrapFuture != null) {
                    final Future<?> future = pool.submit(bootstrapFuture);
                    bootstrapFutures.add(future);
                }
            }
        }

        if (!bootstrapFutures.isEmpty()) {
            for (Future future : bootstrapFutures) {
                future.get();
            }
        }

        config.configConnector(connector0);
        server.setConnectors(new Connector[]{connector0});
        server.setHandler(contexts);
        server.start();
        server.join();
    }

    private static ContextHandlerCollection getContexts(Config config) {
        final List<ContextHandler> contextHandlers = new ArrayList<ContextHandler>();
        final List<Core> cores = config.getCores();

        // handler for each core
        for (Core core : cores) {
            final ContextHandler apiHandler = new ContextHandler(core.getPath());
            apiHandler.setResourceBase(".");
            apiHandler.setHandler(new CoreHandler(core, config));
            apiHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
            contextHandlers.add(apiHandler);
        }

        // handler for the config api
        final ContextHandler configHandler = new ContextHandler(config.getConfigContextPath());
        configHandler.setResourceBase(".");
        configHandler.setHandler(new ConfigHandler(cores));
        configHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        contextHandlers.add(configHandler);

        // we might need an index page too
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
