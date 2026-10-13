// 后端地址，通过环境变量 VITE_API_URL / VITE_WS_URL 覆盖（见 .env.example）。
// 本地开发时直连后端；容器部署时是相对路径 /api，由 nginx 转发，和页面同源
export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:3000";

// API_URL 是相对路径时，WebSocket 跟随当前页面的协议和域名（https 页面自动用 wss）
const origin = API_URL.startsWith("/") ? location.origin : API_URL;
export const WS_URL = import.meta.env.VITE_WS_URL || origin.replace(/^http/, "ws");