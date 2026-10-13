// 后端地址，部署时通过环境变量 VITE_API_URL / VITE_WS_URL 覆盖（见 .env.example）
export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:3000";
export const WS_URL = import.meta.env.VITE_WS_URL || API_URL.replace(/^http/, "ws");