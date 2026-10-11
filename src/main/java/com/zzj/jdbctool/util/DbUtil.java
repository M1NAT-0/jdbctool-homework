package com.zzj.jdbctool.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 数据库统一访问的辅助类：负责读取配置、创建连接、执行建表脚本。
 */
public final class DbUtil {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("找不到 db.properties");
            }
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        URL = props.getProperty("jdbc.url");
        USERNAME = props.getProperty("jdbc.username");
        PASSWORD = props.getProperty("jdbc.password");
    }

    private DbUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 执行 classpath 下的 SQL 脚本（按分号拆分逐条执行），用于初始化表结构。
     */
    public static void executeScript(Connection connection, String resource)
            throws IOException, SQLException {
        String script;
        try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("找不到 SQL 脚本: " + resource);
            }
            script = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }

        String[] statements = script.split(";");
        try (Statement stmt = connection.createStatement()) {
            for (String statement : statements) {
                String sql = statement.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    stmt.execute(sql);
                }
            }
        }
    }
}
