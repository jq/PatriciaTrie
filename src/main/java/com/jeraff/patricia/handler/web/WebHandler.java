package com.jeraff.patricia.handler.web;

import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class WebHandler extends AbstractWebHandler {
    private static final String ACTION_INDEX = "";
    public static final String TEMPLATE_INDEX = "index.ftl";
    public static final String CONTEXT_PATH = "/webui";

    protected PatriciaTrie<String, String> patriciaTrie;

    public WebHandler(PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final String action = getAction(target);

        if (ACTION_INDEX.equals(action)) {
            handleIndex(request, response);
        } else {
            handle404(response);
        }
    }

    private void handleIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String out = renderTemplate(response, TEMPLATE_INDEX, new HashMap<String, Object>());
        final PrintWriter writer = response.getWriter();

        writer.print(out);
        writer.close();
    }

    private void handle404(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        final PrintWriter writer = response.getWriter();
        writer.print("Not Found");
        writer.close();
    }

    private String getAction(String target) {
        target.replaceFirst("/", "");
        if (target.endsWith("/")) {
            target = target.replaceAll("/$", "");
        }
        return target;
    }
}
