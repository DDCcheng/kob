import { defineStore } from 'pinia'
import { API_URL } from '@/config'

// 同一时刻只发一个刷新请求：refresh token 是一次性的，并发刷新会互相作废
let refreshing = null;

export const useUserStore = defineStore('user', {
    state: () => ({
        id: "",
        username: "",
        photo: "",
        token: "",  // access token，只放在内存里；refresh token 在 HttpOnly Cookie 中，JS 读不到
        is_login: false,
        loading_info: true,
    }),
    actions: {
        updateUser(user) {
            this.id = user.id;
            this.username = user.username;
            this.photo = user.photo;
            this.is_login = true;
        },
        updateToken(token) {
            this.token = token;
        },
        update_loadinginfo(loading_info) {
            this.loading_info = loading_info;
        },
        clear() {
            this.token = "";
            this.username = "";
            this.id = "";
            this.photo = "";
            this.is_login = false;
        },
        logout() {
            this.clear();
            fetch(`${API_URL}/user/account/logout/`, { method: "POST", credentials: "include" })
                .catch(() => {});
        },
        login(data) {
            const body = new URLSearchParams();
            body.append("username", data.username);
            body.append("password", data.password);

            fetch(`${API_URL}/user/account/token/`, { method: "POST", body, credentials: "include" })
                .then(r => r.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        this.updateToken(res.token);
                        this.is_login = true;
                        data.success();
                    } else {
                        data.error();
                    }
                })
                .catch(() => data.error());
        },
        // 用 refresh token 换一个新的 access token，成功返回 true
        refresh() {
            if (!refreshing) {
                refreshing = fetch(`${API_URL}/user/account/refresh/`, { method: "POST", credentials: "include" })
                    .then(r => r.json())
                    .then(res => {
                        if (res.error_message !== "SUCCESS") {
                            this.clear();
                            return false;
                        }
                        this.updateToken(res.token);
                        return true;
                    })
                    .catch(() => false)
                    .finally(() => { refreshing = null; });
            }
            return refreshing;
        },
        getinfo(data) {
            fetch(`${API_URL}/user/account/info/`, {
                headers: { Authorization: "Bearer " + this.token },
            })
                .then(r => { if (!r.ok) throw new Error(); return r.json(); })
                .then(resp => {
                    if (resp.error_message === "SUCCESS") {
                        this.updateUser(resp);
                        if (data && data.success) data.success();
                    } else {
                        if (data && data.error) data.error();
                    }
                })
                .catch(() => { if (data && data.error) data.error(); });
        },
    }
})
