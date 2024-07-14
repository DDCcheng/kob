import $ from "jquery"
export default{
    state: {
        id:"",
        username:"",
        photo:"",
        token:"",
        is_login:false,
        loading_info:true,//是否正在加载信息
    },
    getters: {
    },
    mutations: {
        updateUser(state ,user){
            state.id=user.id;
            state.username=user.username;
            state.photo=user.photo;
            state.user=user.is_login;
        },
        updateToken(state,token){
            state.token=token;
        },
        logout(state){
            state.token="",
            state.username="",
            state.id="",
            state.photo="",
            state.is_login=false
        },
        update_loadinginfo(state,loading_info){
            state.loading_info=loading_info
        }
    },
    actions: {
        login(context,data){
            $.ajax({
                url:"http://10.14.17.223:3000/user/account/token/",
                type:"post",
                data: {
                  username:data.username,
                  password:data.password,
                },
                success(res){
                    if(res.error_message==="SUCCESS"){
                        localStorage.setItem("jwt_token",res.token)
                        context.commit("updateToken",res.token);
                        context.state.is_login=true;    
                        data.success()
                    }
                },
                error(resp){
                    data.error(resp);
                }
              })
        },
        getinfo(context,data){
            $.ajax({
                url:"http://10.14.17.223:3000/user/account/info/",
                type:"get",
                headers:{
                        Authorization:"Bearer " + context.state.token,
                },
                success(resp){
                    if(resp.error_message==="SUCCESS"){
                        context.commit("updateUser",{
                            ...resp,
                            // is_login:true,//不起作用，y总的不知道为什么起作用了
                        }),
                        context.state.is_login=true;    
                        data.success();
                    }         
                },
                error(){
                        data.error();
                }
            })
        },
        logout(context){
            context.commit("logout");
            localStorage.removeItem("jwt_token")
        },
        

    },
    modules: {
    }
}