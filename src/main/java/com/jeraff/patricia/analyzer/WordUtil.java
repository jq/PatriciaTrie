package com.jeraff.patricia.analyzer;

import org.apache.commons.lang.StringUtils;

import java.util.*;

class WordUtil {
    static final String SPACE = " ";

    static final HashMap<String, Byte> stopWords = new HashMap<String, Byte>();

    static {
        String[] stopWordsArray = new String[]{
                "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
                "not", "of", "on", "or", "s", "such", "t", "that", "the", "their", "then", "there", "these", "they",
                "this", "to", "was", "will", "with"};

        for (int i = 0; i < stopWordsArray.length; i++) {
            stopWords.put(stopWordsArray[i], Byte.MIN_VALUE);
        }
    }

    static String stripStopWords(String s) {
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

    static boolean isStopWord(String s) {
        return stopWords.containsKey(s.toLowerCase());
    }

    static String clean(String s) {
        return clean(s, false);
    }

    static String clean(String s, boolean stripStopWords) {
        String current = s.replaceAll("[^A-Za-z0-9 ]", "");

        if (stripStopWords) {
            current = stripStopWords(current);
        }

        return StringUtils.trim(StringUtils.chomp(current.toLowerCase()));
    }

    static HashSet<String> getGramsForPut(final String s) {
        final HashSet<String> rtn = new HashSet<String>();
        rtn.addAll(getGramsForCleanedString(clean(s, true)));

        final HashSet<String> cleansStopWordsInTact = getGramsForCleanedString(clean(s, false));
        rtn.addAll(cleansStopWordsInTact);

        final String firstWord = StringUtils.split(s)[0];
        if (WordUtil.isStopWord(firstWord)) {
            rtn.add(getStartsWithKey(firstWord));
        }

        return rtn;
    }

    static String getStartsWithKey(String s) {
        return String.format("^%s", s);
    }

    static HashSet<String> getGramsForCleanedString(final String cleanedString) {
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
}
