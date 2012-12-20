package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class IndexHandler extends BaseHandler {
    public IndexHandler(Config config) {
        this.config = config;
        setupFreemarker();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final HashMap<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("cores", config.getCores());
        final String s = renderTemplate(response, WebHandler.TEMPLATE_INDEX, rootMap);
        response.getWriter().print(s);
        baseRequest.setHandled(true);
    }
}
