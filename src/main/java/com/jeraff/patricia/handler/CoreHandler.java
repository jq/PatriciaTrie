package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CoreHandler extends BaseHandler {
    public static final String TARGET_API = "api";

    private final WebHandler web;
    private final ApiHandler api;

    public CoreHandler(Core core, Config config) {
        final PatriciaTrie<String, String> patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());

        this.web = new WebHandler(patriciaTrie, core, config);
        this.api = new ApiHandler(patriciaTrie, core, config);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (StringUtils.strip(target, "/").equals(TARGET_API)) {
            api.handle(target, baseRequest, request, response);
        } else {
            web.handle(target, baseRequest, request, response);
        }
    }
}
