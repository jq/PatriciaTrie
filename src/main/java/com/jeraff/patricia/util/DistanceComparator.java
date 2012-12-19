package com.jeraff.patricia.util;

import com.jeraff.patricia.analyzer.PatriciaStringAnalyzer;

import java.util.Comparator;

public class DistanceComparator implements Comparator<String> {
    private String input;
    private PatriciaStringAnalyzer analyzer;

    public DistanceComparator(String input, PatriciaStringAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.input = analyzer.clean(input, true);
    }

    @Override
    public int compare(String s0, String s1) {
        s0 = analyzer.clean(s0, true);
        s1 = analyzer.clean(s1, true);

        final int i0 = s0.indexOf(input);
        final int i1 = s1.indexOf(input);

        if (i0 != i1 && (i0 + i1 != -2)) {
            if (i0 == 0) {
                return -1;
            } else if (i1 == 0) {
                return 1;
            }
        }

        final int dist0 = LevenshteinDistance.computeLevenshteinDistance(input, s0);
        final int dist1 = LevenshteinDistance.computeLevenshteinDistance(input, s1);
        final int i = new Integer(dist0).compareTo(dist1);

        return (i != 0)
                ? i
                : s0.compareTo(s1);
    }
}
