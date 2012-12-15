package com.jeraff.patricia.util;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class WordUtil {
    public static final String SPACE = " ";

    public static final List<String> stopWords = Arrays.asList(new String[]{
            "a", "an", "and", "as", "at", "be", "by",
            "if", "in", "is", "it",
            "of", "on", "or",
            "the", "to"});

    public static String stripStopWords(String s) {
        final String[] split = StringUtils.split(s);
        final List<String> cleaned = new ArrayList<String>();

        for (int i = 0; i < split.length; i++) {
            final String token = split[i];
            if (i == 0 || !stopWords.contains(token.toLowerCase())) {
                cleaned.add(token);
            }
        }

        return StringUtils.join(cleaned, SPACE);
    }

    public static String clean(String s) {
        final String charsRemoved = s.replaceAll("[^A-Za-z0-9 ]", "");
        final String stopWordsRemoved = stripStopWords(charsRemoved);
        final String lower = stopWordsRemoved.toLowerCase();
        final String chomp = StringUtils.chomp(lower);
        return chomp;
    }

    public static HashSet<String> getGramsFormPut(final String s) {
        final String clean = clean(s);
        final String[] st = StringUtils.split(clean);

        if (st.length == 0) {
            return new HashSet<String>(0);
        } else if (st.length == 1) {
            return new HashSet<String>() {{
                add(clean(s));
            }};
        }

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList(st));
        final HashSet<String> res = new HashSet<String>();
        final Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            if (!list.isEmpty()) {
                res.add(StringUtils.join(list, " "));
            }
        }

        res.add(clean);
        return res;
    }

    public static String ago(Date date) {
        final Date now = new Date();
        if (now.before(date)) {
            return "";
        }

        long delta = (now.getTime() - date.getTime()) / 1000;
        if (delta < 30) {
            return "just now";
        }

        if (delta < 60) {
            return "1 minute";
        }

        if (delta < 60 * 60) {
            long minutes = delta / 60;
            return String.format("%d minute%s", minutes, pluralize(minutes));
        }

        if (delta < 24 * 60 * 60) {
            long hours = delta / (60 * 60);
            return String.format("%d hour%s", hours, pluralize(hours));
        }

        if (delta < 30 * 24 * 60 * 60) {
            long days = delta / (24 * 60 * 60);
            return String.format("%d day%s", days, pluralize(days));
        }

        if (delta < 365 * 24 * 60 * 60) {
            long months = delta / (30 * 24 * 60 * 60);
            return String.format("%d month%s", months, pluralize(months));
        }

        long years = delta / (365 * 24 * 60 * 60);
        return String.format("%d year%s", years, pluralize(years));
    }

    public static String pluralize(Number n) {
        long l = n.longValue();
        if (l != 1) {
            return "s";
        }
        return "";
    }
}
