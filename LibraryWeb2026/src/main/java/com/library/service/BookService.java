package com.library.service;

import com.library.dao.BookCopyDao;
import com.library.dao.BookDao;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookService {

    private final BookDao bookDao = new BookDao();
    private final BookCopyDao bookCopyDao = new BookCopyDao();

    public List<Book> list(String keyword) {
        return bookDao.list(keyword);
    }

    public Book get(String id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        book.setBookCopies(bookCopyDao.listByBook(id));
        return book;
    }

    public void add(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new RuntimeException("书名不能为空");
        }
        int stock = book.getStock() == null ? 0 : book.getStock();
        if (stock < 0) {
            throw new RuntimeException("库存不能为负数");
        }
        book.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        book.setCreateTime(now);
        book.setUpdateTime(now);
        if (book.getStatus() == null || book.getStatus().isEmpty()) {
            book.setStatus("正常");
        }

        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            bookDao.insert(conn, book);
            for (int i = 0; i < stock; i++) {
                BookCopy copy = new BookCopy();
                copy.setId(UUID.randomUUID().toString());
                copy.setCreateTime(now);
                copy.setUpdateTime(now);
                Book ref = new Book();
                ref.setId(book.getId());
                copy.setBook(ref);
                copy.setBorrowed(false);
                bookCopyDao.insert(conn, copy);
            }
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("添加图书失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }
    }

    public void edit(Book book) {
        if (book.getId() == null) {
            throw new RuntimeException("缺少图书ID");
        }
        Book existing = bookDao.findById(book.getId());
        if (existing == null) {
            throw new RuntimeException("图书不存在");
        }
        book.setUpdateTime(LocalDateTime.now());
        bookDao.update(book);
    }

    public void discard(String id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if ("已废弃".equals(book.getStatus())) {
            throw new RuntimeException("图书已经废弃");
        }
        if (bookDao.countBorrowedCopies(id) > 0) {
            throw new RuntimeException("该图书仍有外借副本，不能废弃");
        }
        bookDao.setStatus(id, "已废弃");
    }

    public void addCopy(String bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            BookCopy copy = new BookCopy();
            LocalDateTime now = LocalDateTime.now();
            copy.setId(UUID.randomUUID().toString());
            copy.setCreateTime(now);
            copy.setUpdateTime(now);
            Book ref = new Book();
            ref.setId(bookId);
            copy.setBook(ref);
            copy.setBorrowed(false);
            bookCopyDao.insert(conn, copy);
            JDBCTool.update(conn, "UPDATE `book` SET stock = stock + 1, updateTime = ? WHERE id = ?", now, bookId);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("新增副本失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }
    }

    public void deleteCopy(String copyId) {
        BookCopy copy = bookCopyDao.findById(copyId);
        if (copy == null) {
            throw new RuntimeException("副本不存在");
        }
        if (copy.isBorrowed()) {
            throw new RuntimeException("该副本处于借阅状态，不能删除");
        }
        String bookId = copy.getBook() == null ? null : copy.getBook().getId();
        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            bookCopyDao.delete(conn, copyId);
            if (bookId != null) {
                JDBCTool.update(conn, "UPDATE `book` SET stock = stock - 1, updateTime = ? WHERE id = ?",
                        LocalDateTime.now(), bookId);
            }
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("删除副本失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }
    }

    public List<Book> findSimilar(String bookName, String author) {
        return bookDao.findSimilar(bookName, author);
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
        }
    }
}
