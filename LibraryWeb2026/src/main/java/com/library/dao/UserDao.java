package com.library.dao;

import com.library.entity.Admin;
import com.library.entity.Operator;
import com.library.entity.Student;
import com.library.entity.Teacher;
import com.library.entity.User;
import com.library.util.JDBCTool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserDao {

    private static final String[] COLUMNS = {
            "id", "createTime", "updateTime", "name", "password", "phone", "role", "major",
            "loginTime", "permissionLevel", "entryTime", "duty", "area", "grade", "college", "title"
    };

    private static final String COLS = String.join(",", COLUMNS);

    private User map(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;
        if ("管理员".equals(role)) {
            Admin admin = new Admin();
            Integer level = rs.getObject("permissionLevel", Integer.class);
            admin.setPermissionLevel(level);
            java.sql.Date entry = rs.getDate("entryTime");
            admin.setEntryTime(entry == null ? null : entry.toLocalDate());
            user = admin;
        } else if ("操作员".equals(role)) {
            Operator operator = new Operator();
            operator.setArea(rs.getString("area"));
            operator.setDuty(rs.getString("duty"));
            user = operator;
        } else if ("教师".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setCollege(rs.getString("college"));
            teacher.setTitle(rs.getString("title"));
            user = teacher;
        } else {
            Student student = new Student();
            student.setGrade(rs.getString("grade"));
            student.setCollege(rs.getString("college"));
            user = student;
        }
        user.setId(rs.getString("id"));
        Timestamp createTime = rs.getTimestamp("createTime");
        user.setCreateTime(createTime == null ? null : createTime.toLocalDateTime());
        Timestamp updateTime = rs.getTimestamp("updateTime");
        user.setUpdateTime(updateTime == null ? null : updateTime.toLocalDateTime());
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(role);
        user.setMajor(rs.getString("major"));
        Timestamp loginTime = rs.getTimestamp("loginTime");
        user.setLoginTime(loginTime == null ? null : loginTime.toLocalDateTime());
        return user;
    }

    public User findByLogin(String name, String password) {
        return JDBCTool.queryOne("SELECT " + COLS + " FROM `user` WHERE name = ? AND password = ?",
                this::map, name, password);
    }

    public User findByName(String name) {
        return JDBCTool.queryOne("SELECT " + COLS + " FROM `user` WHERE name = ?", this::map, name);
    }

    public User findByPhone(String phone) {
        return JDBCTool.queryOne("SELECT " + COLS + " FROM `user` WHERE phone = ?", this::map, phone);
    }

    public User findById(String id) {
        return JDBCTool.queryOne("SELECT " + COLS + " FROM `user` WHERE id = ?", this::map, id);
    }

    public List<User> query(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return JDBCTool.queryList(
                "SELECT " + COLS + " FROM `user` WHERE name LIKE ? OR phone LIKE ? OR role LIKE ? OR major LIKE ? ORDER BY createTime DESC",
                this::map, like, like, like, like);
    }

    public int insert(User user) {
        StringBuilder sql = new StringBuilder("INSERT INTO `user`(");
        for (int i = 0; i < COLUMNS.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(COLUMNS[i]);
        }
        sql.append(") VALUES(");
        for (int i = 0; i < COLUMNS.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        Object[] params = new Object[COLUMNS.length];
        params[0] = user.getId();
        params[1] = user.getCreateTime();
        params[2] = user.getUpdateTime();
        params[3] = user.getName();
        params[4] = user.getPassword();
        params[5] = user.getPhone();
        params[6] = user.getRole();
        params[7] = user.getMajor();
        params[8] = user.getLoginTime();
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            params[9] = admin.getPermissionLevel();
            params[10] = admin.getEntryTime();
        } else if (user instanceof Operator) {
            Operator operator = (Operator) user;
            params[11] = operator.getDuty();
            params[12] = operator.getArea();
        } else if (user instanceof Student) {
            Student student = (Student) user;
            params[13] = student.getGrade();
            params[14] = student.getCollege();
        } else if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            params[14] = teacher.getCollege();
            params[15] = teacher.getTitle();
        }
        return JDBCTool.update(sql.toString(), params);
    }

    public int updateLoginTime(String id, LocalDateTime loginTime) {
        return JDBCTool.update("UPDATE `user` SET loginTime = ? WHERE id = ?", loginTime, id);
    }
}
