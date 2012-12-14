package com.jeraff.patricia.util;

import java.util.Comparator;

public class DistanceComparator implements Comparator<String> {
    private String input;

    public DistanceComparator(String input) {
        this.input = input.toLowerCase();
    }

    @Override
    public int compare(String s0, String s1) {
        final int dist0 = LevenshteinDistance.computeLevenshteinDistance(input, s0.toLowerCase());
        final int dist1 = LevenshteinDistance.computeLevenshteinDistance(input, s1.toLowerCase());
        final int i = new Integer(dist0).compareTo(dist1);

        return (i != 0)
                ? i
                : s0.compareTo(s1);
    }
}
