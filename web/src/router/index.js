import { createRouter, createWebHistory } from 'vue-router'
import PkindexView from '../views/PK/PkindexView'
import RanklistindexView from '../views/RankingList/RanklistindexView'
import UserBotindexView from '../views/User/bots/UserBotindexView'
import NotFound from '../views/error/NotFound'
import RecordindexView from '../views/record/RecordindexView.vue';
import RecordContentView from '@/views/record/RecordContentView.vue'
import UserAccountLoginView from '@/views/User/account/UserAccountLoginView'
import UserRegisterView from '@/views/User/account/UserRegisterView'
import store from '@/store'


const routes = [
  {
    path:"/pk/",
    name:"pk_index",
    component:PkindexView,
    meta:{
      requestAuth:true
    }
  },
  {
    path:"/record/",
    name:"record_index",
    component:RecordindexView,
    meta:{
      requestAuth:true
    }
  },
  {
    path:"/record/:recordId/",
    name:"record_content",
    component:RecordContentView,
    meta:{
      requestAuth:true
    }
  },
  {
    path:"/ranklist/",
    name:"ranklist_index",
    component:RanklistindexView,
    meta:{
      requestAuth:true
    }
  },
  {
    path:"/user/bot/",
    name:"userbot_index",
    component:UserBotindexView,
    meta:{  
      requestAuth:true
    }
  },
  {
    path:"/user/account/login",
    name:"user_account_login",
    component:UserAccountLoginView,
    meta:{
      requestAuth:false
    }
  },
  {
    path:"/user/account/register",
    name:"user_account_register",
    component:UserRegisterView,
    meta:{
      requestAuth:false
    }
  },
  {
    path:"/404/",
    name:"404",
    component:NotFound,
    meta:{
      requestAuth:false
    }
  },
  {
    path:"/",
    name:"home",
    redirect:"/pk/",
    meta:{
      requestAuth:true
    }
  },
  {
    path:"/:catchAll(.*)",
    redirect:"/404/",
    name:"error"
  },
  
] 

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to,from,next)=>{
  if(to.meta.requestAuth&&!store.state.user.is_login){
    next({  name:"user_account_login"});
  }else{
    next();
  }

})

export default router
