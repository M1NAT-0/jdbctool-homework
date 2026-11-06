package com.library.servlet;

import com.google.gson.JsonObject;
import com.library.common.Result;
import com.library.entity.Admin;
import com.library.entity.Operator;
import com.library.entity.Student;
import com.library.entity.Teacher;
import com.library.entity.User;
import com.library.service.UserService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

@WebServlet(urlPatterns = {"/api/login", "/api/logout", "/api/captcha", "/api/me", "/api/register"})
public class AuthServlet extends BaseServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if ("/api/captcha".equals(path)) {
            captcha(req, resp);
        } else if ("/api/me".equals(path)) {
            me(req, resp);
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if ("/api/login".equals(path)) {
            login(req, resp);
        } else if ("/api/register".equals(path)) {
            register(req, resp);
        } else if ("/api/logout".equals(path)) {
            logout(req, resp);
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJson(req);
        String name = jsonString(body, "name");
        String password = jsonString(body, "password");
        String captcha = jsonString(body, "captcha");
        HttpSession session = req.getSession();
        String expected = (String) session.getAttribute("captcha");
        if (expected == null || captcha == null || !expected.equalsIgnoreCase(captcha.trim())) {
            writeJson(resp, Result.fail("验证码错误"));
            return;
        }
        session.removeAttribute("captcha");
        try {
            User user = userService.login(name, password);
            session.setAttribute("user", user);
            writeJson(resp, Result.ok(userService.toInfo(user)));
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJson(req);
        String role = jsonString(body, "role");
        User user = buildUser(role, body);
        try {
            userService.register(user);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeJson(resp, Result.ok());
    }

    private void me(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        if (user == null) {
            writeJson(resp, Result.fail(401, "未登录"));
            return;
        }
        writeJson(resp, Result.ok(userService.toInfo(user)));
    }

    private User buildUser(String role, JsonObject body) {
        User user;
        if ("管理员".equals(role)) {
            Admin admin = new Admin();
            admin.setPermissionLevel(jsonInt(body, "permissionLevel"));
            String entryTime = jsonString(body, "entryTime");
            admin.setEntryTime(entryTime == null || entryTime.isEmpty() ? null : LocalDate.parse(entryTime));
            user = admin;
        } else if ("操作员".equals(role)) {
            Operator operator = new Operator();
            operator.setDuty(jsonString(body, "duty"));
            operator.setArea(jsonString(body, "area"));
            user = operator;
        } else if ("教师".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setCollege(jsonString(body, "college"));
            teacher.setTitle(jsonString(body, "title"));
            user = teacher;
        } else {
            Student student = new Student();
            student.setGrade(jsonString(body, "grade"));
            student.setCollege(jsonString(body, "college"));
            user = student;
            role = "学生";
        }
        user.setName(jsonString(body, "name"));
        user.setPassword(jsonString(body, "password"));
        user.setPhone(jsonString(body, "phone"));
        user.setRole(role);
        user.setMajor(jsonString(body, "major"));
        return user;
    }

    private void captcha(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(245, 245, 245));
        graphics.fillRect(0, 0, width, height);
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        graphics.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < 4; i++) {
            char c = chars.charAt(random.nextInt(chars.length()));
            code.append(c);
            graphics.setColor(new Color(30 + random.nextInt(130), 30 + random.nextInt(130), 30 + random.nextInt(130)));
            graphics.drawString(String.valueOf(c), 15 + i * 25, 28 + random.nextInt(6));
        }
        for (int i = 0; i < 5; i++) {
            graphics.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        graphics.dispose();
        req.getSession().setAttribute("captcha", code.toString());
        resp.setContentType("image/png");
        ImageIO.write(image, "png", resp.getOutputStream());
    }
}
