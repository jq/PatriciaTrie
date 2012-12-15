package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

public abstract class BaseHandler extends AbstractHandler {

    protected Configuration freemarkerConfig;
    protected final Config config;
    protected final PatriciaTrie<String, String> patriciaTrie;

    public BaseHandler(PatriciaTrie<String, String> patriciaTrie, Config config) {
        super();

        this.config = config;
        this.patriciaTrie = patriciaTrie;
        this.freemarkerConfig = new Configuration();

        try {
            final URL resource = getClass().getResource("/ftl/");
            freemarkerConfig.setDirectoryForTemplateLoading(new File(resource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String renderTemplate(HttpServletResponse response, String templateName, HashMap<String, Object> rootMap) {
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
