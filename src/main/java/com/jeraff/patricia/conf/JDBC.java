package com.jeraff.patricia.conf;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect
public class JDBC {
    private String url;
    private String table;
    private String s = "s";
    private String order = "s";
    private String user = "root";
    private String password = "";
    private String hash = "hash";
    private Class driver = com.mysql.jdbc.Driver.class;
    private String createTableSQL;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Class getDriver() {
        return driver;
    }

    public void setDriver(Class driver) {
        this.driver = driver;
    }

    public String getCreateTableSQL() {
        if (createTableSQL == null) {
            createTableSQL = String.format("CREATE TABLE IF NOT EXISTS `%s` (\n" +
                                                   "  `%s` varchar(255) NOT NULL DEFAULT '',\n" +
                                                   "  `%s` varchar(32) NOT NULL DEFAULT '',\n" +
                                                   "  PRIMARY KEY (`%s`)\n" +
                                                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

                                           table,
                                           s,
                                           hash,
                                           hash);
        }

        return createTableSQL;
    }

    public void setCreateTableSQL(String createTableSQL) {
        this.createTableSQL = createTableSQL;
    }
}
