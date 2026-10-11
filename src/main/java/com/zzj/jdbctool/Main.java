package com.zzj.jdbctool;

import com.zzj.jdbctool.entity.College;
import com.zzj.jdbctool.entity.Student;
import com.zzj.jdbctool.util.DbUtil;
import com.zzj.jdbctool.util.JDBCTool;

import java.math.BigDecimal;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * 演示入口：使用 JDBCTool 完成 student 与 college 两张表的增删改查。
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // 统一控制台输出编码为 UTF-8，避免 Windows 下中文乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        try (Connection connection = DbUtil.getConnection()) {
            // 1. 初始化表结构，并清空旧数据，保证每次运行结果一致
            DbUtil.executeScript(connection, "schema.sql");
            clearTables(connection);

            System.out.println("========== 1. 保存数据 (save) ==========");
            College cs = new College("1", "计算机学院", "CS");
            College math = new College("2", "数学学院", "MATH");
            System.out.println("save 学院: " + JDBCTool.save(cs, connection) + " 行 -> " + cs);
            System.out.println("save 学院: " + JDBCTool.save(math, connection) + " 行 -> " + math);

            Student s1 = new Student("1", "张三", "软件工程", 20,
                    LocalDate.of(2023, 9, 1), false, new BigDecimal("12000.00"));
            Student s2 = new Student("2", "李四", "计算机科学", 21,
                    LocalDate.of(2022, 9, 1), false, new BigDecimal("11000.00"));
            Student s3 = new Student("3", "王五", "人工智能", 22,
                    LocalDate.of(2021, 9, 1), true, new BigDecimal("13000.00"));
            System.out.println("save 学生: " + JDBCTool.save(s1, connection) + " 行 -> " + s1);
            System.out.println("save 学生: " + JDBCTool.save(s2, connection) + " 行 -> " + s2);
            System.out.println("save 学生: " + JDBCTool.save(s3, connection) + " 行 -> " + s3);

            System.out.println();
            System.out.println("========== 2. 查询全部 (resultSetToList) ==========");
            List<College> colleges = selectAll(connection, College.class);
            System.out.println("学院表记录数: " + colleges.size());
            colleges.forEach(System.out::println);

            List<Student> students = selectAll(connection, Student.class);
            System.out.println("学生表记录数: " + students.size());
            students.forEach(System.out::println);

            System.out.println();
            System.out.println("========== 3. 按主键查询 (getOneById) ==========");
            Student one = JDBCTool.getOneById("1", Student.class, connection);
            System.out.println("id=1 的学生: " + one);
            College college = JDBCTool.getOneById("2", College.class, connection);
            System.out.println("id=2 的学院: " + college);

            System.out.println();
            System.out.println("========== 4. 更新数据 (update) ==========");
            s1.setAge(21);
            s1.setTuition(new BigDecimal("15000.00"));
            System.out.println("update 学生: " + JDBCTool.update(s1, connection) + " 行 -> " + s1);
            System.out.println("更新后 id=1: " + JDBCTool.getOneById("1", Student.class, connection));

            System.out.println();
            System.out.println("========== 5. 删除数据 (delete) ==========");
            System.out.println("delete 学生: " + JDBCTool.delete(s3, connection) + " 行 -> id=" + s3.getId());
            System.out.println("delete 学院: " + JDBCTool.delete(math, connection) + " 行 -> id=" + math.getId());

            System.out.println();
            System.out.println("========== 6. 删除后再次查询 ==========");
            List<Student> afterDelete = selectAll(connection, Student.class);
            System.out.println("学生表记录数: " + afterDelete.size());
            afterDelete.forEach(System.out::println);

            List<College> afterDeleteCollege = selectAll(connection, College.class);
            System.out.println("学院表记录数: " + afterDeleteCollege.size());
            afterDeleteCollege.forEach(System.out::println);

            System.out.println();
            System.out.println("========== 全部操作执行完成 ==========");
        }
    }

    private static void clearTables(Connection connection) throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM student");
            stmt.execute("DELETE FROM college");
        }
    }

    private static <T> List<T> selectAll(Connection connection, Class<T> clazz) throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName(clazz))) {
            return JDBCTool.resultSetToList(rs, clazz);
        }
    }

    private static String tableName(Class<?> clazz) {
        var table = clazz.getAnnotation(com.zzj.jdbctool.annotation.Table.class);
        return table == null ? clazz.getSimpleName().toLowerCase() : table.value();
    }
}
