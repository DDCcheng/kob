<template>
    <div class="matchground">
        <div class="row">
            <div class="col-6">
                <div class="user-photo">
                    <img :src="$store.state.user.photo">
                </div>
                <div class="user-username">
                    {{ $store.state.user.username }}
                </div>
            </div>
            <div class="col-6">
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
import {ref} from 'vue'
import {useStore} from 'vuex'
   export default{
    components:{
    },
    setup(){
        const store=useStore()
        let match_btn_content=ref("开始匹配")

        const click_match=()=>{
            if(match_btn_content.value==="开始匹配"){
                match_btn_content.value="取消"
                store.state.pk.socket.send(JSON.stringify({
                    event:"start-matching"
                }));
            }else{
                match_btn_content.value="开始匹配"
                store.state.pk.socket.send(JSON.stringify({
                    event:"stop-matching"
                }))
                store.commit("updateOpponent",{
                    username:"我的对手",
                    photo:"https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
                })
            }
        }
        return {
            match_btn_content,
            click_match
        }
    }
   }
   
</script>
   
<style scoped>
div.matchground{
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
    background-color: rgba(50, 50, 50, 0.5);
}
div.user-photo {
    text-align: center;
    padding-top: 8vh;
}
div.user-photo>img{
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
</style>
