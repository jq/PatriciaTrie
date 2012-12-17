package com.jeraff.patricia.util;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class WordUtil {
    public static final String SPACE = " ";

    public static final HashMap<String, Byte> stopWords = new HashMap<String, Byte>();

    static {
        String[] stopWordsArray = new String[]{
                "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
                "not", "of", "on", "or", "s", "such", "t", "that", "the", "their", "then", "there", "these", "they",
                "this", "to", "was", "will", "with"};

        for (int i = 0; i < stopWordsArray.length; i++) {
            stopWords.put(stopWordsArray[i], Byte.MIN_VALUE);
        }
    }

    public static String stripStopWords(String s) {
        final String[] split = StringUtils.split(s);
        final List<String> cleaned = new ArrayList<String>();

        for (int i = 0; i < split.length; i++) {
            final String token = split[i];
            if (i == 0 || !stopWords.containsKey(token.toLowerCase())) {
                cleaned.add(token);
            }
        }

        return StringUtils.join(cleaned, SPACE);
    }

    public static boolean isSTopWord(String s) {
        return stopWords.containsKey(s.toLowerCase());
    }

    public static String clean(String s) {
        return clean(s, false);
    }

    public static String clean(String s, boolean stripStopWords) {
        String current = s.replaceAll("[^A-Za-z0-9 ]", "");

        if (stripStopWords) {
            current = stripStopWords(current);
        }

        return StringUtils.trim(StringUtils.chomp(current.toLowerCase()));
    }

    public static HashSet<String> getGramsForPut(final String s) {
        final HashSet<String> rtn = new HashSet<String>();
        rtn.addAll(getGramsForCleanedString(clean(s, true)));

        final HashSet<String> cleansStopWordsInTact = getGramsForCleanedString(clean(s, false));
        rtn.addAll(cleansStopWordsInTact);

        final String firstWord = StringUtils.split(s)[0];
        if (WordUtil.isSTopWord(firstWord)) {
            rtn.add(getStartsWithKey(firstWord));
        }

        return rtn;
    }

    public static String getStartsWithKey(String s) {
        return String.format("^%s", s);
    }

    public static HashSet<String> getGramsForCleanedString(final String cleanedString) {
        final String[] st = StringUtils.split(cleanedString);

        if (st.length == 0) {
            return new HashSet<String>(0);
        } else if (st.length == 1) {
            return new HashSet<String>() {{
                add(cleanedString);
            }};
        }

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList(st));
        final HashSet<String> res = new HashSet<String>();
        final Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            if (!list.isEmpty()) {
                res.add(StringUtils.join(list, " "));
            }

            iterator.next();
            iterator.remove();
        }

        res.add(cleanedString);
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
