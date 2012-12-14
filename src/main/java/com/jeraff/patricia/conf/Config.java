package com.jeraff.patricia.conf;

import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
    public static final String CONNECTOR = "connector";
    public static final String CONNECTOR_PORT = "port";
    public static final int CONF_CONNECTOR_PORT_DEFAULT = 8666;

    private static final String PROP_CONFIG_FILE = "conf";

    private HashMap<String, Object> confMap;

    public Config(Properties properties) {
        setupDefaults();

        final String confFilePath = properties.getProperty(PROP_CONFIG_FILE);
        if (confFilePath != null) {
            handleConfFile(confFilePath, confMap);
        }
    }

    private void setupDefaults() {
        final HashMap<String, Object> connectorDefaults = new HashMap<String, Object>() {{
            put(CONNECTOR_PORT, CONF_CONNECTOR_PORT_DEFAULT);
        }};

        confMap = new HashMap<String, Object>();
        confMap.put(CONNECTOR, connectorDefaults);
    }

    private void handleConfFile(String confFilePath, HashMap<String, Object> confMap) {
        final File file = new File(confFilePath);
        if (!file.exists() || !file.canRead()) {
            throw new RuntimeException(confFilePath + " is not a readable file");
        }

        String json;
        try {
            final StringWriter stringWriter = new StringWriter();
            IOUtils.copy(new FileInputStream(file), stringWriter);
            json = stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Can't read " + confFilePath, e);
        }

        final HashMap hashMap = new Gson().fromJson(json, HashMap.class);
        confMap.putAll(hashMap);
    }

    public void configConnector(SelectChannelConnector connector) {
        final Object o = confMap.get(CONNECTOR);
        if (o == null || !(o instanceof Map)) {
            return;
        }

        try {
            BeanUtils.populate(connector, (Map) o);
        } catch (Exception e) {
            throw new RuntimeException("Could not configure connector");
        }
    }
}
