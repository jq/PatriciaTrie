package com.jeraff.patricia.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (!stopWords.contains(token.toLowerCase())) {
                cleaned.add(token);
            }
        }

        return StringUtils.join(cleaned, SPACE);
    }

    public static String clean(String s) {
        return stripStopWords(s).toLowerCase();
    }

    public static ArrayList<String> getGramsFormPut(final String s) {
        final String[] st = StringUtils.split(clean(s));
        if (st.length == 1) {
            return new ArrayList<String>(){{
                add(s);
            }};
        }

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList(st));
        final ArrayList<String> res = new ArrayList<String>();
        final int size = list.size();

        for (int i = -1; i < size; i++) {
            if (i >= 0 && i < list.size()) {
                list.remove(i);
            }

            if (list.size() != 1) {
                res.add(StringUtils.join(list, " "));
            }
        }

        return res;
    }
}
