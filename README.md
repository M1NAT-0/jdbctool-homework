# JDBCTool 作业

基于 Java 反射 + JDBC 的简易 ORM 工具，实现对 `datetest` 数据库中学生表 `student` 与学院表 `college` 的增删改查。

## 技术栈

- Java 21
- Maven
- MySQL 8.0
- mysql-connector-j 8.0.33
- 开发环境：VS Code

## 目录结构

```
src/main/java/com/zzj/jdbctool
├── annotation/        # @Table / @Column / @Id 注解
├── entity/            # Student / College 实体类
├── util/              # JDBCTool（五个核心方法）与 DbUtil（连接/建表辅助）
└── Main.java          # 交互式演示入口
src/main/resources/
├── db.properties      # 数据库连接配置
└── schema.sql         # 建表脚本
docs/
├── 运行效果.png        # 运行效果截图
└── run-output.txt     # 控制台输出文本
```

## 运行

1. 先建库：

   ```sql
   CREATE DATABASE IF NOT EXISTS datetest DEFAULT CHARACTER SET utf8mb4;
   ```

2. 修改 `src/main/resources/db.properties` 中的用户名/密码。
3. 在 VS Code 终端执行：

   ```bash
   mvn -q clean compile exec:java
   ```

> 程序启动后进入交互式菜单，所有数据都通过键盘手动输入，不写死：
> 先选择数据表（学生表 / 学院表），再选择增删改查操作，按提示输入对应字段即可。

## 交互式菜单说明

- 学生表 `student`：`save` 新增、`resultSetToList` 查询全部、`getOneById` 按 id 查询、`update` 修改、`delete` 删除。
- 学院表 `college`：同样提供新增、查询全部、按 id 查询、修改、删除。
- 输入 `0` 返回上一级或退出程序。

## JDBCTool 五个方法

- `static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz)`
- `static <T> int save(T obj, Connection connection)`
- `static <T> int update(T obj, Connection connection)`
- `static <T> int delete(T obj, Connection connection)`
- `static <T> T getOneById(String id, Class<T> clazz, Connection connection)`

> 数据库连接信息中的密码为本地开发环境配置，请按需修改。