#### 超级无敌牛逼的史诗级的java巨作

# King of Bots

双人贪吃蛇对战平台：可以亲自上场，也可以上传一段 Java 代码让 Bot 替你出战。

## 架构

| 模块 | 端口 | 职责 |
|---|---|---|
| `web/` | 8080 | 前端：Vue 3 + Vite + Pinia + Bootstrap 5 |
| `backendcloud/backend` | 3000 | 登录注册（JWT）、Bot 管理、WebSocket 对战、对局记录、排行榜 |
| `backendcloud/matchingsystem` | 3001 | 匹配池，按天梯分和等待时间撮合玩家 |
| `backendcloud/botrunningsystem` | 3002 | 编译并执行玩家的 Bot 代码 |

后端为 Spring Boot 3.3 + MyBatis-Plus + MySQL，三个服务之间通过 HTTP 互相调用。

## 环境要求

- JDK 17（必须是 JDK，Bot 运行服务需要用到编译器）
- Node.js 18+
- MySQL 8

Maven 不需要单独安装，用仓库自带的 `mvnw` 即可。

## 启动

### 1. 建库建表

```bash
mysql -u root -p < sql/schema.sql
```

### 2. 配置环境变量

`backend` 启动必须有 `DB_PASSWORD` 和 `JWT_SECRET`，其余变量都有默认值，完整列表见 [.env.example](.env.example)。

生成 `JWT_SECRET`（PowerShell）：

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

或者（bash）：

```bash
openssl rand -base64 32
```

把 `.env.example` 复制为仓库根目录下的 `.env` 并填好这两项即可：`backend` 启动时会自动读取它（从仓库根目录或 `backendcloud` 目录启动都可以）。系统环境变量的优先级高于 `.env`，部署时可以直接用环境变量覆盖。

### 3. 启动后端

```bash
cd backendcloud
./mvnw -DskipTests package
java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
java -jar matchingsystem/target/matchingsystem-1.0-SNAPSHOT.jar
java -jar botrunningsystem/target/botrunningsystem-0.0.1-SNAPSHOT.jar
```

三个 `java -jar` 各占一个终端。Windows 下把 `./mvnw` 换成 `.\mvnw.cmd`。

### 4. 启动前端

```bash
cd web
npm install
npm run dev
```

打开 http://localhost:8080 。后端不在本机时，复制 `web/.env.example` 为 `web/.env.local` 并修改 `VITE_API_URL`。

## 编写 Bot

Bot 是一个实现了 `BotInterface` 的 Java 类，包名和类名必须如下，示例见 [Bot.java](backendcloud/botrunningsystem/src/main/java/com/kob/botrunningsystem/Utils/Bot.java)：

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

### Bot 的运行限制

每一步都在独立的子 JVM 中执行，不与服务共享内存：

- 单步限时 2 秒，超时直接杀掉进程，视为没有操作
- 堆内存上限 64 MB
- 禁止读写文件、访问网络、启动进程、读取环境变量（基于 SecurityManager）

SecurityManager 在 Java 24 被移除，届时只剩进程级的隔离。对外公开部署时，建议把 `botrunningsystem` 整体放进没有外网权限的容器里运行。
