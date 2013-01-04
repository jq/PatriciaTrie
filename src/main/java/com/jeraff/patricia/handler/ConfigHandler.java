package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Core;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends ApiHandler {
    private static final String TARGET_CORES = "cores";

    private HashMap<String, Core> cores;

    public ConfigHandler(List<Core> cores) {
        this.cores = new HashMap<String, Core>();
        for (Core c : cores) {
            this.cores.put(c.getPath(), c);
        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final ApiMethodResult apiMethodResult = new ApiMethodResult();

        if (StringUtils.strip(target, "/").equals(TARGET_CORES)) {
            apiMethodResult.setBody(cores);
        } else {
            apiMethodResult.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        writeApiResponse(request, response, apiMethodResult);
        baseRequest.setHandled(true);
    }
}
