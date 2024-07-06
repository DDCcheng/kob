<template>
  <div>
    <div>botname:{{ bot_name }}</div>
    <div>botstrength:{{ bot_strength }}</div>
  </div>
  <router-view/>
</template>

<script>
import $ from 'jquery';
import { ref } from "vue";
export default{
  name:"App",
  setup:()=>{
    let bot_name=ref("");
    let bot_strength=ref("");

    $.ajax({
      url:"http://127.0.0.1:3000/PK/getbotinfo/",
      type:"get",
      success: resp=> {
        bot_name.value=resp.name,
        bot_strength.value=resp.rating;
      }
    });
    return {
      bot_name,
      bot_strength
    }
  }
}
</script>
<style>
body{
  background-image: url("@/assets/background.png");
  background-size: cover;
}
</style>
