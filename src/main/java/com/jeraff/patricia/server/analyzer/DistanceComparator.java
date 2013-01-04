package com.jeraff.patricia.server.analyzer;

import org.apache.commons.lang.StringUtils;

public class DistanceComparator extends ResultComparator {
    public DistanceComparator(String input, PatriciaStringAnalyzer analyzer) {
        super(input, analyzer);
    }

    @Override
    public int compare(String s0, String s1) {
        s0 = analyzer.getComparable(s0);
        s1 = analyzer.getComparable(s1);

        final int i0 = s0.indexOf(input);
        final int i1 = s1.indexOf(input);

        if (i0 != i1 && (i0 + i1 != -2)) {
            if (i0 == 0) {
                return -1;
            } else if (i1 == 0) {
                return 1;
            }
        }

        final int dist0 = StringUtils.getLevenshteinDistance(input, s0);
        final int dist1 = StringUtils.getLevenshteinDistance(input, s1);
        final int i = new Integer(dist0).compareTo(dist1);

        return (i != 0)
                ? i
                : s0.compareTo(s1);
    }
}
