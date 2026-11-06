package com.library.servlet;

import com.google.gson.JsonObject;
import com.library.common.Result;
import com.library.dao.BookDao;
import com.library.entity.User;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/loans", "/api/loans/*"})
public class LoanServlet extends BaseServlet {

    private final LoanService loanService = new LoanService();
    private final BookDao bookDao = new BookDao();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            writeJson(resp, Result.fail(404, "Not Found"));
            return;
        }
        if ("/mine".equals(pathInfo)) {
            mine(req, resp);
        } else if ("/borrowable".equals(pathInfo)) {
            borrowable(req, resp);
        } else if ("/admin".equals(pathInfo)) {
            admin(req, resp);
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/borrow".equals(pathInfo)) {
            borrow(req, resp);
        } else if ("/return".equals(pathInfo)) {
            returnBook(req, resp);
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }

    private void borrow(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        JsonObject body = readJson(req);
        String bookId = jsonString(body, "bookId");
        try {
            Map<String, Object> result = loanService.borrow(currentUser(req).getId(), bookId);
            writeJson(resp, Result.ok(result));
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void returnBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        JsonObject body = readJson(req);
        String bookCopyId = jsonString(body, "bookCopyId");
        try {
            loanService.returnBook(currentUser(req).getId(), bookCopyId);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void mine(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        User user = currentUser(req);
        String mode = req.getParameter("mode");
        try {
            if ("unreturned".equals(mode)) {
                writeJson(resp, Result.ok(loanService.myUnreturned(user.getId())));
            } else if ("range".equals(mode)) {
                LocalDateTime start = parseTime(req.getParameter("start"));
                LocalDateTime end = parseTime(req.getParameter("end"));
                if (start == null || end == null) {
                    writeJson(resp, Result.fail("请选择开始和结束时间"));
                    return;
                }
                writeJson(resp, Result.ok(loanService.myDateRange(user.getId(), start, end)));
            } else {
                writeJson(resp, Result.ok(loanService.myLoans(user.getId())));
            }
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void borrowable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        writeJson(resp, Result.ok(bookDao.borrowableBooks()));
    }

    private void admin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        String mode = req.getParameter("mode");
        String keyword = req.getParameter("keyword");
        try {
            if ("overdue".equals(mode)) {
                writeJson(resp, Result.ok(loanService.adminOverdue(keyword)));
            } else if ("range".equals(mode)) {
                LocalDateTime start = parseTime(req.getParameter("start"));
                LocalDateTime end = parseTime(req.getParameter("end"));
                if (start == null || end == null) {
                    writeJson(resp, Result.fail("请选择开始和结束时间"));
                    return;
                }
                writeJson(resp, Result.ok(loanService.adminDateRange(start, end)));
            } else {
                writeJson(resp, Result.ok(loanService.adminKeyword(keyword)));
            }
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().replace("T", " ");
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        try {
            return LocalDateTime.parse(normalized, FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("日期格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }
}
