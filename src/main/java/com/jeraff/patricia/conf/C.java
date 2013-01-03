package com.jeraff.patricia.conf;

import java.util.HashMap;

public class C {
    private ConnectorConfig connector;
    private HashMap<String, CoreConfig> cores;
    private JdbcConfig jdbc;

    public ConnectorConfig getConnector() {
        return connector;
    }

    public void setConnector(ConnectorConfig connector) {
        this.connector = connector;
    }

    public HashMap<String, CoreConfig> getCores() {
        return cores;
    }

    public void setCores(HashMap<String, CoreConfig> cores) {
        this.cores = cores;
    }

    public JdbcConfig getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcConfig jdbc) {
        this.jdbc = jdbc;
    }
}
