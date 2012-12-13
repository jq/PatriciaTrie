package com.jeraff.patricia.handler.webui.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.HashMap;

public abstract class BaseHandler extends AbstractHandler{
    Configuration freemarkerConfig;

    public BaseHandler() {
        freemarkerConfig = new Configuration();

        try {
            final URL resource = getClass().getResource("/ftl/");
            freemarkerConfig.setDirectoryForTemplateLoading(new File(resource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void renderTemplate(HttpServletResponse response, String template, HashMap<String, Object> rootMap) {
        StringWriter sw = new StringWriter();
        final Template t;
        try {
            t = freemarkerConfig.getTemplate(template);
            t.process(rootMap, sw);
            final String out = sw.toString().trim();
            response.getWriter().print(out);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try {
                e.printStackTrace(response.getWriter());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
