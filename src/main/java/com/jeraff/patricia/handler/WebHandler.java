package com.jeraff.patricia.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.util.Method;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class WebHandler extends BaseHandler {
    private static final String ACTION_INDEX = "";
    private static final String ACTION_ADD = "add";
    private String ACTION_STATUS = "status";

    public static final String TEMPLATE_AUTO_COMPLETE = "autocomplete.ftl";
    public static final String TEMPLATE_STATUS = "status.ftl";
    public static final String TEMPLATE_ADD = "add.ftl";
    public static final String TEMPLATE_INDEX = "index.ftl";

    protected WebHandler() {

    }

    public WebHandler(PatriciaTrie<String, String> patriciaTrie, Core core, Config config) {
        super(patriciaTrie, core, config);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final String action = getAction(target);

        if (ACTION_INDEX.equals(action)) {
            handleIndex(request, response);
        } else if (ACTION_ADD.equals(action)) {
            handleAdd(request, response);
        } else if (ACTION_STATUS.equals(action)) {
            handleStatus(request, response);
        } else {
            handle404(response);
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HashMap<String, Object> rootMap = new HashMap<String, Object>();

        if (Method.valueOf(request.getMethod()) == Method.POST) {
            final Params params = new Params(request);

            try {
                params.validate(Method.POST);

                final HashMap<String, ArrayList<String>> put = patriciaTrieOps.put(params.getStrings());
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                rootMap.put("resultJson", gson.toJson(put));
                rootMap.put("success", true);
            } catch (ParamValidationError paramValidationError) {
                rootMap.put("success", false);
                rootMap.put("error", paramValidationError.getMessage());
            }
        }

        final String out = renderTemplate(response, TEMPLATE_ADD, rootMap);
        final PrintWriter writer = response.getWriter();

        writer.print(out);
        writer.close();
    }

    private void handleIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String out = renderTemplate(response, TEMPLATE_AUTO_COMPLETE, new HashMap<String, Object>());
        final PrintWriter writer = response.getWriter();

        writer.print(out);
        writer.close();
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HashMap<String, Object> rootMap = new HashMap<String, Object>();
        final int size = patriciaTrieOps.size();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        final Date dateUp = new Date(config.getTime());

        rootMap.put("size", size);
        rootMap.put("upSec", (System.currentTimeMillis() - config.getTime()) / 1000L);
        rootMap.put("upAgo", ago(dateUp));
        rootMap.put("upDate", sdf.format(dateUp));
        rootMap.put("config", config.getConfigFileContent());

        if (size != 0) {
            rootMap.put("firstKey", patriciaTrieOps.firstKey());
            rootMap.put("lastKey", patriciaTrieOps.lastKey());
        }

        final String out = renderTemplate(response, TEMPLATE_STATUS, rootMap);
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
        target = target.replaceFirst("/", "");
        if (target.endsWith("/")) {
            target = target.replaceAll("/$", "");
        }
        return target;
    }

    static String ago(Date date) {
        final Date now = new Date();
        if (now.before(date)) {
            return "";
        }

        long delta = (now.getTime() - date.getTime()) / 1000;
        if (delta < 30) {
            return "just now";
        }

        if (delta < 60) {
            return "1 minute";
        }

        if (delta < 60 * 60) {
            long minutes = delta / 60;
            return String.format("%d minute%s", minutes, pluralize(minutes));
        }

        if (delta < 24 * 60 * 60) {
            long hours = delta / (60 * 60);
            return String.format("%d hour%s", hours, pluralize(hours));
        }

        if (delta < 30 * 24 * 60 * 60) {
            long days = delta / (24 * 60 * 60);
            return String.format("%d day%s", days, pluralize(days));
        }

        if (delta < 365 * 24 * 60 * 60) {
            long months = delta / (30 * 24 * 60 * 60);
            return String.format("%d month%s", months, pluralize(months));
        }

        long years = delta / (365 * 24 * 60 * 60);
        return String.format("%d year%s", years, pluralize(years));
    }

    static String pluralize(Number n) {
        long l = n.longValue();
        if (l != 1) {
            return "s";
        }
        return "";
    }
}
