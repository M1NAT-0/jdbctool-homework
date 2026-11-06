package com.library.servlet;

import com.google.gson.JsonObject;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/books", "/api/books/*"})
public class BookServlet extends BaseServlet {

    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            String keyword = req.getParameter("keyword");
            List<Book> books = bookService.list(keyword);
            writeJson(resp, Result.ok(books));
            return;
        }
        String id = pathInfo.substring(1);
        try {
            writeJson(resp, Result.ok(bookService.get(id)));
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/") && pathInfo.endsWith("/discard")) {
            discard(req, resp, pathInfo);
            return;
        }
        if (!requireRole(req, resp, "操作员")) {
            return;
        }
        JsonObject body = readJson(req);
        Book book = new Book();
        book.setTitle(jsonString(body, "title"));
        book.setAuthor(jsonString(body, "author"));
        book.setType(jsonString(body, "type"));
        book.setPublisher(jsonString(body, "publisher"));
        book.setDays(jsonInt(body, "days"));
        book.setPrice(jsonDouble(body, "price"));
        book.setStock(jsonInt(body, "stock"));
        book.setIntroduction(jsonString(body, "introduction"));
        try {
            bookService.add(book);
            writeJson(resp, Result.ok(book));
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireRole(req, resp, "操作员")) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            writeJson(resp, Result.fail("缺少图书ID"));
            return;
        }
        String id = pathInfo.substring(1);
        JsonObject body = readJson(req);
        Book book = new Book();
        book.setId(id);
        book.setTitle(jsonString(body, "title"));
        book.setAuthor(jsonString(body, "author"));
        book.setType(jsonString(body, "type"));
        book.setPublisher(jsonString(body, "publisher"));
        book.setDays(jsonInt(body, "days"));
        book.setPrice(jsonDouble(body, "price"));
        try {
            bookService.edit(book);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }

    private void discard(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        if (!requireRole(req, resp, "操作员")) {
            return;
        }
        String id = pathInfo.substring(1, pathInfo.length() - "/discard".length());
        try {
            bookService.discard(id);
            writeJson(resp, Result.ok());
        } catch (RuntimeException e) {
            writeJson(resp, Result.fail(e.getMessage()));
        }
    }
}
