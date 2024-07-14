<template>
    <ContentField>
        bot
    </ContentField>
</template>

<script>
import ContentField from '../../../components/ContentField.vue'
import $ from 'jquery'
import {useStore} from 'vuex'
export default {    
    components:{
        ContentField
    },

    setup(){
        const store=useStore();
        // $.ajax({
        //     url:"http://10.14.17.223:3000/user/bot/add/",
        //         type:"post",
        //         data:{
        //             title:"bot标题",
        //             description:"bot的描述",
        //             content:"bot的代码",
        //         },
        //         headers:{
        //                 Authorization:"Bearer " + store.state.user.token,
        //         },
        //         success(resp){
        //            console.log(resp)
        //         },         
        //         error(resp){
        //            console.log(resp)
        //             }         
        //         },)

        // $.ajax({
        //     url:"http://10.14.17.223:3000/user/bot/remove/",
        //         type:"post",
        //         data:{
        //             bot_id:4,
        //         },
        //         headers:{
        //                 Authorization:"Bearer " + store.state.user.token,
        //         },
        //         success(resp){
        //            console.log(resp)
        //         },         
        //         error(resp){
        //            console.log(resp)
        //             }         
        //         },)
        // $.ajax({
        //     url:"http://10.14.17.223:3000/user/bot/update/",
        //         type:"post",
        //         data:{
        //             bot_id:7,
        //             title:"bot标题7",
        //             description:"bot的描述7",
        //             content:"bot的代码7",
        //         },
        //         headers:{
        //                 Authorization:"Bearer " + store.state.user.token,
        //         },
        //         success(resp){
        //            console.log(resp)
        //         },         
        //         error(resp){
        //            console.log(resp)
        //             }         
        //         },)
        $.ajax({
            url:"http://10.14.17.223:3000/user/bot/getlist/",
                type:"get",
                headers:{
                        Authorization:"Bearer " + store.state.user.token,
                },
                success(resp){
                   console.log(resp)
                },         
                error(resp){
                   console.log(resp)
                    }         
                },)
    }



}</script>

<style scoped></style>