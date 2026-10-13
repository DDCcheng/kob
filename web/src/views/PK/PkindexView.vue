<template>
    <PlayGround v-if="pkStore.status==='playing'"/>
    <MatchGround v-if="pkStore.status==='matching'"/>
    <ResultBoard v-if="pkStore.loser!='none'"/>
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import { WS_URL } from '@/config'
import { apiFetch } from '@/api'
import { onMounted, onUnmounted } from 'vue'
import { usePkStore } from '@/store/pk'
import { useRecordStore } from '@/store/record'
import ResultBoard from '../../components/ResultBoard.vue';
import MatchGround from '@/components/MatchGround.vue'
export default {
    components:{ PlayGround, MatchGround, ResultBoard },
    setup() {
        const pkStore = usePkStore();
        const recordStore = useRecordStore();

        pkStore.updateLoser("none");
        recordStore.updateIsRecord(false);

        let socket = null;
        let unmounted = false;
        onMounted(async () => {
            pkStore.updateOpponent({
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            });
            // URL 会被代理和网关记进日志，所以不放 access token，先换一张 30 秒有效的一次性票据
            const resp = await apiFetch("/user/account/ws-ticket/", { method: "POST" });
            if (!resp.ok || unmounted) return;
            const { ticket } = await resp.json();
            socket = new WebSocket(`${WS_URL}/websocket/${ticket}/`);

            socket.onopen = () => {
                console.log("connected");
                pkStore.updateSocket(socket);
            }
            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === 'start-matching') {
                    pkStore.updateOpponent({ username: data.opponent_username, photo: data.opponent_photo });
                    setTimeout(() => { pkStore.updateStatus("playing"); }, 200);
                    pkStore.updateGame(data.game);
                } else if (data.event === "move") {
                    const game = pkStore.gameObject;
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.A_direction);
                    snake1.set_direction(data.B_direction);
                } else if (data.event === "result") {
                    const game = pkStore.gameObject;
                    const [snake0, snake1] = game.snakes;
                    if (data.loser === "all" || data.loser === "A") snake0.status = "die";
                    if (data.loser === "all" || data.loser === "B") snake1.status = "die";
                    pkStore.updateLoser(data.loser);
                }
            }
            socket.onclose = () => { console.log("disconnected"); }
        })
        onUnmounted(() => {
            unmounted = true;
            if (socket) socket.close();
            pkStore.updateStatus("matching");
        })

        return { pkStore }
    },
}
</script>

<style scoped></style>
