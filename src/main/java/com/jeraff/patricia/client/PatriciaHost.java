package com.jeraff.patricia.client;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Connector;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class PatriciaHost {
    private String host = "localhost";
    private int port = Connector.DEFAULT_PORT;
    private String configPath = Config.DEFAULT_CONFIG_CONTEXT_PATH;
    private String scheme = "http";

    public PatriciaHost() {
    }

    public PatriciaHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public PatriciaHost(String host, int port, String configPath) {
        this.host = host;
        this.port = port;
        this.configPath = configPath;
    }

    public PatriciaHost(String scheme, String host, int port, String configPath) {
        this.scheme = scheme;
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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }

    public URI getConfigUri() {
        URIBuilder builder = new URIBuilder()
                .setScheme(scheme)
                .setHost(host)
                .setPort(port)
                .setPath(configPath + "/cores");

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
