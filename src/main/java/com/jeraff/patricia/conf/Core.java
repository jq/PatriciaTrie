package com.jeraff.patricia.conf;

import com.jeraff.patricia.analyzer.PartialMatchAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@JsonAutoDetect
public class Core {
    private String path = "/";
    private Class analyzer = PartialMatchAnalyzer.class;

    public Core() {
    }

    private void setPath(String path) {
        final String s = String.format("/%s", StringUtils.strip(path, "/"));
        this.path = s;
    }

    public String getPath() {
        return path;
    }

    public Class getAnalyzer() {
        return analyzer;
    }

    private String makeUrl(String path) {
        final String strip = StringUtils.strip(this.path, "/");
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
        String s = String.format("%s:Core=%s", getClass().getPackage().getName(), path);
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
        return path.equals("/") ? "default" : StringUtils.strip(path, "/");
    }
}
