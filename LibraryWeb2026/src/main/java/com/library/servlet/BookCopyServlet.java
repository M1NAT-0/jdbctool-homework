package com.library.servlet;

import com.library.common.Result;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/api/bookcopies", "/api/bookcopies/*"})
public class BookCopyServlet extends BaseServlet {

    private final BookService bookService = new BookService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "操作员")) {
            return;
        }
        String bookId = req.getParameter("bookId");
        if (bookId == null || bookId.isEmpty()) {
            writeJson(resp, Result.fail("缺少图书ID"));
            return;
        }
        try {
            bookService.addCopy(bookId);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "操作员")) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            writeJson(resp, Result.fail("缺少副本ID"));
            return;
        }
        String id = pathInfo.substring(1);
        try {
            bookService.deleteCopy(id);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }
}
