package com.jeraff.patricia.server.handler;

import com.jeraff.patricia.server.bootstrap.DirectoryCat;
import com.jeraff.patricia.server.bootstrap.JDBC;
import com.jeraff.patricia.config.Config;
import com.jeraff.patricia.config.Core;
import com.jeraff.patricia.server.ops.PatriciaOps;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CoreHandler extends BaseHandler {
    public static final String TARGET_API = "api";

    private final WebHandler web;
    private final ApiHandler api;
    private final PatriciaTrie<String, String> patriciaTrie;

    public CoreHandler(Core core, Config config) {
        this.patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        this.patriciaTrieOps = new PatriciaOps(core, patriciaTrie);
        this.core = core;
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

    public FutureTask getBootstrapFuture() {
        if (core.getJdbc() != null) {
            return new FutureTask(new Callable() {
                @Override
                public Object call() throws Exception {
                    return new JDBC(core, patriciaTrieOps).bootstrap();
                }
            });
        }

        if (core.getDirCat() != null) {
            return new FutureTask(new Callable() {
                @Override
                public Object call() throws Exception {
                    return new DirectoryCat(core, patriciaTrieOps).bootstrap();
                }
            });
        }

        return null;
    }
}
