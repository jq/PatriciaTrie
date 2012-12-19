package com.jeraff.patricia.analyzer;

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

        final int dist0 = computeLevenshteinDistance(input, s0);
        final int dist1 = computeLevenshteinDistance(input, s1);
        final int i = new Integer(dist0).compareTo(dist1);

        return (i != 0)
                ? i
                : s0.compareTo(s1);
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1]
                                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                : 1));

        return distance[str1.length()][str2.length()];
    }
}
