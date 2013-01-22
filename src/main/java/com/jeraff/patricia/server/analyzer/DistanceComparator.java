package com.jeraff.patricia.server.analyzer;

import com.jeraff.patricia.server.ops.Entry;
import org.apache.commons.lang.StringUtils;

public class DistanceComparator extends ResultComparator {
    public DistanceComparator(String input, PatriciaStringAnalyzer analyzer) {
        super(input, analyzer);
    }

    @Override
    public int compare(Entry e0, Entry e1) {
        final String s0 = analyzer.getComparable(e0.getS());
        final String s1 = analyzer.getComparable(e1.getS());

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
