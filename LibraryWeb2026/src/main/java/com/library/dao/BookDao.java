package com.library.dao;

import com.library.entity.Book;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookDao {

    private static final String COLS = "id,createTime,updateTime,title,author,type,publisher,days,price,stock,status,introduction";

    private Book map(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getString("id"));
        Timestamp createTime = rs.getTimestamp("createTime");
        book.setCreateTime(createTime == null ? null : createTime.toLocalDateTime());
        Timestamp updateTime = rs.getTimestamp("updateTime");
        book.setUpdateTime(updateTime == null ? null : updateTime.toLocalDateTime());
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setType(rs.getString("type"));
        book.setPublisher(rs.getString("publisher"));
        book.setDays(rs.getObject("days", Integer.class));
        book.setPrice(rs.getObject("price", Double.class));
        book.setStock(rs.getObject("stock", Integer.class));
        book.setStatus(rs.getString("status"));
        book.setIntroduction(rs.getString("introduction"));
        return book;
    }

    public List<Book> list(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return JDBCTool.queryList(
                "SELECT " + COLS + " FROM `book` WHERE title LIKE ? OR author LIKE ? OR type LIKE ? OR publisher LIKE ? ORDER BY createTime DESC",
                this::map, like, like, like, like);
    }

    public Book findById(String id) {
        return JDBCTool.queryOne("SELECT " + COLS + " FROM `book` WHERE id = ?", this::map, id);
    }

    public int insert(Book book) {
        try (Connection conn = JDBCTool.getConnection()) {
            return insert(conn, book);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int insert(Connection conn, Book book) throws SQLException {
        return JDBCTool.update(
                conn,
                "INSERT INTO `book`(id,createTime,updateTime,title,author,type,publisher,days,price,stock,status,introduction) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                book.getId(), book.getCreateTime(), book.getUpdateTime(), book.getTitle(), book.getAuthor(),
                book.getType(), book.getPublisher(), book.getDays(), book.getPrice(), book.getStock(),
                book.getStatus(), book.getIntroduction());
    }

    public int update(Book book) {
        return JDBCTool.update(
                "UPDATE `book` SET title = ?, author = ?, type = ?, publisher = ?, days = ?, price = ?, updateTime = ? WHERE id = ?",
                book.getTitle(), book.getAuthor(), book.getType(), book.getPublisher(), book.getDays(),
                book.getPrice(), book.getUpdateTime(), book.getId());
    }

    public int setStatus(String id, String status) {
        return JDBCTool.update("UPDATE `book` SET status = ?, updateTime = ? WHERE id = ?", status, java.time.LocalDateTime.now(), id);
    }

    public int countBorrowedCopies(String bookId) {
        Integer count = JDBCTool.queryOne(
                "SELECT COUNT(*) FROM `bookcopy` WHERE bookId = ? AND isBorrowed = 1",
                rs -> rs.getInt(1), bookId);
        return count == null ? 0 : count;
    }

    public List<Book> findSimilar(String bookName, String author) {
        boolean hasName = bookName != null && !bookName.trim().isEmpty();
        boolean hasAuthor = author != null && !author.trim().isEmpty();
        if (!hasName && !hasAuthor) {
            return new ArrayList<>();
        }
        if (hasName && hasAuthor) {
            String nameLike = "%" + bookName.trim() + "%";
            String authorLike = "%" + author.trim() + "%";
            return JDBCTool.queryList(
                    "SELECT " + COLS + " FROM `book` WHERE title LIKE ? OR author LIKE ? ORDER BY createTime DESC",
                    this::map, nameLike, authorLike);
        }
        if (hasName) {
            String nameLike = "%" + bookName.trim() + "%";
            return JDBCTool.queryList(
                    "SELECT " + COLS + " FROM `book` WHERE title LIKE ? ORDER BY createTime DESC",
                    this::map, nameLike);
        }
        String authorLike = "%" + author.trim() + "%";
        return JDBCTool.queryList(
                "SELECT " + COLS + " FROM `book` WHERE author LIKE ? ORDER BY createTime DESC",
                this::map, authorLike);
    }

    public List<Map<String, Object>> borrowableBooks() {
        return JDBCTool.queryList(
                "SELECT b.id, b.title, b.author, b.type, b.publisher, b.days, b.price, b.stock, b.status, b.introduction, " +
                "COUNT(bc.id) AS availableCopies " +
                "FROM `book` b " +
                "LEFT JOIN `bookcopy` bc ON bc.bookId = b.id AND bc.isBorrowed = 0 " +
                "WHERE b.status = '正常' " +
                "GROUP BY b.id, b.title, b.author, b.type, b.publisher, b.days, b.price, b.stock, b.status, b.introduction, b.createTime " +
                "HAVING COUNT(bc.id) > 0 " +
                "ORDER BY b.createTime DESC",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getString("id"));
                    row.put("title", rs.getString("title"));
                    row.put("author", rs.getString("author"));
                    row.put("type", rs.getString("type"));
                    row.put("publisher", rs.getString("publisher"));
                    row.put("days", rs.getObject("days", Integer.class));
                    row.put("price", rs.getObject("price", Double.class));
                    row.put("stock", rs.getObject("stock", Integer.class));
                    row.put("status", rs.getString("status"));
                    row.put("introduction", rs.getString("introduction"));
                    row.put("availableCopies", rs.getLong("availableCopies"));
                    return row;
                });
    }
}
