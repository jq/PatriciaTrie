package com.jeraff.patricia.server.analyzer;

import java.util.Comparator;

public abstract class ResultComparator implements Comparator<String> {
    protected String input;
    protected PatriciaStringAnalyzer analyzer;

    public ResultComparator(String input, PatriciaStringAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.input = analyzer.getComparable(input);
    }
}
