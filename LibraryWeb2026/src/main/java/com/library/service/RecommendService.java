package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BookCopyDao;
import com.library.dao.BookRecommendDao;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookRecommend;
import com.library.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecommendService {

    private final BookDao bookDao = new BookDao();
    private final BookCopyDao bookCopyDao = new BookCopyDao();
    private final BookRecommendDao recommendDao = new BookRecommendDao();

    public Map<String, Object> recommend(String userId, BookRecommend recommend) {
        List<Book> similar = bookDao.findSimilar(recommend.getBookName(), recommend.getAuthor());
        Map<String, Object> result = new LinkedHashMap<>();
        if (!similar.isEmpty()) {
            result.put("exists", true);
            result.put("books", similar);
            result.put("message", "图书馆已存在相关书籍");
            return result;
        }
        recommend.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        recommend.setCreateTime(now);
        recommend.setUpdateTime(now);
        recommend.setUserId(userId);
        recommend.setStatus("待审核");
        recommendDao.insert(recommend);
        result.put("exists", false);
        result.put("submitted", true);
        result.put("message", "图书馆暂无此书，已自动提交到图书审核");
        return result;
    }

    public List<Map<String, Object>> listPending() {
        return recommendDao.listPending();
    }

    public void approve(String id) {
        BookRecommend recommend = recommendDao.findById(id);
        if (recommend == null) {
            throw new RuntimeException("推荐记录不存在");
        }
        if (!"待审核".equals(recommend.getStatus())) {
            throw new RuntimeException("该推荐已处理");
        }

        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        book.setCreateTime(now);
        book.setUpdateTime(now);
        book.setTitle(recommend.getBookName());
        book.setAuthor(recommend.getAuthor());
        book.setType("未分类");
        book.setPublisher(recommend.getPublisher());
        book.setDays(30);
        book.setPrice(recommend.getPrice());
        book.setStock(1);
        book.setStatus("正常");
        book.setIntroduction("由读者推荐加入");

        Connection conn = null;
        try {
            conn = JDBCTool.getConnection();
            conn.setAutoCommit(false);
            bookDao.insert(conn, book);
            BookCopy copy = new BookCopy();
            copy.setId(UUID.randomUUID().toString());
            copy.setCreateTime(now);
            copy.setUpdateTime(now);
            Book ref = new Book();
            ref.setId(book.getId());
            copy.setBook(ref);
            copy.setBorrowed(false);
            bookCopyDao.insert(conn, copy);
            recommendDao.updateStatus(conn, id, "已通过", null);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("加入图书列表失败: " + e.getMessage(), e);
        } finally {
            JDBCTool.close(conn);
        }
    }

    public void reject(String id, String reason) {
        BookRecommend recommend = recommendDao.findById(id);
        if (recommend == null) {
            throw new RuntimeException("推荐记录不存在");
        }
        if (!"待审核".equals(recommend.getStatus())) {
            throw new RuntimeException("该推荐已处理");
        }
        recommendDao.updateStatus(id, "已驳回", reason);
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
