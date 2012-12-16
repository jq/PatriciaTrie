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
                log.log(Level.INFO, "No JDBC connection established");
                return;
            }

            // initial setup...
            // figure out the column index
            final Statement statement = connection.createStatement();
            int columnIndex = 0;

            ResultSet rs = statement.executeQuery(
                    createSelectQuery(
                            config.getyJdbcTable(),
                            config.getyJdbcColumn(),
                            offset));

            if (!rs.next()) {
                return;
            }

            // figure out the column index
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String name = metaData.getColumnName(i);
                if (name.equals(config.getyJdbcColumn())) {
                    columnIndex = i;
                    break;
                }
            }

            while (rs.next()) {
                ops.put(new String[]{rs.getString(columnIndex)});
                log.log(Level.INFO, "Added: ");

                if (!rs.next()) {
                    offset += LIMIT;
                    rs = statement.executeQuery(createSelectQuery(config.getyJdbcTable(), config.getyJdbcColumn(), offset));
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

    private String createSelectQuery(String table, String column, int offset) {
        String sql = String.format("SELECT %s from %s LIMIT %d, %d", column, table, offset, LIMIT);
        return sql;
    }
}
