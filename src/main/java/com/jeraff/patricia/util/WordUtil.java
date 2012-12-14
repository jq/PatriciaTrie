package com.jeraff.patricia.util;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class WordUtil {
    public static final String SPACE = " ";

    public static final List<String> stopWords = Arrays.asList(new String[]{
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with"});

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
}
