# WirChatten · 在线聊天系统 / Online Web Chat / Online-Web-Chat

[中文](#中文文档) · [English](#english-documentation) · [Deutsch](#deutsche-dokumentation)

---

<a id="中文文档"></a>
## 中文文档

### 一、项目简介

**WirChatten** 是一个基于 Java 的网页在线聊天系统。后端使用 Spring Boot + MyBatis + MySQL，通过 WebSocket 实现消息实时收发；前端为原生 HTML/CSS/JavaScript（jQuery），包含登录、注册、聊天主界面三个页面。系统采用 JWT 做无状态鉴权，并内置了好友申请、未读红点、防顶号等实用功能。

> 本项目是一个用于学习与演示的 Demo，运行前需要自行准备并配置数据库环境。

### 二、功能特性

- **账号**：注册（需填写「注册通行证」）、登录、获取当前用户信息。
- **鉴权**：登录签发 JWT，前端存储后通过 `Authorization: Bearer` 携带；WebSocket 通过 URL 参数携带。
- **好友**：搜索用户、发送好友申请、审核（通过 / 拒绝）、删除好友（双向解除）。
- **聊天**：基于 WebSocket 的实时私聊、历史消息拉取、消息时间显示。
- **会话**：会话列表、最近一条消息预览（含时间）、未读消息红点计数。
- **体验**：根据用户名生成的首字彩色头像、移动端响应式布局。
- **安全**：防顶号 —— 同一账号在线（WebSocket 已连接）时禁止再次登录。

### 三、技术栈

| 层次 | 技术 | 版本 |
| --- | --- | --- |
| 语言 | Java | 17 |
| 框架 | Spring Boot（webmvc、websocket） | 4.0.7 |
| 持久层 | MyBatis（mybatis-spring-boot-starter） | 4.0.1 |
| 数据库 | MySQL（mysql-connector-j） | 9.7.0 |
| 鉴权 | JJWT | 0.11.5 |
| 工具 | Lombok | 随 Spring Boot 管理 |
| 前端 | 原生 HTML/CSS/JS + jQuery（本地文件） | jQuery 3.7.1 |
| 构建 | Maven | — |

### 四、目录结构

```
online-web-chat/                         # Git 仓库根目录
├── README.md
└── online-web-chat/                     # Spring Boot 项目
    ├── pom.xml
    └── src
        ├── main
        │   ├── database.sql             # 建库建表脚本
        │   ├── java/site/curiez/onlinewebchat
        │   │   ├── OnlineWebChatApplication.java
        │   │   ├── API                    # 控制器：UserAPI / FriendAPI / MessageSessionAPI / MessageAPI
        │   │   ├── component              # WebSocketHandler、OnlineUserManager
        │   │   ├── config                 # WebSocketConfig、JwtHandshakeInterceptor、ResponseAdvice、ExceptionHandle
        │   │   ├── enums                  # ResultStatus
        │   │   ├── mapper                 # MyBatis Mapper 接口
        │   │   ├── model                  # 实体类
        │   │   └── utils                  # JwtUtils
        │   └── resources
        │       ├── application.yml        # 仅配置 spring.profiles.active = @profile.name@
        │       ├── application-dev.yml    # 本地数据源（需自行创建，若缺失）
        │       ├── application-prod.yml   # 服务器数据源（未提交到 git，需自行创建）
        │       ├── mapper/*.xml           # MyBatis SQL 映射
        │       └── static                 # 前端页面与静态资源
        │           ├── login.html / register.html / client.html
        │           ├── css/  js/  img/
        └── test
```

### 五、数据库设计

建库脚本：`online-web-chat/src/main/database.sql`。共 6 张表：

| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `user` | `user_id`(PK), `user_name`(唯一), `password` | 用户 |
| `friend` | `user_id`, `friend_id` | 好友关系，双向各存一行 |
| `friend_request` | `request_id`(PK), `from_id`, `to_id`, `request_time` | 好友申请（处理完删除，不留冗余） |
| `message_session` | `session_id`(PK), `last_time` | 会话（一对一会话） |
| `message_session_user` | `session_id`, `user_id` | 会话成员，`session_id` 与 `user_id` 为**一对多** |
| `message` | `message_id`(PK), `from_id`, `session_id`, `content`, `post_time` | 消息 |

关系：一个会话可包含多个用户（`message_session_user`），消息按 `session_id` 归属。

**导入方法**：

```bash
mysql -u root -p < online-web-chat/src/main/database.sql
```

或在客户端中直接执行该 SQL 文件。

### 六、快速开始

**环境要求**：JDK 17、Maven、MySQL 8+。

1. 建库并导入数据：见上一节。
2. 配置数据源：编辑 `src/main/resources/application-dev.yml`（本地）或创建 `application-prod.yml`（服务器），详见「配置与 Profile」。
3. 运行（本地开发）：

```bash
cd online-web-chat
mvn -Pdev spring-boot:run
```

4. 浏览器访问：`http://localhost:8080/login.html`
5. 打包部署（服务器）：

```bash
mvn -Pprod clean package
java -jar target/online-web-chat-0.0.1-SNAPSHOT.jar
```

### 七、配置与 Profile 说明

- `application.yml` 中 `spring.profiles.active: @profile.name@`，该占位符在**构建时**由 Maven 的 profile 替换。
- 因此选择哪个环境，取决于**构建时激活的 Maven profile**：
  - 本地开发用 `dev`：`mvn -Pdev ...`
  - 服务器部署用 `prod`：`mvn -Pprod ...`

> **⚠️ 提醒 1：`application-prod.yml` 未提交到 git，需要你自行创建**
>
> 在 `online-web-chat/src/main/resources/` 下新建 `application-prod.yml`，内容参考（请替换为你的真实地址与账号密码，**不要提交真实密码**）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/online_web_chat?characterEncoding=utf8&useSSL=false
    username: root
    password: 你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/**Mapper.xml
```

> **⚠️ 提醒 2：注意 IntelliJ 的 Maven Profiles 勾选**
>
> 在 IntelliJ 右侧 **Maven 面板 → Profiles** 中：
> - 本地运行：**勾选 `dev`**，取消 `prod`；
> - 服务器运行：**勾选 `prod`**，取消 `dev`。
>
> 勾选正确后重新构建（`Reload` / `Reimport`），`target/classes/application.yml` 才会指向对应环境；否则会出现「本地连了生产库」或「服务器连了本地库」的连不上问题。
>
> 命令行等价写法：`mvn -Pdev ...` 或 `mvn -Pprod ...`。

### 八、注册通行证

注册页面除用户名、密码、确认密码外，还需要填写「**注册通行证**」才能注册，用于限制注册入口。

- 当前通行证值为：**`curiez`**
- 修改位置：`online-web-chat/src/main/java/site/curiez/onlinewebchat/API/UserAPI.java`

```java
private final static String registerKey = "curiez";
```

将字符串改成你想要的值即可。

### 九、接口文档

**统一响应体**：所有接口返回值会被 `ResponseAdvice` 统一包装为 `Result`：

```json
{ "code": "SUCCESS | FAIL", "errorMessage": "", "data": { } }
```

- `code`：`SUCCESS` 成功、`FAIL` 失败；
- `errorMessage`：失败信息；
- `data`：真正的业务数据。

**REST 接口**（除登录/注册外均需在请求头携带 `Authorization: Bearer <token>`）：

| 方法 | 路径 | 参数 | 返回（`data` 内） | 说明 |
| --- | --- | --- | --- | --- |
| POST | `/login` | `userName`, `password` | JWT 字符串 | 登录，签发 token |
| POST | `/register` | `userName`, `password`, `key` | User | 注册（`key` 为注册通行证） |
| GET | `/userInfo` | — | User | 当前用户信息 |
| GET | `/friendList` | — | `Friend[]` | 好友列表 |
| GET | `/searchUser` | `userName` | `User[]` | 搜索用户 |
| POST | `/addFriend` | `toUserId` | `{code,msg}` | 发送好友申请 |
| GET | `/friendRequestList` | — | `FriendRequest[]` | 待我审核的申请 |
| POST | `/handleFriendRequest` | `requestId`, `accept` | `{code,msg}` | 通过/拒绝好友申请 |
| POST | `/deleteFriend` | `sessionId` | `{code,msg}` | 删除好友（双向）并清理会话 |
| GET | `/sessionList` | — | `MessageSession[]` | 会话列表（含最后消息与时间） |
| POST | `/session` | `toUserId` | `{sessionId}` | 获取/创建与某用户的会话（幂等） |
| GET | `/message` | `sessionId` | `Message[]` | 会话历史消息 |

### 十、WebSocket 说明

- 连接地址：`ws://<host>/WebSocketMessage?token=<JWT>`（token 通过握手拦截器校验）。
- 客户端 → 服务端发送消息：

```json
{ "type": "message", "sessionId": 2, "content": "你好" }
```

- 服务端 → 客户端（实时消息，广播给会话内其他人）：

```json
{
  "type": "message",
  "fromId": 1,
  "fromName": "zhangsan",
  "sessionId": 2,
  "content": "你好",
  "postTime": "2026-10-11 15:00"
}
```

- 服务端 → 客户端（事件通知）：好友申请被通过时推送

```json
{ "type": "friendAccepted" }
```

前端收到后会刷新好友列表。

### 十一、鉴权说明

1. 登录成功后，服务端用 `JwtUtils` 签发 JWT，随 `Result.data` 返回。
2. 前端将 token 存入 `localStorage`，之后所有 AJAX 通过 `$.ajaxSetup` 自动携带 `Authorization: Bearer <token>`；WebSocket 通过 `?token=` 携带。
3. 服务端 `JwtUtils.getUser(request)` 校验并解析出用户，替代了传统的 Session。
4. **防顶号**：登录时若该账号已有在线 WebSocket 连接（`OnlineUserManager.isOnline`），直接返回登录失败「该账号已在其他地方登录」。

### 十二、前端说明

| 页面 | 说明 |
| --- | --- |
| `login.html` | 登录页 |
| `register.html` | 注册页（含注册通行证输入） |
| `client.html` | 聊天主界面 |

- 三个页面的 CSS/JS 均**内联**在 HTML 中（仅 `js/jquery.min.js` 为本地独立文件），以减少对静态资源目录的依赖。
- 主界面功能：会话/好友列表切换、搜索并添加好友、新好友审核、删除好友、未读红点、首字头像、移动端「列表 ↔ 聊天」自适应。

### 十三、部署

1. 在服务器上准备好 MySQL，导入 `database.sql`。
2. 创建 `application-prod.yml` 并填入服务器数据库信息（见「配置与 Profile」）。
3. 构建（激活 prod）：`mvn -Pprod clean package`。
4. 上传 `target/online-web-chat-0.0.1-SNAPSHOT.jar` 到服务器并运行：

```bash
nohup java -jar online-web-chat-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

5. 若使用 Nginx 反向代理，建议将**全部请求**都转发到该 jar（含静态资源），避免只代理 HTML 导致样式丢失：

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
# WebSocket 需要 Upgrade 头
location /WebSocketMessage {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 十四、常见问题（FAQ）

- **登录/接口报「数据库连接失败」**
  - 先确认 profile 选对：本地应为 `dev`、服务器应为 `prod`（见「配置与 Profile」的提醒 2）。
  - 确认数据库已启动、库名、账号、密码与所选 profile 的 yml 一致。
- **报 `Public Key Retrieval is not allowed`（公钥报错）**
  - MySQL 8 默认使用 `caching_sha2_password`，JDBC URL 需要 `allowPublicKeyRetrieval=true`（配合 `useSSL=false`）。按需在数据源 URL 追加该参数。
- **本地能跑、部署后样式全丢**
  - 多为 Nginx 只代理了 HTML。将全部请求（含 `/css`、`/js`、`/img`）反代到 jar 即可。
- **防顶号导致无法登录**
  - 若上一次连接的 WebSocket 异常关闭未被及时回收，可稍等或重启服务后再登录。

### 十五、作者与说明

- 项目作者：curiez
- 本项目仅供学习与演示使用。

---

<a id="english-documentation"></a>
## English Documentation

### 1. Overview

**WirChatten** is a Java-based web chat system. The backend is built with Spring Boot + MyBatis + MySQL and delivers real-time messages over WebSocket. The frontend uses plain HTML/CSS/JavaScript (jQuery) with three pages: login, register, and the chat client. Authentication is stateless and based on JWT, and the app includes friend requests, unread badges, and duplicate-login protection.

> This is a learning/demo project. You must prepare and configure your own database before running it.

### 2. Features

- **Accounts**: register (requires a "register key"), login, fetch current user.
- **Auth**: JWT issued on login; sent via `Authorization: Bearer`; carried in the WebSocket URL.
- **Friends**: search users, send/accept/reject friend requests, delete friends (bidirectional).
- **Chat**: real-time private chat over WebSocket, message history, message timestamps.
- **Sessions**: session list, last-message preview (with time), unread red-dot counter.
- **UX**: colored initial-based avatars, responsive mobile layout.
- **Security**: duplicate-login protection — an account already online cannot log in again.

### 3. Tech Stack

| Layer | Technology | Version |
| --- | --- | --- |
| Language | Java | 17 |
| Framework | Spring Boot (webmvc, websocket) | 4.0.7 |
| Persistence | MyBatis (mybatis-spring-boot-starter) | 4.0.1 |
| Database | MySQL (mysql-connector-j) | 9.7.0 |
| Auth | JJWT | 0.11.5 |
| Tooling | Lombok | managed |
| Frontend | HTML/CSS/JS + jQuery (local file) | jQuery 3.7.1 |
| Build | Maven | — |

### 4. Project Structure

```
online-web-chat/                         # git repo root
├── README.md
└── online-web-chat/                     # Spring Boot project
    ├── pom.xml
    └── src/main
        ├── database.sql                 # schema + seed script
        ├── java/site/curiez/onlinewebchat
        │   ├── API                      # controllers
        │   ├── component                # WebSocketHandler, OnlineUserManager
        │   ├── config                   # WebSocketConfig, JwtHandshakeInterceptor, ResponseAdvice, ExceptionHandle
        │   ├── enums / mapper / model / utils
        └── resources
            ├── application.yml          # spring.profiles.active = @profile.name@
            ├── application-dev.yml      # local datasource
            ├── application-prod.yml     # server datasource (not committed — create it yourself)
            ├── mapper/*.xml
            └── static                   # frontend pages and assets
```

### 5. Database

Script: `online-web-chat/src/main/database.sql`. Six tables:

| Table | Key columns | Description |
| --- | --- | --- |
| `user` | `user_id`(PK), `user_name`(unique), `password` | users |
| `friend` | `user_id`, `friend_id` | friendships (two rows, one per direction) |
| `friend_request` | `request_id`(PK), `from_id`, `to_id`, `request_time` | pending requests (deleted after handling) |
| `message_session` | `session_id`(PK), `last_time` | conversation |
| `message_session_user` | `session_id`, `user_id` | conversation members (one-to-many) |
| `message` | `message_id`(PK), `from_id`, `session_id`, `content`, `post_time` | messages |

Import it:

```bash
mysql -u root -p < online-web-chat/src/main/database.sql
```

### 6. Quick Start

**Requirements**: JDK 17, Maven, MySQL 8+.

1. Create the database and import the script (see above).
2. Configure the datasource in `application-dev.yml` (local) or create `application-prod.yml` (server).
3. Run locally:

```bash
cd online-web-chat
mvn -Pdev spring-boot:run
```

4. Open `http://localhost:8080/login.html`.
5. Build for deployment:

```bash
mvn -Pprod clean package
java -jar target/online-web-chat-0.0.1-SNAPSHOT.jar
```

### 7. Configuration & Profiles

- `application.yml` uses `spring.profiles.active: @profile.name@`, resolved by Maven **at build time**.
- Pick the environment by activating the Maven profile:
  - `dev` for local: `mvn -Pdev ...`
  - `prod` for server: `mvn -Pprod ...`

> **⚠️ Reminder 1: `application-prod.yml` is NOT committed — create it yourself.**
>
> Create `application-prod.yml` under `online-web-chat/src/main/resources/` (replace with your real values, **never commit real passwords**):

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/online_web_chat?characterEncoding=utf8&useSSL=false
    username: root
    password: your-db-password
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/**Mapper.xml
```

> **⚠️ Reminder 2: Watch the IntelliJ Maven Profiles checkbox.**
>
> In IntelliJ's **Maven panel → Profiles**:
> - Local: check `dev`, uncheck `prod`.
> - Server: check `prod`, uncheck `dev`.
>
> Reload/reimport after switching so `target/classes/application.yml` points to the right environment. CLI equivalent: `mvn -Pdev ...` / `mvn -Pprod ...`.

### 8. Register Key

Besides username/password/confirm password, registration requires a **register key**.

- Current value: **`curiez`**
- Location: `online-web-chat/src/main/java/site/curiez/onlinewebchat/API/UserAPI.java`

```java
private final static String registerKey = "curiez";
```

Change the string to whatever you want.

### 9. API

**Unified response**: every response is wrapped by `ResponseAdvice` into `Result`:

```json
{ "code": "SUCCESS | FAIL", "errorMessage": "", "data": { } }
```

All endpoints except login/register require the header `Authorization: Bearer <token>`.

| Method | Path | Params | `data` | Notes |
| --- | --- | --- | --- | --- |
| POST | `/login` | `userName`, `password` | JWT string | issue token |
| POST | `/register` | `userName`, `password`, `key` | User | register (`key` = register key) |
| GET | `/userInfo` | — | User | current user |
| GET | `/friendList` | — | `Friend[]` | friend list |
| GET | `/searchUser` | `userName` | `User[]` | search users |
| POST | `/addFriend` | `toUserId` | `{code,msg}` | send friend request |
| GET | `/friendRequestList` | — | `FriendRequest[]` | incoming requests |
| POST | `/handleFriendRequest` | `requestId`, `accept` | `{code,msg}` | accept/reject |
| POST | `/deleteFriend` | `sessionId` | `{code,msg}` | delete friend (bidirectional) |
| GET | `/sessionList` | — | `MessageSession[]` | session list |
| POST | `/session` | `toUserId` | `{sessionId}` | get/create session (idempotent) |
| GET | `/message` | `sessionId` | `Message[]` | message history |

### 10. WebSocket

- URL: `ws://<host>/WebSocketMessage?token=<JWT>`.
- Client → server:

```json
{ "type": "message", "sessionId": 2, "content": "hello" }
```

- Server → client (broadcast to others in the session):

```json
{ "type": "message", "fromId": 1, "fromName": "zhangsan", "sessionId": 2, "content": "hello", "postTime": "2026-10-11 15:00" }
```

- Server → client event (friend request accepted):

```json
{ "type": "friendAccepted" }
```

### 11. Authentication

1. On login the server issues a JWT (via `JwtUtils`) returned in `Result.data`.
2. The frontend stores it in `localStorage`; AJAX adds `Authorization: Bearer <token>` automatically (`$.ajaxSetup`); WebSocket passes it as `?token=`.
3. The server resolves the user from the token (`JwtUtils.getUser(request)`), replacing the traditional Session.
4. **Duplicate-login protection**: if the account already has an online WebSocket (`OnlineUserManager.isOnline`), login is rejected.

### 12. Frontend

| Page | Description |
| --- | --- |
| `login.html` | login |
| `register.html` | register (with register key) |
| `client.html` | main chat UI |

CSS/JS are **inlined** into each HTML (only `js/jquery.min.js` is a local separate file). The client supports session/friend tabs, search + add friend, request review, delete friend, unread badges, initial avatars, and a responsive mobile layout.

### 13. Deployment

1. Prepare MySQL on the server and import `database.sql`.
2. Create `application-prod.yml` with the server's database settings.
3. Build: `mvn -Pprod clean package`.
4. Upload and run:

```bash
nohup java -jar online-web-chat-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

5. If using Nginx, proxy **all** requests (including static assets) to the jar, and enable WebSocket upgrade:

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
location /WebSocketMessage {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 14. FAQ

- **"Database connection failed"** — verify the active profile (`dev` locally, `prod` on server) and that URL/credentials match the running MySQL.
- **`Public Key Retrieval is not allowed`** — MySQL 8 uses `caching_sha2_password`; append `allowPublicKeyRetrieval=true` (with `useSSL=false`) to the JDBC URL.
- **Works locally but styles are lost after deployment** — the reverse proxy likely proxies only HTML; proxy all paths to the jar.
- **Cannot log in due to duplicate-login protection** — wait for the previous socket to be reclaimed, or restart the service.

### 15. Author & Notes

- Author: curiez
- For learning and demonstration purposes only.

---

<a id="deutsche-dokumentation"></a>
## Deutsche Dokumentation

### 1. Überblick

**WirChatten** ist ein Java-basiertes Web-Chat-System. Das Backend basiert auf Spring Boot + MyBatis + MySQL und liefert Nachrichten in Echtzeit über WebSocket. Das Frontend besteht aus reinem HTML/CSS/JavaScript (jQuery) mit drei Seiten: Login, Registrierung und Chat-Client. Die Authentifizierung ist zustandslos und JWT-basiert; enthalten sind Freundschaftsanfragen, Ungelesen-Zähler und Schutz vor doppeltem Login.

> Dies ist ein Lern-/Demo-Projekt. Vor dem Start muss eine eigene Datenbank eingerichtet und konfiguriert werden.

### 2. Funktionen

- **Konten**: Registrierung (mit „Registrierungsschlüssel“), Login, aktueller Benutzer.
- **Authentifizierung**: JWT beim Login; Übertragung per `Authorization: Bearer`; im WebSocket als URL-Parameter.
- **Freunde**: Benutzer suchen, Anfragen senden/annehmen/ablehnen, Freunde löschen (beidseitig).
- **Chat**: Echtzeit-Private-Chat über WebSocket, Nachrichtenverlauf, Zeitstempel.
- **Sitzungen**: Sitzungsliste, Vorschau der letzten Nachricht (mit Zeit), Ungelesen-Zähler.
- **UX**: farbige Avatare aus dem Anfangsbuchstaben, responsives Mobile-Layout.
- **Sicherheit**: Schutz vor doppeltem Login — ein bereits online befindliches Konto kann sich nicht erneut anmelden.

### 3. Technologie-Stack

| Schicht | Technologie | Version |
| --- | --- | --- |
| Sprache | Java | 17 |
| Framework | Spring Boot (webmvc, websocket) | 4.0.7 |
| Persistenz | MyBatis (mybatis-spring-boot-starter) | 4.0.1 |
| Datenbank | MySQL (mysql-connector-j) | 9.7.0 |
| Auth | JJWT | 0.11.5 |
| Tooling | Lombok | verwaltet |
| Frontend | HTML/CSS/JS + jQuery (lokale Datei) | jQuery 3.7.1 |
| Build | Maven | — |

### 4. Projektstruktur

```
online-web-chat/                         # git-Repository-Wurzel
├── README.md
└── online-web-chat/                     # Spring-Boot-Projekt
    ├── pom.xml
    └── src/main
        ├── database.sql                 # Schema- und Seed-Skript
        ├── java/site/curiez/onlinewebchat
        │   ├── API                      # Controller
        │   ├── component                # WebSocketHandler, OnlineUserManager
        │   ├── config                   # WebSocketConfig, JwtHandshakeInterceptor, ResponseAdvice, ExceptionHandle
        │   ├── enums / mapper / model / utils
        └── resources
            ├── application.yml          # spring.profiles.active = @profile.name@
            ├── application-dev.yml      # lokale Datenquelle
            ├── application-prod.yml     # Server-Datenquelle (nicht im git — selbst anlegen)
            ├── mapper/*.xml
            └── static                   # Frontend und Assets
```

### 5. Datenbank

Skript: `online-web-chat/src/main/database.sql`. Sechs Tabellen:

| Tabelle | Wichtige Spalten | Beschreibung |
| --- | --- | --- |
| `user` | `user_id`(PK), `user_name`(unique), `password` | Benutzer |
| `friend` | `user_id`, `friend_id` | Freundschaften (zwei Zeilen, beide Richtungen) |
| `friend_request` | `request_id`(PK), `from_id`, `to_id`, `request_time` | offene Anfragen (nach Bearbeitung gelöscht) |
| `message_session` | `session_id`(PK), `last_time` | Konversation |
| `message_session_user` | `session_id`, `user_id` | Mitglieder (1:n) |
| `message` | `message_id`(PK), `from_id`, `session_id`, `content`, `post_time` | Nachrichten |

Import:

```bash
mysql -u root -p < online-web-chat/src/main/database.sql
```

### 6. Schnellstart

**Voraussetzungen**: JDK 17, Maven, MySQL 8+.

1. Datenbank anlegen und Skript importieren (siehe oben).
2. Datenquelle in `application-dev.yml` (lokal) konfigurieren bzw. `application-prod.yml` (Server) anlegen.
3. Lokal starten:

```bash
cd online-web-chat
mvn -Pdev spring-boot:run
```

4. `http://localhost:8080/login.html` öffnen.
5. Für die Bereitstellung bauen:

```bash
mvn -Pprod clean package
java -jar target/online-web-chat-0.0.1-SNAPSHOT.jar
```

### 7. Konfiguration & Profile

- `application.yml` nutzt `spring.profiles.active: @profile.name@`, das von Maven **zur Build-Zeit** ersetzt wird.
- Umgebung über das aktive Maven-Profil wählen:
  - `dev` für lokal: `mvn -Pdev ...`
  - `prod` für den Server: `mvn -Pprod ...`

> **⚠️ Hinweis 1: `application-prod.yml` ist NICHT im git — selbst anlegen.**
>
> Datei `application-prod.yml` unter `online-web-chat/src/main/resources/` erstellen (echte Werte eintragen, **niemals echte Passwörter committen**):

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/online_web_chat?characterEncoding=utf8&useSSL=false
    username: root
    password: dein-db-passwort
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/**Mapper.xml
```

> **⚠️ Hinweis 2: Auf die IntelliJ-Maven-Profiles achten.**
>
> Im IntelliJ **Maven-Panel → Profiles**:
> - Lokal: `dev` aktivieren, `prod` deaktivieren.
> - Server: `prod` aktivieren, `dev` deaktivieren.
>
> Danach neu laden, damit `target/classes/application.yml` auf die richtige Umgebung zeigt. CLI-Äquivalent: `mvn -Pdev ...` / `mvn -Pprod ...`.

### 8. Registrierungsschlüssel

Zusätzlich zu Benutzername/Passwort ist bei der Registrierung ein **Registrierungsschlüssel** erforderlich.

- Aktueller Wert: **`curiez`**
- Ort: `online-web-chat/src/main/java/site/curiez/onlinewebchat/API/UserAPI.java`

```java
private final static String registerKey = "curiez";
```

Einfach den String ändern.

### 9. API

**Einheitliche Antwort**: Jede Antwort wird von `ResponseAdvice` in `Result` verpackt:

```json
{ "code": "SUCCESS | FAIL", "errorMessage": "", "data": { } }
```

Alle Endpunkte außer Login/Registrierung benötigen den Header `Authorization: Bearer <token>`.

| Methode | Pfad | Parameter | `data` | Hinweis |
| --- | --- | --- | --- | --- |
| POST | `/login` | `userName`, `password` | JWT-String | Token ausstellen |
| POST | `/register` | `userName`, `password`, `key` | User | Registrierung (`key` = Schlüssel) |
| GET | `/userInfo` | — | User | aktueller Benutzer |
| GET | `/friendList` | — | `Friend[]` | Freundesliste |
| GET | `/searchUser` | `userName` | `User[]` | Benutzer suchen |
| POST | `/addFriend` | `toUserId` | `{code,msg}` | Anfrage senden |
| GET | `/friendRequestList` | — | `FriendRequest[]` | eingehende Anfragen |
| POST | `/handleFriendRequest` | `requestId`, `accept` | `{code,msg}` | annehmen/ablehnen |
| POST | `/deleteFriend` | `sessionId` | `{code,msg}` | Freund löschen (beidseitig) |
| GET | `/sessionList` | — | `MessageSession[]` | Sitzungsliste |
| POST | `/session` | `toUserId` | `{sessionId}` | Sitzung holen/erstellen (idempotent) |
| GET | `/message` | `sessionId` | `Message[]` | Nachrichtenverlauf |

### 10. WebSocket

- URL: `ws://<host>/WebSocketMessage?token=<JWT>`.
- Client → Server:

```json
{ "type": "message", "sessionId": 2, "content": "hallo" }
```

- Server → Client (Broadcast an andere in der Sitzung):

```json
{ "type": "message", "fromId": 1, "fromName": "zhangsan", "sessionId": 2, "content": "hallo", "postTime": "2026-10-11 15:00" }
```

- Server → Client Ereignis (Anfrage angenommen):

```json
{ "type": "friendAccepted" }
```

### 11. Authentifizierung

1. Beim Login stellt der Server ein JWT aus (`JwtUtils`) und gibt es in `Result.data` zurück.
2. Das Frontend speichert es in `localStorage`; AJAX sendet automatisch `Authorization: Bearer <token>` (`$.ajaxSetup`); WebSocket übergibt es als `?token=`.
3. Der Server ermittelt den Benutzer aus dem Token (`JwtUtils.getUser(request)`) — dies ersetzt die klassische Session.
4. **Schutz vor doppeltem Login**: Ist das Konto bereits mit einem WebSocket online (`OnlineUserManager.isOnline`), wird der Login abgelehnt.

### 12. Frontend

| Seite | Beschreibung |
| --- | --- |
| `login.html` | Login |
| `register.html` | Registrierung (mit Schlüssel) |
| `client.html` | Chat-Oberfläche |

CSS/JS sind **inline** in jeder HTML (nur `js/jquery.min.js` ist eine lokale Datei). Der Client bietet Sitzungs-/Freundes-Tabs, Suche + Freund hinzufügen, Anfragen prüfen, Freund löschen, Ungelesen-Zähler, Avatare und ein responsives Mobile-Layout.

### 13. Bereitstellung

1. MySQL auf dem Server vorbereiten und `database.sql` importieren.
2. `application-prod.yml` mit den Server-Zugangsdaten anlegen.
3. Bauen: `mvn -Pprod clean package`.
4. Hochladen und starten:

```bash
nohup java -jar online-web-chat-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

5. Bei Nginx **alle** Anfragen (inkl. statischer Assets) an das jar weiterleiten und WebSocket-Upgrade aktivieren:

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
location /WebSocketMessage {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 14. FAQ

- **„Datenbankverbindung fehlgeschlagen"** — prüfen, ob das aktive Profil (`dev` lokal, `prod` am Server) und URL/Zugangsdaten zur laufenden MySQL passen.
- **`Public Key Retrieval is not allowed`** — MySQL 8 nutzt `caching_sha2_password`; in der JDBC-URL `allowPublicKeyRetrieval=true` (mit `useSSL=false`) ergänzen.
- **Lokal ok, aber Styles fehlen nach dem Deployment** — der Reverse-Proxy leitet vermutlich nur HTML weiter; alle Pfade an das jar weiterleiten.
- **Login wegen Doppel-Login-Schutz nicht möglich** — warten, bis der alte Socket freigegeben wird, oder den Dienst neu starten.

### 15. Autor & Hinweise

- Autor: curiez
- Nur für Lern- und Demonstrationszwecke.
