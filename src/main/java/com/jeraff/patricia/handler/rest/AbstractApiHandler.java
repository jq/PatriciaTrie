package com.jeraff.patricia.handler.rest;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractApiHandler<K, V> extends AbstractHandler {
    public static final String CONTENT_TYPE_HTML = "text/html;charset=utf-8";

    protected PatriciaTrie<K, V> patriciaTrie;

    public AbstractApiHandler(PatriciaTrie<K, V> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest httpServletRequest,
                       HttpServletResponse response) throws IOException, ServletException {

        response.setContentType(CONTENT_TYPE_HTML);
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        response.getWriter().println(this.getClass().getCanonicalName());
    }
}
