package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.ops.PatriciaOps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseHandler extends AbstractHandler {

    protected Configuration freemarkerConfig;
    protected Config config;
    protected Core core;
    protected PatriciaOps patriciaTrieOps;
    protected static final Logger log = Logger.getLogger(BaseHandler.class.getCanonicalName());

    protected static final HashMap<SerializationConfig.Feature, Boolean> defaultMapperConfig;
    protected static final ObjectMapper objectMapper;

    static {
        defaultMapperConfig = new HashMap<SerializationConfig.Feature, Boolean>();
        defaultMapperConfig.put(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        defaultMapperConfig.put(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        defaultMapperConfig.put(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        defaultMapperConfig.put(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        defaultMapperConfig.put(SerializationConfig.Feature.USE_ANNOTATIONS, true);
        defaultMapperConfig.put(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        defaultMapperConfig.put(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);

        objectMapper = new ObjectMapper();
        for (Map.Entry<SerializationConfig.Feature, Boolean> entry : defaultMapperConfig.entrySet()) {
            objectMapper.configure(entry.getKey(), entry.getValue());
        }
    }

    protected BaseHandler() {
    }

    public BaseHandler(PatriciaTrie<String, String> patriciaTrie, Core core, Config config) {
        super();
        this.core = core;
        this.config = config;
        this.patriciaTrieOps = new PatriciaOps(core, patriciaTrie);
        setupFreemarker();
    }

    protected void setupFreemarker() {
        try {
            this.freemarkerConfig = new Configuration();
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

            rootMap.put("core", core);
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
