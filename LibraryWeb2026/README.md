# LibraryWeb2026 图书馆管理系统

一个基于 **Servlet + JDBC** 的 Java Web 项目，前端使用 **HTML + CSS + jQuery + Axios + Vue**（不使用 JSP，不使用前后端分离，所有页面由同一个 WAR 提供）。

## 技术栈

- 后端：Jakarta Servlet（Tomcat 10.1+）+ JDBC
- 数据库：MySQL 8.x（数据库名 `libraryadmin`）
- 前端：HTML、CSS、jQuery、Axios、Vue 3（本地静态资源）
- 构建：Maven（war 打包）

## 目录结构

```
LibraryWeb2026
├── pom.xml
├── database/libraryadmin.sql   # 由上传的 LibraryDB.nb3 备份生成的还原脚本（可选）
└── src/main
    ├── java/com/library
    │   ├── entity/    # Base, Book, BookCopy, User, Admin, Operator, Student, Teacher, Loan, BookRecommend
    │   ├── util/      # JDBCTool（JDBC 工具类）
    │   ├── common/    # Result 统一返回
    │   ├── dao/       # 数据访问层
    │   ├── service/   # 业务层（含事务）
    │   └── servlet/   # 请求响应层
    ├── resources/db.properties  # 数据库连接配置
    └── webapp/        # HTML/CSS/JS/WEB-INF
```

## 数据库还原

### 方式一：Navicat 还原（推荐，使用老师提供的备份）

1. 解压老师上传的压缩包，得到 `LibraryDB.nb3`（Navicat 备份文件）。
2. 在 Navicat 中新建 MySQL 连接，右键连接 ->「运行 SQL 文件」或使用「备份还原」功能还原 `LibraryDB.nb3`。
3. 还原后确认数据库名为 `libraryadmin`，包含 `book、bookcopy、bookrecommend、knowledge、loan、log、student、user` 等表。

### 方式二：使用项目内已生成的 SQL 脚本

```bash
mysql -uroot -p < database/libraryadmin.sql
```

## 数据库连接配置

编辑 `src/main/resources/db.properties`，改成你自己电脑上的 MySQL 账号密码：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/libraryadmin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的密码
```

## JDK 与 Tomcat 环境变量

1. 安装 JDK 11+，设置：
   - `JAVA_HOME = F:\JavaJdk`（按实际路径修改）
   - `Path` 追加 `%JAVA_HOME%\bin`
2. 下载 Apache Tomcat 10.1（本项目使用 `jakarta.servlet`，不能用 Tomcat 9 的 `javax.servlet`），解压后设置：
   - `CATALINA_HOME = F:\apache-tomcat-10.1.x`
   - `Path` 追加 `%CATALINA_HOME%\bin`

## 构建与运行

```bash
cd LibraryWeb2026
mvn clean package
```

打包产物：`target/LibraryWeb2026.war`。

把 `LibraryWeb2026.war` 复制到 Tomcat 的 `webapps` 目录，启动 Tomcat：

```bash
%CATALINA_HOME%\bin\startup.bat
```

浏览器访问：`http://localhost:8080/LibraryWeb2026/`

## 内置账号（来自备份数据）

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 操作员 | operator | operator123 |
| 学生 | student1 | student123 |
| 教师 | teacher | teacher123 |

## 功能清单

- 用户注册 / 登录（验证码、重置、同名与手机号去重、按角色填写资料）
- 用户信息查询（管理员，模糊查询，创建时间倒序，弹窗查看详情）
- 图书：添加、查询、废弃、编辑、副本新增/删除
- 借书 / 还书 / 个人借阅查询
- 借阅记录查询（关键字 / 超期 / 日期范围）
- 借阅数据统计（按图书类型 / 按用户专业）
- 推荐图书（教师）与审阅推荐（管理员，加入图书列表 / 驳回并写理由）

## 提交到 Gitee

```bash
git init
git add .
git commit -m "LibraryWeb2026 图书馆管理系统"
git remote add origin https://gitee.com/你的用户名/LibraryWeb2026.git
git push -u origin master
```

> 注意：请先在 Gitee 网页上新建空仓库，再把上面的远程地址替换为你自己的仓库地址。
