package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Core;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends ApiHandler {
    private HashMap<String, Core> cores;

    public ConfigHandler(List<Core> cores) {
        this.cores = new HashMap<String, Core>();
        for (Core c : cores) {
            this.cores.put(c.getPath(), c);
        }
    }

    public void setCores(HashMap<String, Core> cores) {
        this.cores = cores;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        writeApiResponse(request, response, new ApiMethodResult(cores));
        baseRequest.setHandled(true);
    }
}
