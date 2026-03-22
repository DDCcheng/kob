import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        id: "",
        username: "",
        photo: "",
        token: "",
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
        logout() {
            this.token = "";
            this.username = "";
            this.id = "";
            this.photo = "";
            this.is_login = false;
            localStorage.removeItem("jwt_token");
        },
        login(data) {
            const body = new URLSearchParams();
            body.append("username", data.username);
            body.append("password", data.password);

            fetch("http://localhost:3000/user/account/token/", { method: "POST", body })
                .then(r => r.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        localStorage.setItem("jwt_token", res.token);
                        this.updateToken(res.token);
                        this.is_login = true;
                        data.success();
                    } else {
                        data.error();
                    }
                })
                .catch(() => data.error());
        },
        getinfo(data) {
            fetch("http://localhost:3000/user/account/info/", {
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
