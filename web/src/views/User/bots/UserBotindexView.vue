<template>
    <div class="container">
        <div class="row">
            <div class="col-3">
                <div class="card" style="margin-top: 20px;">
                    <div class="card-body">
                        <img :src="$store.state.user.photo" alt="" style="width: 100%;">
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card" style="margin-top: 20px;">
                    <div class="card-header">
                        <span style="font-size: 140%;">我的bot</span>
                        <button type="button" class="btn btn-primary float-end" data-bs-toggle="modal" data-bs-target="#add_bot_btn">
                             添加bot</button>

                        <div class="modal fade" id="add_bot_btn" tabindex="-1">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                <div class="modal-header">
                                 <h1 class="modal-title fs-5">创建bot</h1>
                                 <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                 </div>
                                 <div class="modal-body">
                                    <div class="mb-3">
                                        <label for="add_bot_title" class="form-label">名称</label>
                                        <input type="text" v-model="botadd.title" class="form-control" id="add_bot_title" placeholder="请输入bot名称">
                                    </div>
                                    <div class="mb-3">
                                        <label for="add_bot_description" class="form-label">bot简介</label>
                                        <textarea class="form-control" v-model="botadd.description" id="add_bot_description" rows="3" placeholder="请输入bot简介"></textarea>
                                    </div>
                                    <div class="mb-3">
                                        <label for="add_bot_code" class="form-label">bot代码</label>
                                        <VAceEditor
                                            v-model:value="botadd.content"
                                            @init="editorInit"
                                            lang="c_cpp"
                                            theme="textmate"
                                            style="height: 300px"
                                            :options="{
                                                enableBasicAutocompletion: true,
                                                enableSnippets: true,
                                                enableLiveAutocompletion: true,
                                                fontSize: 14,
                                                tabSize: 2,
                                                showPrintMargin: false,
                                                highlightActiveLine: true,
                                            }"
                                        />
                                    </div>
                                 </div>
                                 <div class="modal-footer">
                                    <div class="error_message">{{ botadd.error_message }}</div>
                                    <button type="button" class="btn btn-primary" @click.prevent="add_btn">提交</button>
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                 </div>
                             </div>
                             </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th>Bot名称</th>
                                    <th>创建时间</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="bot in bots" :key="bot.id">
                                    <td>{{ bot.title }}</td>
                                    <td>{{ bot.createtime }}</td>
                                    <td>
                                        <button type="button" class="btn btn-secondary" style="margin-right: 10px;"
                                        data-bs-toggle="modal" :data-bs-target="'#update_btn'+bot.id">修改</button>
                                        <button type="button" class="btn btn-danger" @click.prevent="remove_btn(bot)">删除</button>

                                        <div class="modal fade" :id="'update_btn'+bot.id" tabindex="-1">
                                            <div class="modal-dialog modal-xl">
                                                <div class="modal-content">
                                                <div class="modal-header">
                                                 <h1 class="modal-title fs-5">更新bot</h1>
                                                 <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                 </div>
                                                 <div class="modal-body">
                                                    <div class="mb-3">
                                                        <label class="form-label">名称</label>
                                                        <input type="text" v-model="bot.title" class="form-control" placeholder="请输入bot名称">
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">bot简介</label>
                                                        <textarea class="form-control" v-model="bot.description" rows="3" placeholder="请输入bot简介"></textarea>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">bot代码</label>
                                                        <VAceEditor
                                                            v-model:value="bot.content"
                                                            @init="editorInit"
                                                            lang="c_cpp"
                                                            theme="textmate"
                                                            style="height: 300px"
                                                            :options="{
                                                                enableBasicAutocompletion: true,
                                                                enableSnippets: true,
                                                                enableLiveAutocompletion: true,
                                                                fontSize: 14,
                                                                tabSize: 2,
                                                                showPrintMargin: false,
                                                                highlightActiveLine: true,
                                                            }"
                                                        />
                                                    </div>
                                                 </div>
                                                 <div class="modal-footer">
                                                    <div class="error_message">{{ bot.error_message }}</div>
                                                    <button type="button" class="btn btn-primary" @click.prevent="update_btn(bot)">保存修改</button>
                                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                                 </div>
                                             </div>
                                             </div>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useStore } from "vuex";
import { Modal } from 'bootstrap';
import { VAceEditor } from 'vue3-ace-editor';
import ace from 'ace-builds';
import 'ace-builds/src-noconflict/mode-c_cpp';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-chrome';
import 'ace-builds/src-noconflict/ext-language_tools';

export default {
    components: { VAceEditor },
    setup() {
        ace.config.set("basePath",
            "https://cdn.jsdelivr.net/npm/ace-builds@" + ace.version + "/src-noconflict/");

        const store = useStore();
        let bots = ref([]);

        const botadd = reactive({
            title: "",
            description: "",
            content: "",
            error_message: ""
        });

        const authHeader = () => ({ Authorization: "Bearer " + store.state.user.token });
        const refresh_bots = () => {
            fetch("http://localhost:3000/user/bot/getlist/", { headers: authHeader() })
                .then(resp => resp.json())
                .then(res => { bots.value = res; });
        }
        const add_btn = () => {
            botadd.error_message = "";
            const body = new URLSearchParams();
            body.append("title", botadd.title);
            body.append("description", botadd.description);
            body.append("content", botadd.content);

            fetch("http://localhost:3000/user/bot/add/", {
                method: "POST",
                headers: authHeader(),
                body,
            })
                .then(resp => resp.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        botadd.title = "";
                        botadd.content = "";
                        botadd.description = "";
                        Modal.getInstance("#add_bot_btn").hide();
                        refresh_bots();
                    } else {
                        botadd.error_message = res.error_message;
                    }
                });
        }

        const remove_btn = bot => {
            const body = new URLSearchParams();
            body.append("bot_id", bot.id);

            fetch("http://localhost:3000/user/bot/remove/", {
                method: "POST",
                headers: authHeader(),
                body,
            })
                .then(resp => resp.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        refresh_bots();
                    } else {
                        bot.error_message = res.error_message;
                    }
                });
        }

        const update_btn = bot => {
            const body = new URLSearchParams();
            body.append("bot_id", bot.id);
            body.append("title", bot.title);
            body.append("description", bot.description);
            body.append("content", bot.content);

            fetch("http://localhost:3000/user/bot/update/", {
                method: "POST",
                headers: authHeader(),
                body,
            })
                .then(resp => resp.json())
                .then(res => {
                    if (res.error_message === "SUCCESS") {
                        Modal.getInstance('#update_btn' + bot.id).hide();
                        refresh_bots();
                    } else {
                        botadd.error_message = res.error_message;
                    }
                });
        }

        refresh_bots();

        return { bots, botadd, add_btn, remove_btn, update_btn }
    }
}
</script>

<style scoped>
div.error_message {
    color: red;
}
</style>
