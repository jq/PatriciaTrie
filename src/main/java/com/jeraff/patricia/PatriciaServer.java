package com.jeraff.patricia;

import com.jeraff.patricia.handler.rest.ApiHandler;
import com.jeraff.patricia.handler.webui.freemarker.WebHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

public class PatriciaServer {
    public static void main(String[] args) throws Exception {
        final PatriciaTrie<String, String> patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        final Server server = new Server(8666);

        final ContextHandler restHandler = new ContextHandler("/api");
        restHandler.setResourceBase(".");
        restHandler.setHandler(new ApiHandler(patriciaTrie));
        restHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        final ContextHandler webUIHandler = new ContextHandler("/webui");
        webUIHandler.setResourceBase(".");
        webUIHandler.setHandler(new WebHandler(patriciaTrie));
        webUIHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{restHandler, webUIHandler});

        server.setHandler(contexts);
        server.start();
        server.join();
    }
}
