package com.jeraff.patricia.conf;

import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.*;

public class Config {
    public static final String CONNECTOR = "connector";
    public static final String CONNECTOR_PORT = "port";
    public static final int CONF_CONNECTOR_PORT_DEFAULT = 8666;

    private static final String PROP_CONFIG_FILE = "conf";
    public static final String PARTRICIA_PROP_PREFIX = "partricia.";

    private HashMap<String, Object> confMap;

    public Config(Properties properties) {
        confMap = new HashMap<String, Object>();
        setupDefaults(confMap);

        final String confFilePath = properties.getProperty(PROP_CONFIG_FILE);
        if (confFilePath != null) {
            handleConfFile(confFilePath, confMap);
        }

        handleSystemProperties(properties, confMap);
    }

    private void handleSystemProperties(Properties properties, HashMap<String, Object> confMap) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object key = entry.getKey();
            if (!(key instanceof String)) {
                continue;
            }

            String name = (String) key;
            if (!name.startsWith(PARTRICIA_PROP_PREFIX)) {
                continue;
            }

            final Object value = entry.getValue();
            final List<String> strings = new ArrayList<String>(Arrays.asList(name.split("\\.")));
            final Iterator<String> iterator = strings.iterator();
            iterator.next();
            iterator.remove();

            Map<String, Object> node = confMap;
            while (iterator.hasNext()) {
                final String next = iterator.next();

                if (iterator.hasNext()) {
                    if (!node.containsKey(next)) {
                        node.put(next, new HashMap<String, Object>());
                    }

                    node = (Map<String, Object>) node.get(next);
                } else {
                    node.put(next, value);
                }

                iterator.remove();
            }
        }
    }

    private void setupDefaults(HashMap<String, Object> confMap) {
        final HashMap<String, Object> connectorDefaults = new HashMap<String, Object>() {{
            put(CONNECTOR_PORT, CONF_CONNECTOR_PORT_DEFAULT);
        }};

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
