package com.jeraff.patricia.ops;

import com.jeraff.patricia.util.DistanceComparator;
import com.jeraff.patricia.util.WordUtil;
import org.limewire.collection.PatriciaTrie;

import java.util.*;

public class PatriciaOps {
    public static final int NUM_PREFIX_MATCHES = 25;
    private PatriciaTrie<String, String> patriciaTrie;

    public PatriciaOps(PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
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

            for (String gram : WordUtil.getGramsForPut(string)) {
                final String clean = WordUtil.clean(gram);
                final String put = patriciaTrie.put(clean, string);
                grams.add(gram);
            }

            result.put(string, grams);
        }

        return result;
    }

    public List<String> getPrefixedBy(String prefix) {
        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(WordUtil.clean(prefix));

        if (prefixedBy.isEmpty()) {
            return new ArrayList<String>();
        }

        List<String> result = new ArrayList<String>(new TreeSet<String>(prefixedBy.values()));
        final int total = result.size();
        if (total > 25) {
            result = result.subList(0, NUM_PREFIX_MATCHES);
        }

        Collections.sort(result, new DistanceComparator(prefix));
        return result;
    }

    public int getPrefixedByCount(String string) {
        return patriciaTrie.getPrefixedBy(string).size();
    }

    public HashMap<String, String> remove(String[] strings) {
        final int length = strings.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (String string : strings) {
            for (String gram : WordUtil.getGramsForPut(string)) {
                result.put(string, patriciaTrie.remove(gram));
            }
        }

        return result;
    }
}
