# syntax=docker/dockerfile:1.7

# ---------- 构建阶段 ----------
FROM node:22-alpine AS build
WORKDIR /src
# 先只拷依赖清单，依赖没变时这一层直接命中缓存
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund
COPY . .
# 后端地址写成相对路径，由 nginx 转发，页面和接口同源
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# ---------- 运行阶段 ----------
# 非 root 运行的 nginx，监听 8080
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY nginx/kob/ /etc/nginx/kob/
COPY --from=build /src/dist/ /usr/share/nginx/html/
EXPOSE 8080
