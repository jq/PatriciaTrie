package com.jeraff.patricia.conf;

public class ConnectorConfig {
    int port;
    int maxIdleTime;
    int requestHeaderSize;
    int acceptors;
    int acceptQueueSize;
    int lowResourcesMaxIdleTime;
    int lowResourcesConnections;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public int getRequestHeaderSize() {
        return requestHeaderSize;
    }

    public void setRequestHeaderSize(int requestHeaderSize) {
        this.requestHeaderSize = requestHeaderSize;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public void setAcceptors(int acceptors) {
        this.acceptors = acceptors;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public void setAcceptQueueSize(int acceptQueueSize) {
        this.acceptQueueSize = acceptQueueSize;
    }

    public int getLowResourcesMaxIdleTime() {
        return lowResourcesMaxIdleTime;
    }

    public void setLowResourcesMaxIdleTime(int lowResourcesMaxIdleTime) {
        this.lowResourcesMaxIdleTime = lowResourcesMaxIdleTime;
    }

    public int getLowResourcesConnections() {
        return lowResourcesConnections;
    }

    public void setLowResourcesConnections(int lowResourcesConnections) {
        this.lowResourcesConnections = lowResourcesConnections;
    }
}
