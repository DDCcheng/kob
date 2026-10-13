# syntax=docker/dockerfile:1.7
#
# 三个后端服务共用这一个 Dockerfile，用构建参数区分：
#   MODULE   要构建的模块：backend / matchingsystem / botrunningsystem
#   RUNTIME  运行时镜像：jre（默认）或 jdk。botrunningsystem 要现场编译 Bot 代码，必须用 jdk

ARG RUNTIME=jre

# ---------- 构建阶段 ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /src
COPY . .
# Maven 本地仓库放在 BuildKit 缓存里，重复构建不用重新下载依赖
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl "${MODULE}" -am -DskipTests package
# 把 fat jar 拆成几层：依赖很少变、业务代码经常变，分层后改代码只需要重建和推送最后一层
RUN mkdir /layers && cd /layers \
 && java -Djarmode=layertools -jar /src/${MODULE}/target/*.jar extract

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-${RUNTIME}-alpine
# 固定 UID 的非 root 用户
RUN addgroup -S -g 10001 kob && adduser -S -u 10001 -G kob kob
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
USER 10001:10001
# 堆大小跟随容器的内存限制；内存耗尽时直接退出，交给编排层重启
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    TZ=Asia/Shanghai
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
