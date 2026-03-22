<template>
    <div class="result-board">
        <div class="result-board-text" v-if="pkStore.loser==='all'">Draw</div>
        <div class="result-board-text" v-else-if="pkStore.loser==='A' && pkStore.a_id==parseInt(userStore.id)">Lose</div>
        <div class="result-board-text" v-else-if="pkStore.loser==='B' && pkStore.b_id===parseInt(userStore.id)">Lose</div>
        <div class="result-board-text" v-else>Win</div>
        <div class="result-board-btn">
            <button @click="restart" type="button" class="btn btn-warning btn-lg">再来!</button>
        </div>
    </div>
</template>

<script>
import { usePkStore } from '@/store/pk'
import { useUserStore } from '@/store/user'
export default{
    setup(){
        const pkStore = usePkStore();
        const userStore = useUserStore();

        const restart = () => {
            pkStore.updateStatus("matching");
            pkStore.updateLoser("none");
            pkStore.updateOpponent({
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            });
        }
        return { pkStore, userStore, restart };
    }
}
</script>

<style scoped>
div.result-board{
    height: 30vh;
    width: 30vw;
    background-color: rgba(50, 50, 50, 0.5);
    position: absolute;
    top: 30vh;
    left: 35vw;
}
div.result-board-text{
    color: white;
    font-size: 50px;
    font-weight: 600;
    font-style: italic;
    text-align: center;
    padding-top: 5vh;
}
div.result-board-btn{
    padding-top: 6vh;
    text-align: center;
}
</style>
