package com.jeraff.patricia.conf;

import com.google.gson.internal.StringMap;
import com.jeraff.patricia.analyzer.PartialMatchAnalyzer;
import com.jeraff.patricia.util.GsonIgnore;
import org.apache.commons.lang.StringUtils;

public class Core {
    String contextPath;
    @GsonIgnore
    Class analyzerClass;
    String analyzer;

    private static final String KEY_ANALYZER = "analyzer";

    public Core(String contextPath) {
        analyzerClass = PartialMatchAnalyzer.class;
        analyzer = PartialMatchAnalyzer.class.getCanonicalName();
        setContextPath(contextPath);
    }

    public Core(String contextPath, StringMap<String> map) throws ClassNotFoundException {
        analyzerClass = Class.forName(map.get(KEY_ANALYZER));
        analyzer = map.get(KEY_ANALYZER);
        setContextPath(contextPath);
    }

    private void setContextPath(String contextPath) {
        final String s = String.format("/%s", StringUtils.strip(contextPath, "/"));
        this.contextPath = s;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Class getAnalyzerClass() {
        return analyzerClass;
    }
}
