package com.zzj.jdbctool;

import com.zzj.jdbctool.entity.College;
import com.zzj.jdbctool.entity.Student;
import com.zzj.jdbctool.util.DbUtil;
import com.zzj.jdbctool.util.JDBCTool;

import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * 交互式演示入口：通过键盘手动输入，使用 JDBCTool 完成 student 与 college 两张表的增删改查。
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Charset charset = consoleCharset();
        configureConsole(charset);

        try (Connection connection = DbUtil.getConnection();
             Scanner scanner = new Scanner(System.in, charset)) {
            // 仅确保表存在，不清空已有数据，便于手动录入的数据持久化
            DbUtil.executeScript(connection, "schema.sql");
            run(connection, scanner);
        }
    }

    private static Charset consoleCharset() {
        Console console = System.console();
        return console == null ? StandardCharsets.UTF_8 : console.charset();
    }

    private static void configureConsole(Charset charset) {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, charset));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, charset));
        } catch (Exception ignored) {
            // 无法重设控制台编码时保持默认输出
        }
    }

    private static void run(Connection connection, Scanner scanner) throws Exception {
        while (true) {
            System.out.println();
            System.out.println("========== JDBCTool 增删改查 ==========");
            System.out.println("请选择要操作的数据表：");
            System.out.println("  1. 学生表 student");
            System.out.println("  2. 学院表 college");
            System.out.println("  0. 退出程序");
            System.out.print("请输入选项：");
            switch (readLine(scanner)) {
                case "1" -> operateStudent(connection, scanner);
                case "2" -> operateCollege(connection, scanner);
                case "0" -> {
                    System.out.println("程序结束。");
                    return;
                }
                default -> System.out.println("输入无效，请重新输入。");
            }
        }
    }

    private static void operateStudent(Connection connection, Scanner scanner) throws Exception {
        while (true) {
            System.out.println();
            System.out.println("---- 学生表 student 操作 ----");
            System.out.println("  1. 新增学生 save");
            System.out.println("  2. 查询全部 resultSetToList");
            System.out.println("  3. 按 id 查询 getOneById");
            System.out.println("  4. 修改学生 update");
            System.out.println("  5. 删除学生 delete");
            System.out.println("  0. 返回上一级");
            System.out.print("请输入选项：");
            try {
                switch (readLine(scanner)) {
                    case "1" -> saveStudent(connection, scanner);
                    case "2" -> listStudents(connection);
                    case "3" -> getStudent(connection, scanner);
                    case "4" -> updateStudent(connection, scanner);
                    case "5" -> deleteStudent(connection, scanner);
                    case "0" -> { return; }
                    default -> System.out.println("输入无效，请重新输入。");
                }
            } catch (Exception e) {
                System.out.println("操作失败：" + e.getMessage());
            }
        }
    }

    private static void operateCollege(Connection connection, Scanner scanner) throws Exception {
        while (true) {
            System.out.println();
            System.out.println("---- 学院表 college 操作 ----");
            System.out.println("  1. 新增学院 save");
            System.out.println("  2. 查询全部 resultSetToList");
            System.out.println("  3. 按 id 查询 getOneById");
            System.out.println("  4. 修改学院 update");
            System.out.println("  5. 删除学院 delete");
            System.out.println("  0. 返回上一级");
            System.out.print("请输入选项：");
            try {
                switch (readLine(scanner)) {
                    case "1" -> saveCollege(connection, scanner);
                    case "2" -> listColleges(connection);
                    case "3" -> getCollege(connection, scanner);
                    case "4" -> updateCollege(connection, scanner);
                    case "5" -> deleteCollege(connection, scanner);
                    case "0" -> { return; }
                    default -> System.out.println("输入无效，请重新输入。");
                }
            } catch (Exception e) {
                System.out.println("操作失败：" + e.getMessage());
            }
        }
    }

    // ==================== 学生 ====================
    private static void saveStudent(Connection connection, Scanner scanner) throws Exception {
        System.out.print("id：");
        String id = readLine(scanner);
        System.out.print("姓名 name：");
        String name = readLine(scanner);
        System.out.print("专业 major：");
        String major = readLine(scanner);
        System.out.print("年龄 age：");
        Integer age = Integer.valueOf(readLine(scanner));
        System.out.print("入学时间 enrollDate(yyyy-MM-dd)：");
        LocalDate enrollDate = LocalDate.parse(readLine(scanner));
        System.out.print("是否毕业 graduated(true/false)：");
        Boolean graduated = parseBoolean(readLine(scanner));
        System.out.print("学费 tuition：");
        BigDecimal tuition = new BigDecimal(readLine(scanner));

        Student student = new Student(id, name, major, age, enrollDate, graduated, tuition);
        int rows = JDBCTool.save(student, connection);
        System.out.println("新增成功，影响 " + rows + " 行：" + student);
    }

    private static void listStudents(Connection connection) throws Exception {
        List<Student> students = selectAll(connection, Student.class);
        System.out.println("学生表记录数：" + students.size());
        students.forEach(System.out::println);
    }

    private static void getStudent(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要查询的学生 id：");
        Student student = JDBCTool.getOneById(readLine(scanner), Student.class, connection);
        System.out.println(student == null ? "未查询到该学生。" : student);
    }

    private static void updateStudent(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要修改的学生 id：");
        String id = readLine(scanner);
        Student student = new Student();
        student.setId(id);
        System.out.print("新的姓名 name：");
        student.setName(readLine(scanner));
        System.out.print("新的专业 major：");
        student.setMajor(readLine(scanner));
        System.out.print("新的年龄 age：");
        student.setAge(Integer.valueOf(readLine(scanner)));
        System.out.print("新的入学时间 enrollDate(yyyy-MM-dd)：");
        student.setEnrollDate(LocalDate.parse(readLine(scanner)));
        System.out.print("新的是否毕业 graduated(true/false)：");
        student.setGraduated(parseBoolean(readLine(scanner)));
        System.out.print("新的学费 tuition：");
        student.setTuition(new BigDecimal(readLine(scanner)));

        int rows = JDBCTool.update(student, connection);
        System.out.println("修改完成，影响 " + rows + " 行：" + student);
    }

    private static void deleteStudent(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要删除的学生 id：");
        String id = readLine(scanner);
        Student student = new Student();
        student.setId(id);
        int rows = JDBCTool.delete(student, connection);
        System.out.println("删除完成，影响 " + rows + " 行。");
    }

    // ==================== 学院 ====================
    private static void saveCollege(Connection connection, Scanner scanner) throws Exception {
        System.out.print("id：");
        String id = readLine(scanner);
        System.out.print("学院名称 name：");
        String name = readLine(scanner);
        System.out.print("学院代码 code：");
        String code = readLine(scanner);

        College college = new College(id, name, code);
        int rows = JDBCTool.save(college, connection);
        System.out.println("新增成功，影响 " + rows + " 行：" + college);
    }

    private static void listColleges(Connection connection) throws Exception {
        List<College> colleges = selectAll(connection, College.class);
        System.out.println("学院表记录数：" + colleges.size());
        colleges.forEach(System.out::println);
    }

    private static void getCollege(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要查询的学院 id：");
        College college = JDBCTool.getOneById(readLine(scanner), College.class, connection);
        System.out.println(college == null ? "未查询到该学院。" : college);
    }

    private static void updateCollege(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要修改的学院 id：");
        String id = readLine(scanner);
        College college = new College();
        college.setId(id);
        System.out.print("新的学院名称 name：");
        college.setName(readLine(scanner));
        System.out.print("新的学院代码 code：");
        college.setCode(readLine(scanner));

        int rows = JDBCTool.update(college, connection);
        System.out.println("修改完成，影响 " + rows + " 行：" + college);
    }

    private static void deleteCollege(Connection connection, Scanner scanner) throws Exception {
        System.out.print("请输入要删除的学院 id：");
        String id = readLine(scanner);
        College college = new College();
        college.setId(id);
        int rows = JDBCTool.delete(college, connection);
        System.out.println("删除完成，影响 " + rows + " 行。");
    }

    // ==================== 通用辅助 ====================
    private static String readLine(Scanner scanner) {
        return scanner.nextLine().trim();
    }

    private static Boolean parseBoolean(String text) {
        String value = text.trim();
        return "1".equals(value) || "true".equalsIgnoreCase(value)
                || "y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)
                || "是".equals(value);
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
