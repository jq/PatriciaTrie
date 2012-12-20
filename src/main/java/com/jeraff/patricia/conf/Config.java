package com.jeraff.patricia.conf;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    protected static final Logger log = Logger.getLogger(Config.class.getCanonicalName());

    public static final String CONNECTOR = "connector";
    public static final String CONNECTOR_ACCEPTORS = "acceptors";
    public static final String CONNECTOR_PORT = "port";
    public static final int CONNECTOR_PORT_DEFAULT = 8666;

    private static final String CORES = "cores";

    public static final String JDBC = "jdbc";
    public static final String JDBC_TABLE = "table";
    public static final String JDBC_COLUMN_STRING = "s";
    public static final String JDBC_COLUMN_ORDER = "orderby";
    public static final String JDBC_URL = "url";

    public static final String PARTRICIA_PROP_PREFIX = "patricia.";
    public static final String PROP_CONFIG_FILE = PARTRICIA_PROP_PREFIX + "conf";

    private HashMap<String, Object> confMap;
    private long time;
    private HashMap<String, Core> cores = new HashMap<String, Core>();
    private final String confFilePath;

    public Config(Properties properties) {
        confMap = new HashMap<String, Object>();
        time = System.currentTimeMillis();

        setupDefaults(confMap);

        confFilePath = properties.getProperty(PROP_CONFIG_FILE);
        if (confFilePath != null) {
            handleConfFile(confFilePath, confMap);
        }

        handleSystemProperties(properties, confMap);
        setupCores();
    }

    public String getConfigFileContent() throws IOException {
        if (confFilePath == null) {
            return "";
        }

        FileInputStream in = null;

        try {
            in = new FileInputStream(new File(confFilePath));
            InputStreamReader inR = new InputStreamReader(in);
            BufferedReader buf = new BufferedReader(inR);
            List<String> lines = new ArrayList<String>();
            String line;

            while ((line = buf.readLine()) != null) {
                lines.add(line);
            }

            return StringUtils.join(lines, "\n");
        } finally {
            if (in != null) {
                in.close();
            }
        }
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
            put(CONNECTOR_PORT, CONNECTOR_PORT_DEFAULT);
        }};

        confMap.put(CONNECTOR, connectorDefaults);
    }

    private void handleConfFile(String confFilePath, HashMap<String, Object> confMap) {
        final File file = new File(confFilePath);
        if (!file.exists() || !file.canRead()) {
            String s = confFilePath + " is not a readable file";
            log.log(Level.SEVERE, s);
            throw new RuntimeException(s);
        }

        String json;
        try {
            final StringWriter stringWriter = new StringWriter();
            IOUtils.copy(new FileInputStream(file), stringWriter);
            json = stringWriter.toString();
        } catch (Exception e) {
            String error = "Can't read " + confFilePath;
            log.log(Level.SEVERE, error, e);
            throw new RuntimeException(error, e);
        }

        final HashMap hashMap = new Gson().fromJson(json, HashMap.class);
        confMap.putAll(hashMap);
    }

    public void configConnector(SelectChannelConnector connector) {
        final Object o = confMap.get(CONNECTOR);
        if (o == null || !(o instanceof Map)) {
            return;
        }

        if (!((Map) o).containsKey(CONNECTOR_ACCEPTORS)) {
            ((Map) o).put(CONNECTOR_ACCEPTORS, 2 * Runtime.getRuntime().availableProcessors());
        }

        try {
            BeanUtils.populate(connector, (Map) o);
        } catch (Exception e) {
            String error = "Could not configure connector";
            log.log(Level.SEVERE, error, e);
            throw new RuntimeException(error, e);
        }
    }

    public long getTime() {
        return time;
    }

    /////////////////////////////////////////////////////////////////
    // cores
    /////////////////////////////////////////////////////////////////
    private void setupCores() {
        final String err = "Invalid core configuration";
        final Object rawCoreConfig = confMap.get(CORES);

        if (rawCoreConfig == null) {
            return;
        } else if (!(rawCoreConfig instanceof StringMap)) {
            log.log(Level.SEVERE, err);
            throw new RuntimeException(err);
        }

        StringMap<StringMap> coreConfig = (StringMap<StringMap>) rawCoreConfig;
        for (Map.Entry<String, StringMap> entry : coreConfig.entrySet()) {
            try {
                final Core core = new Core(entry.getKey(), entry.getValue());
                cores.put(core.contextPath, core);
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, err);
                throw new RuntimeException(err);
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // jdbc stuff
    /////////////////////////////////////////////////////////////////
    public boolean hasJdbc() {
        return confMap.containsKey(JDBC);
    }

    public Connection getJdbcConnection() {
        if (!hasJdbc()) {
            log.log(Level.WARNING, "No JDBC configuration available");
            return null;
        }

        try {
            final Map<String, Object> jdbcInfo = (Map<String, Object>) confMap.get(JDBC);
            jdbcInfo.put(JDBC_COLUMN_STRING, jdbcInfo.get(JDBC_COLUMN_STRING));

            final Properties properties = new Properties();
            properties.putAll(jdbcInfo);

            return DriverManager.getConnection((String) jdbcInfo.get(JDBC_URL), properties);
        } catch (Exception e) {
            final String error = "Could not create JDBC connection";
            log.log(Level.SEVERE, error, e);
        }

        return null;
    }

    public String getyJdbcTable() {
        return (String) ((Map<String, Object>) confMap.get(JDBC)).get(JDBC_TABLE);
    }

    public String getyJdbcStringColumn() {
        return (String) ((Map<String, Object>) confMap.get(JDBC)).get(JDBC_COLUMN_STRING);
    }

    public String getyJdbcOrderColumn() {
        String s = (String) ((Map<String, Object>) confMap.get(JDBC)).get(JDBC_COLUMN_ORDER);
        return (s != null)
                ? s
                : getyJdbcStringColumn();
    }

    public Collection<Core> getCores() {
        if (cores.size() != 0) {
            return cores.values();
        }

        return new ArrayList<Core>(1){{
            add(new Core("/"));
        }};
    }
}
