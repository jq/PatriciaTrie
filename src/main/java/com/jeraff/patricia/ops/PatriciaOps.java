package com.jeraff.patricia.ops;

import com.jeraff.patricia.analyzer.PartialMatchAnalyzer;
import com.jeraff.patricia.analyzer.DistanceComparator;
import org.limewire.collection.PatriciaTrie;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
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
            final ArrayList<String> keys = new ArrayList<String>();

            final Set<Map.Entry<String, String>> indexEntries = analyzer.getIndexEntry(string);
            for (Map.Entry<String, String> entry : indexEntries) {
                patriciaTrie.put(entry.getKey(), string);
                keys.add(entry.getKey());
            }

            if (result != null) {
                result.put(string, keys);
            }
        }

        return result;
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

    public void enqueue(final String[] strings) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                for (String string : strings) {
                    if (log.isLoggable(Level.INFO)) {
                        log.log(Level.INFO, "Working on {0} strings", strings.length);
                    }

                    final Set<Map.Entry<String, String>> indexEntries = analyzer.getIndexEntry(string);
                    for (Map.Entry<String, String> entry : indexEntries) {
                        final String key = entry.getKey();

                        if (patriciaTrie.containsKey(key)) {
                            final String existing = patriciaTrie.get(key);
                            final String winner = analyzer.getPreferred(existing, string);
                            if (!winner.equals(existing)) {
                                put(new String[]{winner});
                            }
                        } else {
                            put(new String[]{string});
                        }
                    }
                }
            }
        });
    }
}
