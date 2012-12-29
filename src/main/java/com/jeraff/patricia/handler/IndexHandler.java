package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class IndexHandler extends BaseHandler {
    public static final String HEADER_LOCATION = "Location";
    public static final String CORES = "cores";

    public IndexHandler(Config config) {
        this.config = config;
        setupFreemarker();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final List<Core> cores = config.getCores();

        if (cores.size() == 1) {
            if (!response.isCommitted()) {
                response.sendRedirect(cores.get(0).getContextPath());
            }
        } else {
            final HashMap<String, Object> rootMap = new HashMap<String, Object>() {{
                put(CORES, cores);
            }};

            final String s = renderTemplate(response, WebHandler.TEMPLATE_INDEX, rootMap);
            response.getWriter().print(s);
        }

        baseRequest.setHandled(true);
    }
}
