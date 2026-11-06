package com.library.servlet;

import com.google.gson.JsonObject;
import com.library.common.Result;
import com.library.entity.BookRecommend;
import com.library.service.RecommendService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/api/recommend", "/api/recommend/*"})
public class RecommendServlet extends BaseServlet {

    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        writeJson(resp, Result.ok(recommendService.listPending()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            recommend(req, resp);
            return;
        }
        if (pathInfo.endsWith("/approve")) {
            approve(req, resp, pathInfo);
        } else if (pathInfo.endsWith("/reject")) {
            reject(req, resp, pathInfo);
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }

    private void recommend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "教师")) {
            return;
        }
        JsonObject body = readJson(req);
        BookRecommend recommend = new BookRecommend();
        recommend.setBookName(jsonString(body, "bookName"));
        recommend.setAuthor(jsonString(body, "author"));
        recommend.setPublisher(jsonString(body, "publisher"));
        recommend.setPrice(jsonDouble(body, "price"));
        if (recommend.getBookName() == null || recommend.getBookName().trim().isEmpty()) {
            writeJson(resp, Result.fail("书名不能为空"));
            return;
        }
        try {
            writeJson(resp, Result.ok(recommendService.recommend(currentUser(req).getId(), recommend)));
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void approve(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        String id = pathInfo.substring(1, pathInfo.length() - "/approve".length());
        try {
            recommendService.approve(id);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void reject(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        String id = pathInfo.substring(1, pathInfo.length() - "/reject".length());
        JsonObject body = readJson(req);
        String reason = jsonString(body, "reason");
        try {
            recommendService.reject(id, reason);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }
}
