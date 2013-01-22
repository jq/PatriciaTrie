package com.jeraff.patricia.client;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

public class IndexEntry {
    @JsonProperty
    private String s;
    @JsonProperty
    private String h;
    @JsonProperty
    private ArrayList<String> keys;

    public IndexEntry() {
    }

    public IndexEntry(String s, String h, ArrayList<String> keys) {
        this.s = s;
        this.h = h;
        this.keys = keys;
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

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }
}
