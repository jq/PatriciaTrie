package com.jeraff.patricia.server.bootstrap;

import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.ops.PatriciaOps;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBC implements Bootstrap {
    protected static final Logger log = Logger.getLogger(JDBC.class.getCanonicalName());
    private static final int LIMIT = 250;

    private Core core;
    private PatriciaOps ops;

    public JDBC(Core core, PatriciaOps ops) {
        this.core = core;
        this.ops = ops;
    }

    public Connection getJdbcConnection() {
        if (core.getJdbc() == null) {
            log.log(Level.WARNING, "No JDBC configuration available");
            return null;
        }

        try {
            final Properties properties = new Properties();
            properties.put("user", core.getJdbc().getUser());
            properties.put("password", core.getJdbc().getPassword());

            return DriverManager.getConnection(core.getJdbc().getUrl(), properties);
        } catch (Exception e) {
            final String error = "Could not create JDBC connection";
            log.log(Level.SEVERE, error, e);
            return null;
        }
    }

    public boolean bootstrap() throws Exception {
        Connection connection = null;
        int numInserted = 0;

        try {
            connection = getJdbcConnection();
            if (connection == null) {
                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "No JDBC connection established");
                }
                return false;
            }

            // initial setup...
            // figure out the column index
            final Statement statement = connection.createStatement();
            int columnIndex = 0;

            int offset = 0;
            ResultSet rs = statement.executeQuery(
                    createSelectQuery(
                            core.getJdbc().getTable(),
                            core.getJdbc().getS(),
                            core.getJdbc().getOrder(),
                            offset));

            // figure out the column index
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String name = metaData.getColumnName(i);
                if (name.equals(core.getJdbc().getS())) {
                    columnIndex = i;
                    break;
                }
            }

            while (rs.next()) {
                String string = rs.getString(columnIndex);
//                ops.put(new String[]{string}, false);
                numInserted++;

                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Bootstrap: {0} in {1}", new Object[]{string, core});
                }

                if (!rs.next()) {
                    offset += LIMIT;
                    rs = statement.executeQuery(createSelectQuery(
                            core.getJdbc().getTable(),
                            core.getJdbc().getS(),
                            core.getJdbc().getOrder(),
                            offset));
                } else {
                    rs.previous();
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Bootstrap error", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }

        log.log(Level.INFO, "Inserted {0} strings in {1}", new Object[]{numInserted, core});
        return true;
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
