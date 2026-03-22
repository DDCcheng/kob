<template>
    <ContentField v-if="!userStore.loading_info">
    <div class="row  justify-content-md-center">
        <div class="col-3">
            <form @submit.prevent="login" >
                <div class="mb-3">
                <label for="username" class="form-label">用户名</label>
                <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名">
                </div>
                <div class="mb-3">
                <label for="password" class="form-label">密码</label>
                <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码">
                </div>
                <div class="error_message">{{ error_message }}</div>
                <button type="submit" class="btn btn-primary">提交</button>
            </form>
        </div>
    </div>
 </ContentField>
</template>

<script>
import ContentField from '@/components/ContentField.vue'
import { useUserStore } from '@/store/user'
import { ref } from 'vue';
import router from '@/router/index'
export default {
    components:{ ContentField },
    setup(){
        const userStore = useUserStore();
        let username = ref('');
        let password = ref('');
        let error_message = ref('');

        const jwt_token = localStorage.getItem("jwt_token");
        if (jwt_token) {
            userStore.updateToken(jwt_token);
            userStore.getinfo({
                success() {
                    router.push({ name: "home" });
                    userStore.update_loadinginfo(false);
                },
                error() {
                    userStore.update_loadinginfo(false);
                }
            });
        } else {
            userStore.update_loadinginfo(false);
        }

        const login = () => {
            error_message.value = "";
            userStore.login({
                username: username.value,
                password: password.value,
                success() {
                    userStore.getinfo({
                        success() {
                            router.push({ name: "home" });
                        },
                    });
                },
                error() {
                    error_message.value = "用户名密码不正确";
                }
            });
        }

        return { userStore, username, password, error_message, login }
    }
}
</script>

<style scoped>
button{ width: 100%; }
div.error_message{ color: red; }
</style>
