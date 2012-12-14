package com.jeraff.patricia.util;

import java.util.Comparator;

public class DistanceComparator implements Comparator<String> {
    private String input;

    public DistanceComparator(String input) {
        this.input = input.toLowerCase();
    }

    @Override
    public int compare(String s0, String s1) {
        final int d0 = LevenshteinDistance.computeLevenshteinDistance(input, s0.toLowerCase());
        final int d1 = LevenshteinDistance.computeLevenshteinDistance(input, s1.toLowerCase());
        final int i = new Integer(d0).compareTo(d1);
        return i;
    }
}
