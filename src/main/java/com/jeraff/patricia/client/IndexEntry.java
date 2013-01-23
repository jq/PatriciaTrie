package com.jeraff.patricia.client;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

public class IndexEntry {
    @JsonProperty(value = "s")
    private String string;
    @JsonProperty(value = "h")
    private String hash;
    @JsonProperty
    private ArrayList<String> keys;

    public IndexEntry() {
    }

    public IndexEntry(String s, String h, ArrayList<String> keys) {
        this.string = s;
        this.hash = h;
        this.keys = keys;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String h) {
        this.hash = h;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }
}
