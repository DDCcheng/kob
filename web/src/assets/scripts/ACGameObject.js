const Ac_Game_Objects=[];

export class AcGameObject {
    constructor(){
        Ac_Game_Objects.push(this);
        this.timedelta=0;
        this.has_called_start=false;
    }

    start(){//执行一次

    }

    update(){//每一帧执行一次，除了第一帧

    }
    on_destroy(){

    }

    destroy(){
        this.on_destroy();
        for(let i of Ac_Game_Objects){
            const obj =Ac_Game_Objects[i];
            if(obj==this){
                Ac_Game_Objects.splice(i);
                break;
            }
        }
    }
}

let last_timestamp;
const step= timestamp=>{
    for(let obj of Ac_Game_Objects){
        if(!obj.has_called_start){
            obj.has_called_start=true;
            obj.start();
        }else{
            obj.timedelta=timestamp-last_timestamp;
            obj.update();
        }
    }
    last_timestamp=timestamp;
    requestAnimationFrame(step)
}


requestAnimationFrame(step)