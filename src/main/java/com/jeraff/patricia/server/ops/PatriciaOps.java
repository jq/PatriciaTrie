package com.jeraff.patricia.server.ops;

import com.jeraff.patricia.client.IndexEntry;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.conf.JDBC;
import com.jeraff.patricia.server.analyzer.DistanceComparator;
import com.jeraff.patricia.server.analyzer.PartialMatchAnalyzer;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.limewire.collection.PatriciaTrie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatriciaOps {
    private static final Logger log = Logger.getLogger(PatriciaOps.class.getCanonicalName());
    private static final int NUM_PREFIX_MATCHES = 10;
    private static final int DEFAULT_THREADS = 20;

    private JDBC jdbc;
    private PatriciaTrie<String, String> patriciaTrie;
    private PartialMatchAnalyzer analyzer;
    private ExecutorService putExector;
    private ExecutorService dbExecutor;
    private ComboPooledDataSource dbPool;

    public PatriciaOps(final Core core, PatriciaTrie<String, String> patriciaTrie) {
        this.patriciaTrie = patriciaTrie;
        this.analyzer = new PartialMatchAnalyzer();

        final String canonicalCoreName = core.canonicalName();
        this.putExector = Executors.newFixedThreadPool(DEFAULT_THREADS, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "PatriciaOps.PutPool." + canonicalCoreName);
            }
        });

        if (core.getJdbc() != null) {
            this.dbExecutor = Executors.newFixedThreadPool(DEFAULT_THREADS, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "PatriciaOps.DBPool." + canonicalCoreName);
                }
            });

            try {
                jdbc = core.getJdbc();

                dbPool = new ComboPooledDataSource();
                dbPool.setDriverClass(jdbc.getDriver().getCanonicalName());
                dbPool.setJdbcUrl(jdbc.getUrl());
                dbPool.setUser(jdbc.getUser());
                dbPool.setPassword(jdbc.getPassword());
                dbPool.setAutoCommitOnClose(true);
                dbPool.setMaxPoolSize(DEFAULT_THREADS);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Couldn't create DB connection", e);
                throw new RuntimeException(e);
            }
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

    public HashMap<String, IndexEntry> put(String k, String v) {
        return put(new BasicNameValuePair(k, v));
    }

    public HashMap<String, IndexEntry> put(NameValuePair nvp) {
        ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(nvp);
        return put(nvps);
    }

    public HashMap<String, IndexEntry> put(List<NameValuePair> nameValuePairs) {
        final int length = nameValuePairs.size();
        final HashMap<String, IndexEntry> result = new HashMap<String, IndexEntry>(length);

        for (NameValuePair nvp : nameValuePairs) {
            String name = nvp.getName();
            String value = nvp.getValue();

            final ArrayList<String> keys = new ArrayList<String>();
            final Set<Map.Entry<String, String>> indexEntries = analyzer.getIndexEntry(name);

            for (Map.Entry<String, String> entry : indexEntries) {
                patriciaTrie.put(entry.getKey(), value);
                keys.add(entry.getKey());
            }

            result.put(name, new IndexEntry(value, analyzer.getHash(name + value), keys));
        }

        return result;
    }

    public List<Entry> getPrefixedBy(String prefix) {
        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(analyzer.getPrefixSearchKey(prefix));

        if (prefixedBy.isEmpty()) {
            return new ArrayList<Entry>();
        }

        List<Entry> result = new ArrayList<Entry>();
        if (!prefix.isEmpty()) {
            for (Map.Entry<String, String> entry : prefixedBy.entrySet()) {
                final String s = entry.getValue();
                result.add(new Entry(s, analyzer.getHash(s)));
            }
        }

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

    public void persistString(final String str) {
        dbExecutor.submit(new Runnable() {
            private String insertString = String.format(
                    "INSERT INTO %s(%s, %s) VALUES(?, ?) ON DUPLICATE KEY UPDATE %s=?",
                    jdbc.getTable(), jdbc.getHash(), jdbc.getS(), jdbc.getS());

            @Override
            public void run() {
                Connection connection = null;
                try {
                    connection = dbPool.getConnection();
                    final PreparedStatement statement = connection.prepareStatement(insertString);

                    statement.setString(1, analyzer.getHash(str));
                    statement.setString(2, str);
                    statement.setString(3, str);
                    statement.execute();

                } catch (SQLException e) {
                    log.log(Level.WARNING, "Couldn't execute query", e);
                } finally {
                    if (connection != null) {
                        try {
                            connection.commit();
                        } catch (SQLException e) {
                            log.log(Level.FINE, "WTF", e);
                        }

                        try {
                            connection.close();
                        } catch (SQLException e) {
                            log.log(Level.FINE, "WTF", e);
                        }
                    }
                }
            }
        });
    }

    public String getHash(String s) {
        return analyzer.getHash(s);
    }
}
