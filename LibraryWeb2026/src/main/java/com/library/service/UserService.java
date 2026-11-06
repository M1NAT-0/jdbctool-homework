package com.library.service;

import com.library.dao.UserDao;
import com.library.entity.Admin;
import com.library.entity.Operator;
import com.library.entity.Student;
import com.library.entity.Teacher;
import com.library.entity.User;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserService {

    private final UserDao userDao = new UserDao();

    public void register(User user) {
        user.setName(user.getName() == null ? null : user.getName().trim());
        user.setPhone(user.getPhone() == null ? null : user.getPhone().trim());
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!user.getPhone().matches("1\\d{10}")) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (userDao.findByName(user.getName().trim()) != null) {
            throw new RuntimeException("同名账号不能重复注册");
        }
        if (userDao.findByPhone(user.getPhone().trim()) != null) {
            throw new RuntimeException("手机号不能重复");
        }
        user.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userDao.insert(user);
    }

    public User login(String name, String password) {
        User user = userDao.findByLogin(name, password);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        userDao.updateLoginTime(user.getId(), LocalDateTime.now());
        user.setLoginTime(LocalDateTime.now());
        return user;
    }

    public List<User> queryUsers(String keyword) {
        return userDao.query(keyword);
    }

    public User getUser(String id) {
        return userDao.findById(id);
    }

    public Map<String, Object> toInfo(User user) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("name", user.getName());
        info.put("phone", user.getPhone());
        info.put("role", user.getRole());
        info.put("major", user.getMajor());
        info.put("createTime", user.getCreateTime() == null ? null : user.getCreateTime().toString());
        info.put("loginTime", user.getLoginTime() == null ? null : user.getLoginTime().toString());
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            info.put("permissionLevel", admin.getPermissionLevel());
            info.put("entryTime", admin.getEntryTime() == null ? null : admin.getEntryTime().toString());
        } else if (user instanceof Operator) {
            Operator operator = (Operator) user;
            info.put("duty", operator.getDuty());
            info.put("area", operator.getArea());
        } else if (user instanceof Student) {
            Student student = (Student) user;
            info.put("grade", student.getGrade());
            info.put("college", student.getCollege());
        } else if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            info.put("college", teacher.getCollege());
            info.put("title", teacher.getTitle());
        }
        info.put("permissions", user.hasPermission());
        return info;
    }
}
