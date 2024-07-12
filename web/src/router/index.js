import { createRouter, createWebHistory } from 'vue-router'
import PkindexView from '../views/PK/PkindexView'
import RanklistindexView from '../views/RankingList/RanklistindexView'
import UserBotindexView from '../views/User/bots/UserBotindexView'
import NotFound from '../views/error/NotFound'
import FightlistindexView from '../views/FightList/FightlistindexView';
import UserAccountLoginView from '@/views/User/account/UserAccountLoginView'
import UserRegisterView from '@/views/User/account/UserRegisterView'



const routes = [
  {
    path:"/pk/",
    name:"pk_index",
    component:PkindexView,
  },
  {
    path:"/fightlist/",
    name:"fightlist_index",
    component:FightlistindexView,
  },
  {
    path:"/ranklist/",
    name:"ranklist_index",
    component:RanklistindexView,
  },
  {
    path:"/user/bot/",
    name:"userbot_index",
    component:UserBotindexView,
  },
  {
    path:"/user/account/login",
    name:"user_account_login",
    component:UserAccountLoginView,
  },
  {
    path:"/user/account/register",
    name:"user_account_register",
    component:UserRegisterView,
  },
  {
    path:"/404/",
    name:"404",
    component:NotFound,
  },
  {
    path:"/",
    name:"home",
    redirect:"/pk/"
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

export default router
