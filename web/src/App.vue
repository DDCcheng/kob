<template>
  <NavBar></NavBar>
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"
// import $ from "jquery"
// const token="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI0ZWUyZjMwYzc3ZGU0MGNlOGRkZjA1NjAxZjE5ZWZmNCIsInN1YiI6IjIiLCJpc3MiOiJzZyIsImlhdCI6MTcyMDc2NDIyNCwiZXhwIjoxNzIxOTczODI0fQ.ERzeM4tGxkf-ztYu255IQkR5hEQcu-eO0pXC5lFHMBo"
export default{
  components:{
    NavBar
  },
  // setup() {
  //   $.ajax({
  //     url:"http://localhost:3000/user/account/token/",
  //     type:"post",
  //     data: {
  //       username:'b',
  //       password:"pb",
  //     },
  //     success(res){
  //       console.log(res)
  //     },
  //     error(res){
  //       console.log(res)
  //     }
  //   }),
  //   $.ajax({
  //     url:"http://localhost:3000/user/account/info/",
  //     type:"get",
  //     headers:{
  //       Authorization:"Bearer " + token
  //     },
  //     success(resp){
  //       console.log(resp);
  //     },
  //     error(res){
  //       console.log(res)
  //     }
  //   }
  // ),
  // $.ajax({
  //     url:"http://localhost:3000/user/account/register/",
  //     type:"post",
  //     data:{
  //       username:"cc",
  //       password:"pcc",
  //       confirmPassword:"pcc",
  //     },
  //     success(resp){
  //       console.log(resp);
  //     },
  //     error(resp){
  //       console.log(resp)
  //     } 
  //   }
  // )
  // }
}
</script>
<style>
body{
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}
</style>
