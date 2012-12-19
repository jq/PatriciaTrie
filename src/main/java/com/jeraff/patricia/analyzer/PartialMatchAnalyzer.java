package com.jeraff.patricia.analyzer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class PartialMatchAnalyzer implements PatriciaStringAnalyzer {

    @Override
    public Set<Map.Entry<String, String>> getIndexKeyValues(String s) {
        final HashMap<String, String> rtn = new HashMap<String, String>();

        for (String gram : getGramsForPut(s)) {
            final String clean = (gram.indexOf("^") == 0) ? gram.toLowerCase() : WordUtil.clean(gram);
            final String key = generateKey(s, clean);
            rtn.put(key, s);
        }

        return rtn.entrySet();
    }

    @Override
    public String getPrefixSearchKey(String s) {
        return (WordUtil.isSTopWord(s))
                ? WordUtil.getStartsWithKey(s.toLowerCase())
                : WordUtil.clean(s);
    }

    @Override
    public String clean(String String, boolean b) {
        return WordUtil.clean(String, b);
    }

    private String generateKey(String string, String clean) {
        final String suffix = (string.length() < 32)
                ? StringUtils.deleteWhitespace(WordUtil.clean(string, false))
                : DigestUtils.md5Hex(string);

        return String.format("%s.%s", clean, suffix);
    }

    private HashSet<String> getGramsForPut(String s) {
        final HashSet<String> rtn = new HashSet<String>();
        rtn.addAll(WordUtil.getGramsForCleanedString(WordUtil.clean(s, true)));

        final HashSet<String> cleansStopWordsInTact = WordUtil.getGramsForCleanedString(WordUtil.clean(s, false));
        rtn.addAll(cleansStopWordsInTact);

        final String firstWord = StringUtils.split(s)[0];
        if (WordUtil.isSTopWord(firstWord)) {
            rtn.add(WordUtil.getStartsWithKey(firstWord));
        }

        return rtn;
    }
}
