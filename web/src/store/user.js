export default {
    state: {
        id: "",
        username: "",
        photo: "",
        token: "",
        is_login: false,
        loading_info: true,
    },
    getters: {
    },
    mutations: {
        updateUser(state, user) {
            state.id = user.id;
            state.username = user.username;
            state.photo = user.photo;
            state.is_login = true;
        },
        updateToken(state, token) {
            state.token = token;
        },
        logout(state) {
            state.token = "";
            state.username = "";
            state.id = "";
            state.photo = "";
            state.is_login = false;
        },
        update_loadinginfo(state, loading_info) {
            state.loading_info = loading_info;
        }
    },
    actions: {
        login(context, data) {
            const body = new URLSearchParams();
            body.append("username", data.username);
            body.append("password", data.password);

            fetch("http://localhost:3000/user/account/token/", {
                method: "POST",
                body,
            })
                .then(resp => resp.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        localStorage.setItem("jwt_token", res.token);
                        context.commit("updateToken", res.token);
                        context.state.is_login = true;
                        data.success();
                    } else {
                        data.error();
                    }
                })
                .catch(() => data.error());
        },

        getinfo(context, data) {
            fetch("http://localhost:3000/user/account/info/", {
                headers: {
                    Authorization: "Bearer " + context.state.token,
                },
            })
                .then(resp => {
                    if (!resp.ok) throw new Error();
                    return resp.json();
                })
                .then(resp => {
                    if (resp.error_message === "SUCCESS") {
                        context.commit("updateUser", resp);
                        if (data && data.success) data.success();
                    } else {
                        if (data && data.error) data.error();
                    }
                })
                .catch(() => {
                    if (data && data.error) data.error();
                });
        },

        logout(context) {
            context.commit("logout");
            localStorage.removeItem("jwt_token");
        },
    },
    modules: {
    }
}
