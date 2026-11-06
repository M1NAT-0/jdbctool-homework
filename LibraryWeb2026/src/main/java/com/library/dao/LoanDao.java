package com.library.dao;

import com.library.entity.Loan;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoanDao {

    private static final String JOIN_SQL =
            "SELECT l.id, l.createTime, l.updateTime, l.bookCopyId, l.userId, l.borrowDate, l.returnDate, l.dueDate, l.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor, b.type AS bookType, u.name AS userName " +
            "FROM `loan` l " +
            "JOIN `bookcopy` bc ON l.bookCopyId = bc.id " +
            "JOIN `book` b ON bc.bookId = b.id " +
            "JOIN `user` u ON l.userId = u.id ";

    private Map<String, Object> mapLoan(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        Timestamp createTime = rs.getTimestamp("createTime");
        row.put("createTime", createTime == null ? null : createTime.toLocalDateTime().toString());
        Timestamp borrowDate = rs.getTimestamp("borrowDate");
        LocalDateTime borrow = borrowDate == null ? null : borrowDate.toLocalDateTime();
        Timestamp returnDate = rs.getTimestamp("returnDate");
        LocalDateTime returned = returnDate == null ? null : returnDate.toLocalDateTime();
        Timestamp dueDate = rs.getTimestamp("dueDate");
        LocalDateTime due = dueDate == null ? null : dueDate.toLocalDateTime();
        row.put("bookCopyId", rs.getString("bookCopyId"));
        row.put("userId", rs.getString("userId"));
        row.put("borrowDate", borrow == null ? null : borrow.toString());
        row.put("returnDate", returned == null ? null : returned.toString());
        row.put("dueDate", due == null ? null : due.toString());
        String status = rs.getString("status");
        row.put("status", status);
        row.put("bookTitle", rs.getString("bookTitle"));
        row.put("bookAuthor", rs.getString("bookAuthor"));
        row.put("bookType", rs.getString("bookType"));
        row.put("userName", rs.getString("userName"));
        boolean overdue = "未还".equals(status) && due != null && due.isBefore(LocalDateTime.now());
        row.put("overdue", overdue);
        return row;
    }

    public int insert(Loan loan) {
        try (Connection conn = JDBCTool.getConnection()) {
            return insert(conn, loan);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int insert(Connection conn, Loan loan) throws SQLException {
        return JDBCTool.update(
                conn,
                "INSERT INTO `loan`(id,createTime,updateTime,bookCopyId,userId,borrowDate,returnDate,dueDate,status) VALUES(?,?,?,?,?,?,?,?,?)",
                loan.getId(), loan.getCreateTime(), loan.getUpdateTime(),
                loan.getBookCopy() == null ? null : loan.getBookCopy().getId(),
                loan.getUser() == null ? null : loan.getUser().getId(),
                loan.getBorrowDate(), loan.getReturnDate(), loan.getDueDate(), loan.getStatus());
    }

    public List<Map<String, Object>> listByUser(String userId) {
        return JDBCTool.queryList(JOIN_SQL + "WHERE l.userId = ? ORDER BY l.borrowDate DESC", this::mapLoan, userId);
    }

    public List<Map<String, Object>> listUnreturnedByUser(String userId) {
        return JDBCTool.queryList(JOIN_SQL + "WHERE l.userId = ? AND l.status = '未还' ORDER BY l.borrowDate DESC", this::mapLoan, userId);
    }

    public List<Map<String, Object>> listByUserDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return JDBCTool.queryList(JOIN_SQL + "WHERE l.userId = ? AND l.borrowDate BETWEEN ? AND ? ORDER BY l.borrowDate DESC",
                this::mapLoan, userId, start, end);
    }

    public Map<String, Object> findActiveByCopyAndUser(String bookCopyId, String userId) {
        return JDBCTool.queryOne(JOIN_SQL + "WHERE l.bookCopyId = ? AND l.userId = ? AND l.status = '未还' LIMIT 1",
                this::mapLoan, bookCopyId, userId);
    }

    public int returnLoan(String id, LocalDateTime returnDate) {
        try (Connection conn = JDBCTool.getConnection()) {
            return returnLoan(conn, id, returnDate);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int returnLoan(Connection conn, String id, LocalDateTime returnDate) throws SQLException {
        return JDBCTool.update(conn, "UPDATE `loan` SET returnDate = ?, status = '已还', updateTime = ? WHERE id = ?",
                returnDate, LocalDateTime.now(), id);
    }

    public List<Map<String, Object>> adminKeyword(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return JDBCTool.queryList(JOIN_SQL + "WHERE u.name LIKE ? OR b.title LIKE ? ORDER BY l.borrowDate DESC",
                this::mapLoan, like, like);
    }

    public List<Map<String, Object>> adminOverdue(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return JDBCTool.queryList(JOIN_SQL + "WHERE l.status = '未还' AND l.dueDate < NOW() AND (u.name LIKE ? OR b.title LIKE ?) ORDER BY l.dueDate ASC",
                this::mapLoan, like, like);
    }

    public List<Map<String, Object>> adminDateRange(LocalDateTime start, LocalDateTime end) {
        return JDBCTool.queryList(JOIN_SQL + "WHERE l.borrowDate BETWEEN ? AND ? ORDER BY l.borrowDate DESC",
                this::mapLoan, start, end);
    }

    public List<Map<String, Object>> countByType() {
        return JDBCTool.queryList(
                "SELECT b.type AS name, COUNT(l.id) AS cnt FROM `book` b " +
                "LEFT JOIN `bookcopy` bc ON bc.bookId = b.id " +
                "LEFT JOIN `loan` l ON l.bookCopyId = bc.id " +
                "GROUP BY b.type ORDER BY cnt DESC",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", rs.getString("name"));
                    row.put("cnt", rs.getLong("cnt"));
                    return row;
                });
    }

    public List<Map<String, Object>> countByMajor() {
        return JDBCTool.queryList(
                "SELECT u.major AS name, COUNT(l.id) AS cnt FROM `loan` l JOIN `user` u ON l.userId = u.id " +
                "GROUP BY u.major ORDER BY cnt DESC",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", rs.getString("name"));
                    row.put("cnt", rs.getLong("cnt"));
                    return row;
                });
    }
}
