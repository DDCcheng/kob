<template>
    <div class="matchground">
        <div class="row">
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.user.photo">
                </div>
                <div class="user-username">
                    {{ $store.state.user.username }}
                </div>
            </div>
            <div class="col-4">
                <div class="user-select-bot">
                    <select v-model="select_bot" class="form-select" aria-label="Default select example">
                        <option value="-1" selected>亲自出马</option>
                        <option v-for="bot in bots" :key="bot.id" :value="bot.id">{{ bot.title }}</option>
                    </select>
                </div>
            </div>
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.pk.opponent_photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.pk.opponent_username }}
                </div>
            </div>
            <div class="col-12" style="text-align: center; padding-top: 15vh;">
                <button type="button" class="btn btn-warning btn-lg" @click="click_match">{{ match_btn_content }}</button>
            </div>
        </div>
    </div>
</template>

<script>
import { ref } from 'vue'
import { useStore } from 'vuex'

export default {
    setup() {
        const store = useStore();
        let match_btn_content = ref("开始匹配");
        let bots = ref([]);
        let select_bot = ref("-1");

        const click_match = () => {
            if (match_btn_content.value === "开始匹配") {
                match_btn_content.value = "取消";
                store.state.pk.socket.send(JSON.stringify({
                    event: "start-matching",
                    bot_id: select_bot.value
                }));
            } else {
                match_btn_content.value = "开始匹配";
                store.state.pk.socket.send(JSON.stringify({ event: "stop-matching" }));
                store.commit("updateOpponent", {
                    username: "我的对手",
                    photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
                });
            }
        }

        const refresh_bots = () => {
            fetch("http://localhost:3000/user/bot/getlist/", {
                headers: { Authorization: "Bearer " + store.state.user.token }
            })
                .then(resp => resp.json())
                .then(res => { bots.value = res; });
        }

        refresh_bots();

        return { match_btn_content, click_match, bots, select_bot }
    }
}
</script>

<style scoped>
div.matchground {
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
    background-color: rgba(50, 50, 50, 0.5);
}
div.user-photo {
    text-align: center;
    padding-top: 8vh;
}
div.user-photo>img {
    width: 20vh;
    border-radius: 50%;
}
div.user-username {
    color: white;
    text-align: center;
    font-size: 20px;
    font-weight: 600;
    padding-top: 2vh;
}
div.user-select-bot {
    padding-top: 20vh;
}
div.user-select-bot>select {
    width: 80%;
    margin: 0 auto;
}
</style>
