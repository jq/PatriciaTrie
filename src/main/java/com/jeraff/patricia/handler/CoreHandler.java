package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CoreHandler extends BaseHandler {
    public static final String TARGET_API = "api";

    private final WebHandler web;
    private final ApiHandler api;

    public CoreHandler(PatriciaTrie<String, String> patriciaTrie, Config config) {
        web = new WebHandler(patriciaTrie, config);
        api = new ApiHandler(patriciaTrie, config);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (target.equals(TARGET_API)) {
            api.handle(target, baseRequest, request, response);
        } else {
            web.handle(target, baseRequest, request, response);
        }
    }
}
