package com.library.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * JDBC工具类：负责连接获取、参数设置、增删改与查询，以及资源释放。
 * 配置从类路径下的 db.properties 读取。
 */
public final class JDBCTool {

    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties properties = new Properties();
        try (InputStream in = JDBCTool.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        DRIVER = properties.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
        URL = properties.getProperty("jdbc.url",
                "jdbc:mysql://localhost:3306/libraryadmin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
        USERNAME = properties.getProperty("jdbc.username", "root");
        PASSWORD = properties.getProperty("jdbc.password", "123456");
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private JDBCTool() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void close(AutoCloseable... resources) {
        if (resources == null) {
            return;
        }
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;
            if (param == null) {
                ps.setObject(index, null);
            } else if (param instanceof String) {
                ps.setString(index, (String) param);
            } else if (param instanceof Integer) {
                ps.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                ps.setLong(index, (Long) param);
            } else if (param instanceof Double) {
                ps.setDouble(index, (Double) param);
            } else if (param instanceof Float) {
                ps.setFloat(index, (Float) param);
            } else if (param instanceof Boolean) {
                ps.setBoolean(index, (Boolean) param);
            } else if (param instanceof LocalDateTime) {
                ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) param));
            } else if (param instanceof LocalDate) {
                ps.setDate(index, Date.valueOf((LocalDate) param));
            } else {
                ps.setObject(index, param);
            }
        }
    }

    public static int update(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        } finally {
            close(ps, conn);
        }
    }

    public static int update(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            return ps.executeUpdate();
        } finally {
            close(ps);
        }
    }

    public static <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        Connection conn = null;
        try {
            conn = getConnection();
            return queryList(conn, sql, mapper, params);
        } catch (SQLException e) {
            throw new RuntimeException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    public static <T> List<T> queryList(Connection conn, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            rs = ps.executeQuery();
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapper.map(rs));
            }
            return list;
        } finally {
            close(rs, ps);
        }
    }

    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }
}
