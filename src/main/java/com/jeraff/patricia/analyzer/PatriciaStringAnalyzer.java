package com.jeraff.patricia.analyzer;

import java.util.Map;
import java.util.Set;

public interface PatriciaStringAnalyzer {
    public Set<Map.Entry<String, String>> getIndexEntry(String s);
    public String getPrefixSearchKey(String s);
    public String getComparable(String String);
    public String getPreferred(String s0, String s1);
}
