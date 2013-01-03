package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.ops.PatriciaOps;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

public class CoreHandler extends BaseHandler {
    public static final String TARGET_API = "api";

    private final WebHandler web;
    private final ApiHandler api;
    private final PatriciaTrie<String, String> patriciaTrie;

    public CoreHandler(Core core, Config config) {
        this.patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        this.core = core;
        this.web = new WebHandler(patriciaTrie, core, config);
        this.api = new ApiHandler(patriciaTrie, core, config);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (StringUtils.strip(target, "/").equals(TARGET_API)) {
            api.handle(target, baseRequest, request, response);
        } else {
            web.handle(target, baseRequest, request, response);
        }
    }

    public FutureTask getBootstrapFuture() {
        if (core.getJdbc() == null) {
            return null;
        }

        return new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                return new Bootstrap().run();
            }
        });
    }

    private class Bootstrap {
        private static final int LIMIT = 250;

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
            }

            return null;
        }

        public boolean run() throws Exception {
            PatriciaOps ops = new PatriciaOps(core, patriciaTrie);
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
                    ops.put(new String[]{string});
                    numInserted++;

                    if (log.isLoggable(Level.INFO)) {
                        log.log(Level.INFO, "Bootstrap: {0}", string);
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
}
