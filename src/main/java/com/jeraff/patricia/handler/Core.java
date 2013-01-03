package com.jeraff.patricia.handler;

import com.google.gson.internal.StringMap;
import com.jeraff.patricia.analyzer.PartialMatchAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@JsonAutoDetect
public class Core {
    private String contextPath;
    private Class analyzer;

    private static final String KEY_ANALYZER = "analyzer";

    public Core(String contextPath) {
        analyzer = PartialMatchAnalyzer.class;
        setContextPath(contextPath);
    }

    public Core(String contextPath, StringMap<String> map) throws ClassNotFoundException {
        analyzer = Class.forName(map.get(KEY_ANALYZER));
        setContextPath(contextPath);
    }

    private void setContextPath(String contextPath) {
        final String s = String.format("/%s", StringUtils.strip(contextPath, "/"));
        this.contextPath = s;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Class getAnalyzer() {
        return analyzer;
    }

    private String makeUrl(String path) {
        final String strip = StringUtils.strip(contextPath, "/");
        if (StringUtils.isBlank(strip)) {
            return String.format("/%s/", StringUtils.strip(path, "/"));
        } else {
            return String.format("/%s/%s/", strip, StringUtils.strip(path, "/"));
        }
    }

    public String getAddUrl() {
        return makeUrl("add");
    }

    public String getApiUrl() {
        return makeUrl("api");
    }

    public String getStatusUrl() {
        return makeUrl("status");
    }

    @JsonIgnore
    public ObjectName getMBeanName() {
        String s = String.format("%s:Core=%s", getClass().getPackage().getName(), contextPath);
        try {
            return new ObjectName(s);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    @JsonProperty(value = "mBeanName")
    public String _getMBeanObjectName() {
        return getMBeanName().getCanonicalName();
    }

    public String canonicalName() {
        return contextPath.equals("/") ? "default" : StringUtils.strip(contextPath, "/");
    }
}
