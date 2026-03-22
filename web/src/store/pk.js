import { defineStore } from 'pinia'

export const usePkStore = defineStore('pk', {
    state: () => ({
        status: "matching",
        socket: null,
        opponent_username: "",
        opponent_photo: "",
        gamemap: null,
        a_id: 0,
        a_sx: 0,
        a_sy: 0,
        b_id: 0,
        b_sx: 0,
        b_sy: 0,
        gameObject: null,
        loser: "none",
    }),
    actions: {
        updateSocket(socket) { this.socket = socket; },
        updateOpponent(opponent) {
            this.opponent_username = opponent.username;
            this.opponent_photo = opponent.photo;
        },
        updateStatus(status) { this.status = status; },
        updateGame(game) {
            this.gamemap = game.map;
            this.a_id = game.a_id;
            this.a_sx = game.a_sx;
            this.a_sy = game.a_sy;
            this.b_id = game.b_id;
            this.b_sx = game.b_sx;
            this.b_sy = game.b_sy;
        },
        updateGameObject(gameObject) { this.gameObject = gameObject; },
        updateLoser(loser) { this.loser = loser; },
    }
})
