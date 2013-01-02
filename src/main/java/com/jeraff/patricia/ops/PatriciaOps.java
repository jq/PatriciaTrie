package com.jeraff.patricia.ops;

import com.jeraff.patricia.analyzer.PartialMatchAnalyzer;
import com.jeraff.patricia.analyzer.DistanceComparator;
import org.limewire.collection.PatriciaTrie;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class PatriciaOps {
    private static final Logger log = Logger.getLogger(PatriciaOps.class.getCanonicalName());
    private static final int NUM_PREFIX_MATCHES = 10;
    private static final int DEFAULT_THREADS = 20;

    private PatriciaTrie<String, String> patriciaTrie;
    private final PartialMatchAnalyzer analyzer;
    private final ExecutorService executor;

    public PatriciaOps(PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
        this.analyzer = new PartialMatchAnalyzer();
        this.executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
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

        for (String string : strings) {
            put(string, result);
        }

        return result;
    }

    private void put(String string, HashMap<String, ArrayList<String>> result) {
        final ArrayList<String> indexKeys = new ArrayList<String>();

        final Set<Map.Entry<String, String>> indexKeyValues = analyzer.getIndexEntry(string);
        for (Map.Entry<String, String> indexKeyValue : indexKeyValues) {
            patriciaTrie.put(indexKeyValue.getKey(), string);
            indexKeys.add(indexKeyValue.getKey());
        }

        if (result != null) {
            result.put(string, indexKeys);
        }
    }

    public List<String> getPrefixedBy(String prefix) {
        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(analyzer.getPrefixSearchKey(prefix));

        if (prefixedBy.isEmpty()) {
            return new ArrayList<String>();
        }

        List<String> result = new ArrayList<String>(new TreeSet<String>(prefixedBy.values()));
        final int total = result.size();

        if (total > NUM_PREFIX_MATCHES) {
            result = result.subList(0, NUM_PREFIX_MATCHES);
        }

        Collections.sort(result, new DistanceComparator(prefix, analyzer));
        return result;
    }

    public int getPrefixedByCount(String string) {
        return getPrefixedBy(string).size();
    }

    public HashMap<String, String> remove(String[] strings) {
        final int length = strings.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (String string : strings) {
            final Set<Map.Entry<String, String>> entries = analyzer.getIndexEntry(string);
            for (Map.Entry<String, String> entry : entries) {
                result.put(string, patriciaTrie.remove(entry.getKey()));
            }
        }

        return result;
    }

    public void queuePut(final String[] strings) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                for (String string : strings) {
                    put(string, null);
                }
            }
        });
    }
}
