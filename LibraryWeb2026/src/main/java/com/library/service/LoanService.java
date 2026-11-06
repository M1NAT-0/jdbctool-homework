package com.library.service;

import com.library.dao.BookCopyDao;
import com.library.dao.BookDao;
import com.library.dao.LoanDao;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoanService {

    private final LoanDao loanDao = new LoanDao();
    private final BookDao bookDao = new BookDao();
    private final BookCopyDao bookCopyDao = new BookCopyDao();

    public Map<String, Object> borrow(String userId, String bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if ("已废弃".equals(book.getStatus())) {
            throw new RuntimeException("图书已废弃，不能借阅");
        }
        BookCopy copy = bookCopyDao.findAvailableByBook(bookId);
        if (copy == null) {
            throw new RuntimeException("该图书暂无可用副本");
        }

        LocalDateTime now = LocalDateTime.now();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setCreateTime(now);
        loan.setUpdateTime(now);
        loan.setBookCopy(copy);
        User user = new UserPlaceholder();
        user.setId(userId);
        loan.setUser(user);
        loan.setBorrowDate(now);
        int days = book.getDays() == null ? 30 : book.getDays();
        loan.setDueDate(now.plusDays(days));
        loan.setStatus("未还");

        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            bookCopyDao.setBorrowed(conn, copy.getId(), true);
            loanDao.insert(conn, loan);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("借书失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("loanId", loan.getId());
        result.put("bookCopyId", copy.getId());
        result.put("bookTitle", book.getTitle());
        result.put("borrowDate", now.toString());
        result.put("dueDate", loan.getDueDate().toString());
        return result;
    }

    public void returnBook(String userId, String bookCopyId) {
        Map<String, Object> active = loanDao.findActiveByCopyAndUser(bookCopyId, userId);
        if (active == null) {
            throw new RuntimeException("没有找到该副本对应的未还记录");
        }
        String loanId = (String) active.get("id");
        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            loanDao.returnLoan(conn, loanId, LocalDateTime.now());
            bookCopyDao.setBorrowed(conn, bookCopyId, false);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("还书失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }
    }

    public List<Map<String, Object>> myLoans(String userId) {
        return loanDao.listByUser(userId);
    }

    public List<Map<String, Object>> myUnreturned(String userId) {
        return loanDao.listUnreturnedByUser(userId);
    }

    public List<Map<String, Object>> myDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return loanDao.listByUserDateRange(userId, start, end);
    }

    public List<Map<String, Object>> adminKeyword(String keyword) {
        return loanDao.adminKeyword(keyword);
    }

    public List<Map<String, Object>> adminOverdue(String keyword) {
        return loanDao.adminOverdue(keyword);
    }

    public List<Map<String, Object>> adminDateRange(LocalDateTime start, LocalDateTime end) {
        return loanDao.adminDateRange(start, end);
    }

    public List<Map<String, Object>> countByType() {
        return loanDao.countByType();
    }

    public List<Map<String, Object>> countByMajor() {
        return loanDao.countByMajor();
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
        }
    }

    private static class UserPlaceholder extends User {
        @Override
        public String[] hasPermission() {
            return new String[0];
        }
    }
}
