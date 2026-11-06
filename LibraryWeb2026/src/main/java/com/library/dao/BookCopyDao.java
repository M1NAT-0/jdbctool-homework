package com.library.dao;

import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class BookCopyDao {

    private BookCopy map(ResultSet rs) throws SQLException {
        BookCopy copy = new BookCopy();
        copy.setId(rs.getString("id"));
        Timestamp createTime = rs.getTimestamp("createTime");
        copy.setCreateTime(createTime == null ? null : createTime.toLocalDateTime());
        Timestamp updateTime = rs.getTimestamp("updateTime");
        copy.setUpdateTime(updateTime == null ? null : updateTime.toLocalDateTime());
        Book book = new Book();
        book.setId(rs.getString("bookId"));
        copy.setBook(book);
        Boolean borrowed = rs.getObject("isBorrowed", Boolean.class);
        copy.setBorrowed(borrowed != null && borrowed);
        return copy;
    }

    public int insert(BookCopy copy) {
        try (Connection conn = JDBCTool.getConnection()) {
            return insert(conn, copy);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int insert(Connection conn, BookCopy copy) throws SQLException {
        return JDBCTool.update(
                conn,
                "INSERT INTO `bookcopy`(id,createTime,updateTime,bookId,isBorrowed) VALUES(?,?,?,?,?)",
                copy.getId(), copy.getCreateTime(), copy.getUpdateTime(),
                copy.getBook() == null ? null : copy.getBook().getId(), copy.isBorrowed());
    }

    public int delete(String id) {
        try (Connection conn = JDBCTool.getConnection()) {
            return delete(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int delete(Connection conn, String id) throws SQLException {
        return JDBCTool.update(conn, "DELETE FROM `bookcopy` WHERE id = ?", id);
    }

    public BookCopy findById(String id) {
        return JDBCTool.queryOne(
                "SELECT id,createTime,updateTime,bookId,isBorrowed FROM `bookcopy` WHERE id = ?", this::map, id);
    }

    public BookCopy findAvailableByBook(String bookId) {
        return JDBCTool.queryOne(
                "SELECT id,createTime,updateTime,bookId,isBorrowed FROM `bookcopy` WHERE bookId = ? AND isBorrowed = 0 ORDER BY createTime ASC LIMIT 1",
                this::map, bookId);
    }

    public int setBorrowed(String id, boolean borrowed) {
        try (Connection conn = JDBCTool.getConnection()) {
            return setBorrowed(conn, id, borrowed);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int setBorrowed(Connection conn, String id, boolean borrowed) throws SQLException {
        return JDBCTool.update(conn, "UPDATE `bookcopy` SET isBorrowed = ?, updateTime = ? WHERE id = ?",
                borrowed, java.time.LocalDateTime.now(), id);
    }

    public int countByBook(String bookId) {
        Integer count = JDBCTool.queryOne("SELECT COUNT(*) FROM `bookcopy` WHERE bookId = ?",
                rs -> rs.getInt(1), bookId);
        return count == null ? 0 : count;
    }

    public List<BookCopy> listByBook(String bookId) {
        return JDBCTool.queryList(
                "SELECT id,createTime,updateTime,bookId,isBorrowed FROM `bookcopy` WHERE bookId = ? ORDER BY createTime ASC",
                this::map, bookId);
    }
}
