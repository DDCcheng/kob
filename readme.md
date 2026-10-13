#### 超级无敌牛逼的史诗级的java巨作

# King of Bots

双人贪吃蛇对战平台：可以亲自上场，也可以上传一段 Java 代码让 Bot 替你出战。

## 架构

| 服务 | 端口 | 职责 |
|---|---|---|
| `web` | 80 / 443 | nginx：托管前端静态文件，把 `/api/` 和 `/websocket/` 转发给 `backend` |
| `backend` | 3000 | 登录注册、Bot 管理、WebSocket 对战、对局记录、排行榜 |
| `matchingsystem` | 3001 | 匹配池，按天梯分和等待时间撮合玩家 |
| `botrunningsystem` | 3002 | 在沙箱里编译并执行玩家的 Bot 代码 |
| `mysql` | 3306 | MySQL 8.4 |
| `redis` | 6379 | Redis 7，存放 refresh token 和 WebSocket 票据 |

前端是 Vue 3 + Vite + Pinia，后端是 Spring Boot 3.3（Java 17）+ MyBatis-Plus，三个后端服务之间通过 HTTP 互相调用。

## 部署（Docker）

只需要装 Docker。

### 1. 配置

把 `.env.example` 复制为 `.env`，填好三个必填项：

| 变量 | 说明 |
|---|---|
| `DB_PASSWORD` | 应用连接数据库用的密码（账号 `kob`，只有 `kob` 这一个库的权限） |
| `DB_ROOT_PASSWORD` | MySQL 的 root 密码，只在运维时使用，应用不使用 |
| `JWT_SECRET` | 签发 access token 的密钥，Base64 编码、解码后至少 32 字节 |

生成 `JWT_SECRET`：

```bash
openssl rand -base64 32
```

PowerShell 下：

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

### 2. 启动

```bash
docker compose up -d --build
```

打开 http://localhost 。80 端口被占用时，在 `.env` 里改 `WEB_PORT`。

第一次启动时 MySQL 会自动创建 `kob` 库、应用账号，并执行 `sql/schema.sql` 建表。数据保存在 Docker volume 里，`docker compose down` 不会丢，`docker compose down -v` 才会清空。

注意：MySQL 的账号和密码只在数据目录为空时初始化。之后再改 `.env` 里的数据库密码，不会改动已有的数据库，需要自己在库里 `ALTER USER`。

### 3. 启用 HTTPS

把证书放进 `certs/` 目录（`fullchain.pem` 和 `privkey.pem`），然后叠加 TLS 配置启动：

```bash
docker compose -f docker-compose.yml -f docker-compose.tls.yml up -d --build
```

启用后 HTTP 端口只做 301 跳转，refresh token 的 Cookie 带上 `Secure`，前端的 WebSocket 自动使用 `wss`。对公网提供服务时必须启用，否则 token 是明文传输的。

### 容器化的设计

- **镜像**：多阶段构建，运行镜像里没有源码和构建工具。后端用 Spring Boot 分层 jar，依赖层和业务代码层分开，改代码只重建最后一层。前端用非 root 的 nginx 镜像。
- **以非 root 运行**：后端固定 UID 10001，nginx 是 101。
- **加固**：应用容器的根文件系统只读，去掉全部 Linux capability，禁止提权（`no-new-privileges`）。
- **资源限制**：每个容器都限制了内存、CPU 和进程数，JVM 的堆大小跟随容器内存限制。
- **健康检查和启动顺序**：`backend` 等数据库和 Redis 就绪后才启动，`web` 等 `backend` 就绪后才启动。`backend` 的健康检查包含数据库和 Redis 的连通性。
- **优雅停机**：收到停止信号后先处理完在途请求再退出。
- **日志**：输出到标准输出，由 Docker 收集并按大小轮转。

网络分成三段，对外只暴露 `web` 一个端口：

| 网络 | 成员 | 说明 |
|---|---|---|
| `edge` | web、backend | 入口流量 |
| `services` | backend、matchingsystem、botrunningsystem | 服务间调用，没有外网出口 |
| `data` | backend、mysql、redis | 只有 `backend` 能访问数据库和 Redis，没有外网出口 |

- 服务之间的内部接口（`/pk/start/game/` 等）按来源 IP 做白名单，只放行 `services` 网段。nginx 转发来的请求走 `edge` 网络，来源 IP 不在白名单里；nginx 自己也直接对 `/api/pk/` 返回 404。两层互为兜底。
- `botrunningsystem` 执行的是用户提交的代码，所以只接入 `services` 网络：访问不了外网，也够不到数据库和 Redis。
- 登录相关接口在 nginx 层按来源 IP 限流。

## 本地开发

需要 JDK 17（必须是 JDK，Bot 运行服务要用到编译器）、Node.js 18+ 和 Docker。

只用容器跑 MySQL 和 Redis，端口映射到本机（只监听 `127.0.0.1`）：

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d mysql redis
```

端口取 `.env` 里的 `DB_PORT` 和 `REDIS_PORT`（默认 3306 和 6379）。本机运行的 `backend` 启动时会自动读取同一份 `.env`，所以改一处两边都生效。

启动后端，三个 `java -jar` 各占一个终端（Windows 下把 `./mvnw` 换成 `.\mvnw.cmd`）：

```bash
cd backendcloud
./mvnw -DskipTests package
java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
java -jar matchingsystem/target/matchingsystem-1.0-SNAPSHOT.jar
java -jar botrunningsystem/target/botrunningsystem-0.0.1-SNAPSHOT.jar
```

启动前端：

```bash
cd web
npm install
npm run dev
```

打开 http://localhost:8080 。前端换了端口或域名时，要把新地址加到后端的 `WEB_ORIGINS` 里，否则跨域请求会被拒绝。

不想用 Docker 的话，也可以自己装 MySQL 8 和 Redis 6.2+，创建 `kob` 库和应用账号后执行 `sql/schema.sql`。

## 登录鉴权

采用双 token：

| | access token | refresh token |
|---|---|---|
| 形式 | JWT | 32 字节随机串 |
| 有效期 | 15 分钟 | 14 天 |
| 服务端存储 | 不存，验签即可 | Redis，只存 SHA-256 哈希 |
| 前端存放 | 内存（Pinia） | HttpOnly + SameSite=Strict 的 Cookie |
| 用途 | 放在 `Authorization` 头里访问接口 | 只用来换新的 access token |

- 普通请求只验 JWT 签名，既不查 Redis 也不查数据库；只有刷新和退出才访问 Redis。
- refresh token 一次性使用：`/user/account/refresh/` 用 Redis 的 `GETDEL` 原子地取出并删除旧 token，再签发一对新的。同一个 refresh token 并发刷新时只有一个请求能成功。
- 退出登录会删除 Redis 里的 refresh token；已签发的 access token 最多再有效 15 分钟。
- 前端请求统一走 `web/src/api.js` 的 `apiFetch`：收到 401 时自动刷新并重试一次，刷新失败则跳转登录页。

### WebSocket 的鉴权

浏览器的 WebSocket API 不能自定义请求头，凭证只能放在 URL 里，而 URL 会被反向代理和网关记进访问日志。所以 URL 里不放 access token：

1. 前端先用 access token 调 `/user/account/ws-ticket/`，换一张票据。
2. 用票据建立连接：`/websocket/{ticket}/`。
3. 票据存在 Redis 里，30 秒过期，握手时用 `GETDEL` 取出即作废，只能用一次。

票据无效时服务端以 1008 状态码正常关闭连接。

## 对局

- **调度**：所有对局共用一个调度线程池，由定时任务按 500ms 的节拍推进。对局在等待玩家操作时不占用线程，同时进行的对局数不受线程数限制。
- **回合**：每回合先把局面发给 Bot，然后最多等 5 个节拍。双方操作到齐就结算，超时没有操作的一方判负。
- **并发**：玩家的操作由 WebSocket 线程或 Bot 回调线程写入，由调度线程读取，用一把锁保护。同一回合内先到先得，后到的操作不覆盖先到的；不合法的方向直接丢弃。
- **匹配**：断线时自动退出匹配池。只能用自己的 Bot 出战。

## 编写 Bot

Bot 是一个实现了 `BotInterface` 的 Java 类，包名和类名必须如下：

```java
package com.kob.botrunningsystem.Utils;

public class Bot implements com.kob.botrunningsystem.Utils.BotInterface {
    @Override
    public Integer nextMove(String input) {
        return 0; // 0 上、1 右、2 下、3 左
    }
}
```

`input` 的格式为 `地图#我的起点x#我的起点y#(我的操作序列)#对手起点x#对手起点y#(对手的操作序列)`，地图是 13×14 按行展开的 01 串，1 表示墙。

完整示例见 [Bot.java](backendcloud/botrunningsystem/src/main/java/com/kob/botrunningsystem/Utils/Bot.java)：它对每个能走的方向做一次 flood-fill，统计走过去之后还能到达多少个空格，选可达区域最大的方向，从而避开被墙和蛇身围住的死胡同。

## Bot 沙箱

`botrunningsystem` 收到一步 Bot 请求后的处理流程：

1. **线程池调度**：请求进入一个固定大小的线程池（2 到 4 个工作线程，有界队列）。队列满时采用 `CallerRunsPolicy`，由提交任务的请求线程自己执行，不丢弃任何一步，同时让上游变慢形成反压。
2. **编译**：按代码的 SHA-256 缓存，同一份代码只编译一次。
3. **在子进程中执行**：工作线程不直接运行用户代码，而是把任务交给一个预先启动好的子 JVM。每个子进程只执行一步就销毁，不复用。
4. **超时**：从任务交给已就绪的子进程开始计时，500ms 内没有结果就直接杀掉进程。因为杀的是进程而不是线程，死循环一定会被终止，工作线程随即回到池中。

隔离分三层：

| 层 | 手段 | 限制了什么 |
|---|---|---|
| 进程内 | SecurityManager，Bot 的类目录没有任何权限 | 读写文件、访问网络、启动进程、读环境变量、替换标准输出 |
| 进程 | 独立子 JVM，不继承服务的环境变量 | 堆内存 64 MB；和服务不共享内存；超时强杀 |
| 容器 | 只接入内部网络；只读文件系统；无 capability；CPU、内存、进程数配额 | 外网、数据库、Redis 都不可达；资源耗尽只影响这一个容器 |

已知的不足：

- SecurityManager 在 Java 17 已废弃、Java 24 被移除，届时进程内这一层会失效，只剩后两层。
- 所有 Bot 共用同一个容器和它的资源配额，没有做到每个 Bot 一个容器。
- 编译仍在服务进程内进行。
