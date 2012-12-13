package com.jeraff.patricia.handler.webui.freemarker;

import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebUIHandler extends BaseHandler {
    protected PatriciaTrie<String, String> patriciaTrie;

    public WebUIHandler(PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    }
}
