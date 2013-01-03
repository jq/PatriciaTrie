package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.ops.PatriciaOps;
import org.apache.commons.beanutils.BeanMap;
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
import java.util.logging.Level;

public class CoreHandler extends BaseHandler {
    public static final String TARGET_API = "api";

    private final WebHandler web;
    private final ApiHandler api;
    private final PatriciaTrie<String, String> patriciaTrie;

    public CoreHandler(Core core, Config config) {
        this.patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());

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

    private class Bootstrap {
        private static final int LIMIT = 250;

        public Connection getJdbcConnection() {
            if (core.getJdbc() != null) {
                log.log(Level.WARNING, "No JDBC configuration available");
                return null;
            }

            try {
                final BeanMap map = new BeanMap(core.getJdbc());
                final Properties properties = new Properties();

                for (Object o : map.keySet()) {
                    properties.put(o, map.get(o));
                }

                return DriverManager.getConnection(core.getJdbc().getUrl(), properties);
            } catch (Exception e) {
                final String error = "Could not create JDBC connection";
                log.log(Level.SEVERE, error, e);
            }

            return null;
        }

        public void run() throws Exception {
            PatriciaOps ops = new PatriciaOps(core, patriciaTrie);
            Connection connection = null;
            try {
                connection = getJdbcConnection();
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
}
