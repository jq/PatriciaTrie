package com.jeraff.patricia.analyzer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class PartialMatchAnalyzer implements PatriciaStringAnalyzer {
    @Override
    public Set<Map.Entry<String, String>> getIndexEntry(String s) {
        final HashMap<String, String> rtn = new HashMap<String, String>();

        for (String gram : getGramsForPut(s)) {
            final String clean = (gram.indexOf("^") == 0) ? gram.toLowerCase() : clean(gram);
            final String key = generateKey(s, clean);
            rtn.put(key, s);
        }

        return rtn.entrySet();
    }

    @Override
    public String getPrefixSearchKey(String s) {
        return (startsWithStopWord(s))
                ? getStartsWithKey(s.toLowerCase())
                : clean(s);
    }

    @Override
    public String getComparable(String String) {
        return clean(String, true);
    }

    @Override
    public String getPreferred(String s0, String s1) {
        final int i0 = numCaps(s0);
        final int i1 = numCaps(s1);

        if (i1 > i0) {
            return s1;
        }

        return s0;
    }

    ////////////////////////////////////////////////////////////////////////
    // helper methods & vars
    ////////////////////////////////////////////////////////////////////////
    private static final String SPACE = " ";
    private static final HashMap<String, Byte> stopWords = new HashMap<String, Byte>();

    static {
        String[] stopWordsArray = new String[]{
                "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
                "not", "of", "on", "or", "s", "such", "t", "that", "the", "their", "then", "there", "these", "they",
                "this", "to", "was", "will", "with"};

        for (String stopWord : stopWordsArray) {
            stopWords.put(stopWord, Byte.MIN_VALUE);
        }
    }

    private String generateKey(String string, String clean) {
        final String suffix = (string.length() < 32)
                ? StringUtils.deleteWhitespace(clean(string, false))
                : DigestUtils.md5Hex(string);

        return String.format("%s.%s", clean, suffix);
    }

    private HashSet<String> getGramsForPut(String s) {
        final HashSet<String> rtn = new HashSet<String>();
        rtn.addAll(getGramsForCleanedString(clean(s, true)));

        final HashSet<String> cleansStopWordsInTact = getGramsForCleanedString(clean(s, false));
        rtn.addAll(cleansStopWordsInTact);

        if (startsWithStopWord(s)) {
            rtn.add(getStartsWithKey(s.toLowerCase()));
        }

        return rtn;
    }

    private String stripStopWords(String s) {
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

    private String clean(String s) {
        return clean(s, false);
    }

    private String clean(String s, boolean stripStopWords) {
        String current = s.replaceAll("[^A-Za-z0-9 ]", "");

        if (stripStopWords) {
            current = stripStopWords(current);
        }

        return StringUtils.trim(StringUtils.chomp(current.toLowerCase()));
    }

    private String getStartsWithKey(String s) {
        return String.format("^%s", s);
    }

    private HashSet<String> getGramsForCleanedString(final String cleanedString) {
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

    private static boolean startsWithStopWord(String s) {
        for (String stopWord : stopWords.keySet()) {
            if (s.toLowerCase().startsWith(stopWord)) {
                return true;
            }
        }
        return false;
    }

    private int numCaps(String str) {
        int num = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (Character.isUpperCase(str.charAt(i))) {
                num++;
            }
        }
        return num;
    }
}
