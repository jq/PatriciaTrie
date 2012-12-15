package com.jeraff.patricia.ops;

import com.jeraff.patricia.util.DistanceComparator;
import com.jeraff.patricia.util.WordUtil;
import org.limewire.collection.PatriciaTrie;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatriciaOps {
    private static final Logger log = Logger.getLogger(PatriciaOps.class.getPackage().getName());
    public static final int NUM_PREFIX_MATCHES = 25;

    private PatriciaTrie<String, String> patriciaTrie;
    private MessageDigest messageDigest;

    public PatriciaOps(PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.severe("Couldn't create an MD5 digest. Key collisions may take place & clobber your data");
        }
    }

    public String firstKey() {
        return patriciaTrie.firstKey();
    }

    public String lastKey() {
        return patriciaTrie.lastKey();
    }

    public int size() {
        return patriciaTrie.size();
    }

    public HashMap<String, ArrayList<String>> put(String[] strings) {
        final int length = strings.length;
        final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(length);

        for (int i = 0; i < length; i++) {
            final String string = strings[i];
            final ArrayList<String> grams = new ArrayList<String>();

            if (messageDigest == null) {
                log.log(Level.WARNING, "Performing put(\"{0}\") w/o md5 digest. Key collision is possible", string);
            }

            for (String gram : WordUtil.getGramsForPut(string)) {
                final String clean = WordUtil.clean(gram);
                if (messageDigest != null) {
                    patriciaTrie.put(generateKey(string, clean), string);
                } else {
                    patriciaTrie.put(clean, string);
                }

                grams.add(gram);
            }

            result.put(string, grams);
        }

        return result;
    }

    private String generateKey(String string, String clean) {
        return String.format("%s.%s", clean, messageDigest.digest(string.getBytes()));
    }

    public List<String> getPrefixedBy(String prefix) {
        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(WordUtil.clean(prefix));

        if (prefixedBy.isEmpty()) {
            return new ArrayList<String>();
        }

        List<String> result = new ArrayList<String>(new TreeSet<String>(prefixedBy.values()));
        final int total = result.size();
        if (total > NUM_PREFIX_MATCHES) {
            result = result.subList(0, NUM_PREFIX_MATCHES);
        }

        Collections.sort(result, new DistanceComparator(prefix));
        return result;
    }

    public int getPrefixedByCount(String string) {
        return getPrefixedBy(string).size();
    }

    public HashMap<String, String> remove(String[] strings) {
        final int length = strings.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (String string : strings) {
            for (String gram : WordUtil.getGramsForPut(string)) {
                final String clean = WordUtil.clean(gram);
                result.put(string, patriciaTrie.remove(generateKey(string, clean)));
            }
        }

        return result;
    }
}
