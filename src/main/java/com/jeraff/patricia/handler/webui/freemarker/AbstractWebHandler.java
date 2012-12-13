package com.jeraff.patricia.handler.webui.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.HashMap;

public abstract class AbstractWebHandler extends AbstractHandler{
    protected Configuration freemarkerConfig;

    public AbstractWebHandler() {
        freemarkerConfig = new Configuration();

        try {
            final URL resource = getClass().getResource("/ftl/");
            freemarkerConfig.setDirectoryForTemplateLoading(new File(resource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String renderTemplate(HttpServletResponse response, String templateName, HashMap<String, Object> rootMap) {
        try {
            final Template template;
            final StringWriter sw = new StringWriter();

            template = freemarkerConfig.getTemplate(templateName);
            template.process(rootMap, sw);

            return sw.toString().trim();
        } catch (Exception e) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(pw);

            return sw.toString();
        }
    }
}
