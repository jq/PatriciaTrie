package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.ops.PatriciaOps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseHandler extends AbstractHandler {

    protected Configuration freemarkerConfig;
    protected final Config config;
    protected final PatriciaOps patriciaTrieOps;
    protected static final Logger log = Logger.getLogger(BaseHandler.class.getPackage().getName());

    public BaseHandler(PatriciaTrie<String, String> patriciaTrie, Config config) {
        super();

        this.config = config;
        this.freemarkerConfig = new Configuration();
        this.patriciaTrieOps = new PatriciaOps(patriciaTrie);

        try {
            freemarkerConfig.setClassForTemplateLoading(BaseHandler.class, "/ftl");
        } catch (Exception e) {
            final String err = "Can't configure freemarker";
            final RuntimeException rte = new RuntimeException(err, e);

            log.log(Level.SEVERE, err, e);
            throw rte;
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

            log.log(Level.SEVERE, "Problem rendering ftl template: " + templateName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(pw);

            return sw.toString();
        }
    }
}
