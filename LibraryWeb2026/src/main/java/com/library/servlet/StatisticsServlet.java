package com.library.servlet;

import com.library.common.Result;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/stats/*")
public class StatisticsServlet extends BaseServlet {

    private final LoanService loanService = new LoanService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "管理员")) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if ("/by-type".equals(pathInfo)) {
            writeJson(resp, Result.ok(loanService.countByType()));
        } else if ("/by-major".equals(pathInfo)) {
            writeJson(resp, Result.ok(loanService.countByMajor()));
        } else {
            writeJson(resp, Result.fail(404, "Not Found"));
        }
    }
}
