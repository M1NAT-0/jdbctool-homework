package com.library.dao;

import com.library.entity.BookRecommend;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookRecommendDao {

    public int insert(BookRecommend recommend) {
        return JDBCTool.update(
                "INSERT INTO `bookrecommend`(id,createTime,updateTime,userId,bookName,author,publisher,price,status,reason) VALUES(?,?,?,?,?,?,?,?,?,?)",
                recommend.getId(), recommend.getCreateTime(), recommend.getUpdateTime(), recommend.getUserId(),
                recommend.getBookName(), recommend.getAuthor(), recommend.getPublisher(), recommend.getPrice(),
                recommend.getStatus(), recommend.getReason());
    }

    public BookRecommend findById(String id) {
        return JDBCTool.queryOne(
                "SELECT id,createTime,updateTime,userId,bookName,author,publisher,price,status,reason FROM `bookrecommend` WHERE id = ?",
                this::map, id);
    }

    private BookRecommend map(ResultSet rs) throws SQLException {
        BookRecommend r = new BookRecommend();
        r.setId(rs.getString("id"));
        Timestamp createTime = rs.getTimestamp("createTime");
        r.setCreateTime(createTime == null ? null : createTime.toLocalDateTime());
        Timestamp updateTime = rs.getTimestamp("updateTime");
        r.setUpdateTime(updateTime == null ? null : updateTime.toLocalDateTime());
        r.setUserId(rs.getString("userId"));
        r.setBookName(rs.getString("bookName"));
        r.setAuthor(rs.getString("author"));
        r.setPublisher(rs.getString("publisher"));
        r.setPrice(rs.getObject("price", Double.class));
        r.setStatus(rs.getString("status"));
        r.setReason(rs.getString("reason"));
        return r;
    }

    public List<Map<String, Object>> listPending() {
        return JDBCTool.queryList(
                "SELECT r.id, r.createTime, r.userId, r.bookName, r.author, r.publisher, r.price, r.status, r.reason, u.name AS userName " +
                "FROM `bookrecommend` r JOIN `user` u ON r.userId = u.id WHERE r.status = '待审核' ORDER BY r.createTime DESC",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getString("id"));
                    Timestamp createTime = rs.getTimestamp("createTime");
                    row.put("createTime", createTime == null ? null : createTime.toLocalDateTime().toString());
                    row.put("userId", rs.getString("userId"));
                    row.put("bookName", rs.getString("bookName"));
                    row.put("author", rs.getString("author"));
                    row.put("publisher", rs.getString("publisher"));
                    row.put("price", rs.getObject("price", Double.class));
                    row.put("status", rs.getString("status"));
                    row.put("reason", rs.getString("reason"));
                    row.put("userName", rs.getString("userName"));
                    return row;
                });
    }

    public int updateStatus(String id, String status, String reason) {
        try (Connection conn = JDBCTool.getConnection()) {
            return updateStatus(conn, id, status, reason);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }

    public int updateStatus(Connection conn, String id, String status, String reason) throws SQLException {
        return JDBCTool.update(conn, "UPDATE `bookrecommend` SET status = ?, reason = ?, updateTime = ? WHERE id = ?",
                status, reason, java.time.LocalDateTime.now(), id);
    }
}
