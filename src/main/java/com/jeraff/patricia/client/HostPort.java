package com.jeraff.patricia.client;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Connector;

public class HostPort {
    private String host = "localhost";
    private int port = Connector.DEFAULT_PORT;
    private String configPath = Config.DEFAULT_CONFIG_CONTEXT_PATH;

    public HostPort() {
    }

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HostPort(String host, int port, String configPath) {
        this.host = host;
        this.port = port;
        this.configPath = configPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }
}
