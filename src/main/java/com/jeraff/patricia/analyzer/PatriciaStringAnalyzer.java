package com.jeraff.patricia.analyzer;

import java.util.Map;
import java.util.Set;

public interface PatriciaStringAnalyzer {
    public Set<Map.Entry<String, String>> getIndexKeyValues(String s);
    public String getPrefixSearchKey(String s);
    public String clean(String String, boolean b);
}
