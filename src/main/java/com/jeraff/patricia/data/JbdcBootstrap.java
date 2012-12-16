package com.jeraff.patricia.data;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.ops.PatriciaOps;
import org.limewire.collection.PatriciaTrie;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JbdcBootstrap {
    protected static final Logger log = Logger.getLogger(JbdcBootstrap.class.getPackage().getName());

    private final PatriciaOps ops;
    private Config config;

    private int offset;
    private static final int LIMIT = 500;

    public JbdcBootstrap(PatriciaTrie<String, String> patriciaTrie, Config config) {
        this.config = config;
        this.ops = new PatriciaOps(patriciaTrie);
    }

    public void run() {
        Connection connection = null;

        try {
            connection = config.getJdbcConnection();
            if (connection == null) {
                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "No JDBC connection established");
                }
                return;
            }

            // initial setup...
            // figure out the column index
            final Statement statement = connection.createStatement();
            int columnIndex = 0;

            ResultSet rs = statement.executeQuery(
                    createSelectQuery(
                            config.getyJdbcTable(),
                            config.getyJdbcStringColumn(),
                            config.getyJdbcOrderColumn(),
                            offset));

            // figure out the column index
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String name = metaData.getColumnName(i);
                if (name.equals(config.getyJdbcStringColumn())) {
                    columnIndex = i;
                    break;
                }
            }

            while (rs.next()) {
                String string = rs.getString(columnIndex);
                ops.put(new String[]{string});

                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Bootstrap: {0}", string);
                }

                if (!rs.next()) {
                    offset += LIMIT;
                    rs = statement.executeQuery(createSelectQuery(
                            config.getyJdbcTable(),
                            config.getyJdbcStringColumn(),
                            config.getyJdbcOrderColumn(),
                            offset));
                } else {
                    rs.previous();
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Bootstrap error", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.log(Level.INFO, "Error closing connection", e);
                }
            }
        }
    }

    private String createSelectQuery(String table, String stringColumn, String orderColumn, int offset) {
        String sql = String.format("SELECT %s from %s ORDER BY %s ASC LIMIT %d OFFSET %d",
                stringColumn, table, orderColumn, LIMIT, offset);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, sql);
        }

        return sql;
    }
}
