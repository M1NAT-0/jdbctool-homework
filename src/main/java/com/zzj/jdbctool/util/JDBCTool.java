package com.zzj.jdbctool.util;

import com.zzj.jdbctool.annotation.Column;
import com.zzj.jdbctool.annotation.Id;
import com.zzj.jdbctool.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于反射 + JDBC 的简易 ORM 工具。
 *
 * <p>通过 {@link Table}、{@link Column}、{@link Id} 三个注解把实体类映射到数据库表，
 * 提供统一的增、删、改、查五个访问方法。</p>
 */
public final class JDBCTool {

    private JDBCTool() {
    }

    /**
     * 方法1：把 ResultSet 中的每一行转换为目标类的实例，返回对象列表。
     */
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz)
            throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();
        List<Field> fields = persistentFields(clazz);
        while (rs.next()) {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                Object value = rs.getObject(columnName(field));
                setField(instance, field, value);
            }
            list.add(instance);
        }
        return list;
    }

    /**
     * 方法2：保存对象到数据库（INSERT）。
     */
    public static <T> int save(T obj, Connection connection)
            throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        List<Field> fields = persistentFields(clazz);
        List<String> columns = fields.stream()
                .map(JDBCTool::columnName)
                .collect(Collectors.toList());
        String sql = "INSERT INTO " + tableName(clazz)
                + "(" + String.join(",", columns) + ")"
                + " VALUES(" + placeholders(fields.size()) + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                setParameter(ps, index++, field.get(obj));
            }
            return ps.executeUpdate();
        }
    }

    /**
     * 方法3：更新对象（UPDATE），主键作为 WHERE 条件。
     */
    public static <T> int update(T obj, Connection connection)
            throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        List<Field> fields = persistentFields(clazz);
        Field idField = idField(clazz);
        List<Field> nonIdFields = fields.stream()
                .filter(field -> !field.equals(idField))
                .collect(Collectors.toList());

        String setClause = nonIdFields.stream()
                .map(field -> columnName(field) + "=?")
                .collect(Collectors.joining(","));
        String sql = "UPDATE " + tableName(clazz)
                + " SET " + setClause
                + " WHERE " + columnName(idField) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Field field : nonIdFields) {
                field.setAccessible(true);
                setParameter(ps, index++, field.get(obj));
            }
            idField.setAccessible(true);
            setParameter(ps, index, idField.get(obj));
            return ps.executeUpdate();
        }
    }

    /**
     * 方法4：删除对象（DELETE），主键作为 WHERE 条件。
     */
    public static <T> int delete(T obj, Connection connection)
            throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field idField = idField(clazz);
        String sql = "DELETE FROM " + tableName(clazz)
                + " WHERE " + columnName(idField) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            idField.setAccessible(true);
            setParameter(ps, 1, idField.get(obj));
            return ps.executeUpdate();
        }
    }

    /**
     * 方法5：根据主键 id 查询单个对象。
     */
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection)
            throws SQLException, ReflectiveOperationException {
        Field idField = idField(clazz);
        String sql = "SELECT * FROM " + tableName(clazz)
                + " WHERE " + columnName(idField) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = resultSetToList(rs, clazz);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    // ==================== 辅助方法 ====================

    private static String tableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return camelToSnake(clazz.getSimpleName());
    }

    private static String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            return column.value();
        }
        return camelToSnake(field.getName());
    }

    private static List<Field> persistentFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Field idField(Class<?> clazz) {
        for (Field field : persistentFields(clazz)) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("实体类 " + clazz.getName() + " 缺少 @Id 主键字段");
    }

    private static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    private static String camelToSnake(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void setField(Object target, Field field, Object value)
            throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (value == null) {
            if (!type.isPrimitive()) {
                field.set(target, null);
            }
            return;
        }
        field.set(target, convert(value, type));
    }

    private static void setParameter(PreparedStatement ps, int index, Object value)
            throws SQLException {
        Object jdbcValue = value;
        if (value instanceof LocalDate) {
            jdbcValue = java.sql.Date.valueOf((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            jdbcValue = Timestamp.valueOf((LocalDateTime) value);
        } else if (value instanceof java.util.Date && !(value instanceof java.sql.Date)
                && !(value instanceof Timestamp)) {
            jdbcValue = new Timestamp(((java.util.Date) value).getTime());
        }
        ps.setObject(index, jdbcValue);
    }

    private static Object convert(Object value, Class<?> type) {
        if (type.isInstance(value)) {
            return value;
        }
        if (type == String.class) {
            return value.toString();
        }
        if (type == Integer.class || type == int.class) {
            return toInt(value);
        }
        if (type == Long.class || type == long.class) {
            return toLong(value);
        }
        if (type == Double.class || type == double.class) {
            return Double.valueOf(value.toString());
        }
        if (type == Float.class || type == float.class) {
            return Float.valueOf(value.toString());
        }
        if (type == Boolean.class || type == boolean.class) {
            return toBoolean(value);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(value.toString());
        }
        if (type == LocalDate.class) {
            if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).toLocalDate();
            }
            if (value instanceof LocalDate) {
                return value;
            }
            return LocalDate.parse(value.toString());
        }
        if (type == LocalDateTime.class) {
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime();
            }
            if (value instanceof LocalDateTime) {
                return value;
            }
            return LocalDateTime.parse(value.toString());
        }
        return value;
    }

    private static Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String s = value.toString();
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }
}
