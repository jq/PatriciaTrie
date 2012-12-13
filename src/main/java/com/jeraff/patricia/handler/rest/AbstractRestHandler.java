package com.jeraff.patricia.handler.rest;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractRestHandler<K, V> extends AbstractHandler {
    public static final String CONTENT_TYPE_HTML = "text/html;charset=utf-8";

    protected PatriciaTrie<K, V> patriciaTrie;

    public AbstractRestHandler(PatriciaTrie<K, V> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
    }

    public void handle(String target, Request request, HttpServletRequest httpServletRequest,
                       HttpServletResponse response) throws IOException, ServletException {

        response.setContentType(CONTENT_TYPE_HTML);
        response.setStatus(HttpServletResponse.SC_OK);
        request.setHandled(true);

        response.getWriter().println(this.getClass().getCanonicalName());
    }
}
