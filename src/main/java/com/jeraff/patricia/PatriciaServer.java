package com.jeraff.patricia;

import com.jeraff.patricia.handler.GetHandler;
import com.jeraff.patricia.handler.PutHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class PatriciaServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8666);

        ContextHandler get = new ContextHandler();
        get.setContextPath("/get");
        get.setClassLoader(Thread.currentThread().getContextClassLoader());
        get.setHandler(new GetHandler());

        ContextHandler put = new ContextHandler();
        put.setContextPath("/put");
        put.setClassLoader(Thread.currentThread().getContextClassLoader());
        put.setHandler(new PutHandler());

        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        handlerCollection.setHandlers(new Handler[]{get, put});

        server.setHandler(handlerCollection);

        server.start();
        server.join();
    }

}
