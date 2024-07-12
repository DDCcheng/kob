import $ from "jquery"




export default{
    state: {
        id:"",
        username:"",
        photo:"",
        token:"",
        is_login:false
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
        }
    },
    actions: {
        login(context,data){
            $.ajax({
                url:"http://localhost:3000/user/account/token/",
                type:"post",
                data: {
                  username:data.username,
                  password:data.password,
                },
                success(res){
                    if(res.error_message==="success"){
                        context.commit("updateToken",res.token);
                        context.state.is_login=true;
                        data.success()
                    }
                },
                error(){
                    data.error();
                }
              })
        },
        getinfo(context,data){
            $.ajax({
                url:"http://localhost:3000/user/account/info/",
                type:"get",
                headers:{
                        Authorization:"Bearer " + context.state.token,
                },
                success(resp){
                    if(resp.error_message==="SUCCESS"){
                        context.commit("updateUser",{
                            ...resp,
                            is_login:true
                        }),
                        data.success(resp);
                    }         
                },
                error(resp){
                        data.error(resp);
                }
            })
        },
        logout(context){
            context.commit("logout");
        }

    },
    modules: {
    }
}