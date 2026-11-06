package com.library.servlet;

import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/users", "/api/users/*"})
public class UserServlet extends BaseServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            String keyword = req.getParameter("keyword");
            List<User> users = userService.queryUsers(keyword);
            List<Map<String, Object>> list = new ArrayList<>();
            for (User user : users) {
                list.add(userService.toInfo(user));
            }
            writeJson(resp, Result.ok(list));
            return;
        }
        String id = pathInfo.substring(1);
        User user = userService.getUser(id);
        if (user == null) {
            writeJson(resp, Result.fail("用户不存在"));
            return;
        }
        writeJson(resp, Result.ok(userService.toInfo(user)));
    }
}
