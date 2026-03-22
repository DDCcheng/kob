<template>
    <PlayGround v-if="pkStore.status==='playing'"/>
    <MatchGround v-if="pkStore.status==='matching'"/>
    <ResultBoard v-if="pkStore.loser!='none'"/>
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import { onMounted, onUnmounted } from 'vue'
import { usePkStore } from '@/store/pk'
import { useUserStore } from '@/store/user'
import { useRecordStore } from '@/store/record'
import ResultBoard from '../../components/ResultBoard.vue';
import MatchGround from '@/components/MatchGround.vue'
export default {
    components:{ PlayGround, MatchGround, ResultBoard },
    setup() {
        const pkStore = usePkStore();
        const userStore = useUserStore();
        const recordStore = useRecordStore();

        const socket_url = `ws://localhost:3000/websocket/${userStore.token}/`;

        pkStore.updateLoser("none");
        recordStore.updateIsRecord(false);

        let socket = null;
        onMounted(() => {
            pkStore.updateOpponent({
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            });
            socket = new WebSocket(socket_url);

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
            socket.close();
            pkStore.updateStatus("matching");
        })

        return { pkStore }
    },
}
</script>

<style scoped></style>
