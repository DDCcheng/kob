import { API_URL } from '@/config'
import { useUserStore } from '@/store/user'
import router from '@/router/index'

// 带着 access token 请求后端。access token 过期（401）时，用 refresh token 换新后重试一次
export async function apiFetch(path, options = {}) {
    const userStore = useUserStore();
    const send = () => fetch(API_URL + path, {
        ...options,
        headers: { ...options.headers, Authorization: "Bearer " + userStore.token },
    });

    let resp = await send();
    if (resp.status === 401) {
        if (await userStore.refresh()) {
            resp = await send();
        } else {
            router.push({ name: "user_account_login" });
        }
    }
    return resp;
}
