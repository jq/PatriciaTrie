package com.jeraff.patricia.server.analyzer;

import com.jeraff.patricia.server.ops.Entry;

import java.util.Comparator;

public abstract class ResultComparator implements Comparator<Entry> {
    protected String input;
    protected PatriciaStringAnalyzer analyzer;

    public ResultComparator(String prefix, PatriciaStringAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.input = analyzer.getComparable(prefix);
    }
}
