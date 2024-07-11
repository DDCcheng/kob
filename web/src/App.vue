<template>
  <NavBar></NavBar>
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"
import $ from "jquery"
export default{
  components:{
    NavBar
  },
  setup() {
    $.ajax({
      url:"http://localhost:3000/user/account/token/",
      type:"post",
      data: {
        username:'yxc',
        password:"pyxc",
      },
      success(res){
        console.log(res)
      },
      error(res){
        console.log(res)
      }
    })
  }
}
</script>
<style>
body{
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}
</style>
