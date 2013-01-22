package com.jeraff.patricia.server.ops;

import org.codehaus.jackson.annotate.JsonProperty;

public class Entry {
    @JsonProperty
    private String s;
    @JsonProperty
    private String h;

    public Entry() {
    }

    public Entry(String s, String h) {
        this.s = s;
        this.h = h;
    }

    public Entry(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }
}
