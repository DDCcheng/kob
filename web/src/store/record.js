import { defineStore } from 'pinia'

export const useRecordStore = defineStore('record', {
    state: () => ({
        is_record: false,
        a_steps: "",
        b_steps: "",
        record_loser: "",
    }),
    actions: {
        updateIsRecord(is_record) { this.is_record = is_record; },
        updateSteps(data) {
            this.a_steps = data.a_steps;
            this.b_steps = data.b_steps;
        },
        updateRecordLoser(loser) { this.record_loser = loser; },
    }
})
