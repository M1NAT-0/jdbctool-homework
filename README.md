# JDBCTool 作业

基于 Java 反射 + JDBC 的简易 ORM 工具，实现对 `datetest` 数据库中学生表 `student` 与学院表 `college` 的增删改查。

## 技术栈

- Java 21
- Maven
- MySQL 8.0
- mysql-connector-j 8.0.33

## 目录结构

```
src/main/java/com/zzj/jdbctool
├── annotation/        # @Table / @Column / @Id 注解
├── entity/            # Student / College 实体类
├── util/              # JDBCTool（五个核心方法）与 DbUtil（连接/建表辅助）
└── Main.java          # 演示入口
src/main/resources/
├── db.properties      # 数据库连接配置
└── schema.sql         # 建表脚本
```

## 运行

1. 先建库：`CREATE DATABASE IF NOT EXISTS datetest DEFAULT CHARACTER SET utf8mb4;`
2. 修改 `src/main/resources/db.properties` 中的用户名/密码。
3. 执行：

   ```bash
   mvn -q clean compile exec:java
   ```

## JDBCTool 五个方法

- `static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz)`
- `static <T> int save(T obj, Connection connection)`
- `static <T> int update(T obj, Connection connection)`
- `static <T> int delete(T obj, Connection connection)`
- `static <T> T getOneById(String id, Class<T> clazz, Connection connection)`

> 数据库连接信息中的密码为本地开发环境配置，请按需修改。
