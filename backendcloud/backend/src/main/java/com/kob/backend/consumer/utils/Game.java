package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    //所有对局共用一个调度线程池，由定时任务按节拍推进，对局在等待时不占用线程
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread thread = new Thread(r, "game-scheduler");
                thread.setDaemon(true);
                return thread;
            });

    private static final long TICK = 500;//节拍，单位毫秒

    private static final int MAX_WAIT_TICKS = 5;//每回合最多等待玩家操作的节拍数

    private static final int MAX_ROUNDS = 1000;

    private int round = 0;

    private final Integer rows;

    private final Integer cols;

    private final Integer inner_walls_count;

    private final int [][] g;

    private final static int[] dx={-1,0,1,0};

    private final static int[] dy={0,1,0,-1};

    private final Player playerA,playerB;

    private  Integer nextstepA=null;

    private  Integer nextstepB=null;

    private ReentrantLock lock = new ReentrantLock();

    private volatile String status="playing";//playing表示正在游戏,over表示游戏结束

    private String loser="";//all表示平局,A表示A输

    public  Game(Integer rows, Integer cols,
                 Integer inner_walls_count,
                 Integer idA, Integer idB,
                 Bot botA, Bot botB)
    {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA=-1,botIdB=-1;
        String botCodeA=null,botCodeB=null;
        if(botA!=null){
            botIdA=botA.getId();
            botCodeA=botA.getContent();
        }
        if(botB!=null){
            botIdB=botB.getId();
            botCodeB=botB.getContent();
        }
        playerA =new Player(idA,botIdA,botCodeA,rows-2,1,new ArrayList<>());
        playerB =new Player(idB,botIdB,botCodeB,1,cols-2,new ArrayList<>());
    }

    public Player getPlayerA(){
        return playerA;
    }

    public Player getPlayerB(){
        return playerB;
    }

    private boolean acceptable(Integer direction){//对局进行中，且方向合法
        return "playing".equals(status)&&direction!=null&&direction>=0&&direction<4;
    }

    public void setNextstepA(Integer nextstepA){
        if(!acceptable(nextstepA)) return;
        lock.lock();
        try {
            if(this.nextstepA==null){//同一回合先到先得，后到的操作不覆盖
                this.nextstepA=nextstepA;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setNextstepB(Integer nextstepB){
        if(!acceptable(nextstepB)) return;
        lock.lock();
        try {
            if(this.nextstepB==null){
                this.nextstepB=nextstepB;
            }
        } finally {
            lock.unlock();
        }
    }

    public int [][] getG(){
        return g;
    }

    private boolean checkConnectivity(int sx,int sy,int tx,int ty){
        if(sx==tx&&sy==ty) return true;
        g[sx][sy]=1;
        for(int i=0;i<4;i++){
            int x=sx+dx[i],y=sy+dy[i];
            if(x>=0&&x<this.rows&&y>=0&&y<this.cols&&g[x][y]==0){
                if(checkConnectivity(x,y,tx,ty)) {
                    g[sx][sy]=0;
                    return true;
                }
            }
        }
        g[sx][sy]=0;
        return false;
    }

    private boolean draw(){
        for(int r = 0; r < this.rows; r++){
            for(int c = 0; c < this.cols; c++){
                g[r][c]=0;
            }
        }
        for(int r = 0; r < this.rows; r++){
            g[r][0]=g[r][this.cols-1]=1;
        }
        for(int r = 0; r < this.cols; r++){
            g[0][r]=g[this.rows-1][r]=1;
        }
        Random random=new Random();
        for(int i=0;i<this.inner_walls_count/2;i++){
            for(int j=0;j<1000;j++){
                int r= random.nextInt(this.rows);
                int c= random.nextInt(this.cols);
                if(g[r][c]==1||g[this.rows-1-r][this.cols-1-c]==1) continue;
                if(r==this.rows-2&& c==1||r==1&&c==this.cols-2) continue;
                g[r][c]=g[this.rows-1-r][this.cols-1-c]=1;
                break;
            }
        }
        return checkConnectivity(this.rows-2,1,1,this.cols-2);
    }

    public void createMap(){
        for(int i=0;i<1000;i++){
            if(draw())
                break;
        }
    }

    private String getInput(Player player){//将当前的局面信息转换成字符串
        Player me,you;
        if(playerA.getId().equals(player.getId())){
            me=playerA;
            you=playerB;
        }else {
            me=playerB;
            you=playerA;
        }
        return getMapString()+"#"+
                me.getSx()+"#"+
                me.getSy()+"#("+
                me.getStepsString()+")#"+
                you.getSx()+"#"+
                you.getSy()+"#("+
                you.getStepsString()+")";
    }

    private void  sendBotCode(Player player){
        if(player.getBotId().equals(-1)) return;
        MultiValueMap<String,String> data=new LinkedMultiValueMap<>();
        data.add("user_id",player.getId().toString());
        data.add("bot_code",player.getBotCode());
        data.add("input",getInput(player));
        WebSocketServer.restTemplate.postForObject(WebSocketServer.addBotUrl,data,String.class);
    }

    private void schedule(Runnable step){//一个节拍之后执行对局的下一个阶段
        scheduler.schedule(() -> {
            try {
                step.run();
            } catch (Exception e) {//出现意外时按平局结束，不让对局悬在半空
                e.printStackTrace();
                if("playing".equals(status)){
                    status="over";
                    loser="all";
                    sendAllMessage("{\"event\":\"result\",\"loser\":\"all\"}");
                }
            }
        }, TICK, TimeUnit.MILLISECONDS);
    }

    public void start(){
        schedule(this::beginRound);
    }

    private void beginRound(){//回合开始：把局面发给Bot，然后等待两名玩家的操作
        for(Player player:List.of(playerA,playerB)){
            try {
                sendBotCode(player);
            } catch (Exception e) {//Bot服务不可用时，这名玩家会因为超时没有操作而判负
                System.out.println("send bot code failed: "+e.getMessage());
            }
        }
        schedule(() -> waitForSteps(1));
    }

    private void waitForSteps(int tick){//每个节拍检查一次两名玩家的操作是否到齐
        boolean ready;
        lock.lock();
        try {
            ready=nextstepA!=null&&nextstepB!=null;
            if(ready){
                playerA.getSteps().add(nextstepA);
                playerB.getSteps().add(nextstepB);
            }
        }finally {
            lock.unlock();
        }

        if(ready){
            judge();
            if("playing".equals(status)&&++round>=MAX_ROUNDS){
                status="over";
                loser="all";
            }
            if("playing".equals(status)){
                sendMove();
                schedule(this::beginRound);
            }else {
                sendResult();
            }
        }else if(tick<MAX_WAIT_TICKS){
            schedule(() -> waitForSteps(tick+1));
        }else {//超时，没有操作的一方判负
            status="over";
            lock.lock();
            try {
                if(nextstepA==null&&nextstepB==null){
                    loser="all";
                }
                else if(nextstepA==null){
                    loser="A";
                }else {
                    loser="B";
                }
            }finally {
                lock.unlock();
            }
            sendResult();
        }
    }

    private String getMapString(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<this.rows;i++){
            for(int j=0;j<this.cols;j++){
                sb.append(g[i][j]);
            }
        }
        return sb.toString();
    }

    private void updateUserRating(Player player,Integer rating){
        User user=WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void SaveToDatabase() {//存储对局

        Integer ratingA=WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB=WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if("A".equals(loser)){
            ratingA=ratingA-2;
            ratingB=ratingB+5;
        }
        else if("B".equals(loser)){
            ratingB=ratingB-2;
            ratingA=ratingA+5;
        }
        updateUserRating(playerA,ratingA);
        updateUserRating(playerB,ratingB);

        Record record=new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult(){//向两个client发生结果
        JSONObject resp = new JSONObject();
        resp.put("event","result");
        resp.put("loser",loser);
        SaveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    private  boolean check_valid(List<Cell> cellsA,List<Cell> cellsB){
        int n=cellsA.size();
        Cell cell=cellsA.get(n-1);
        if(g[cell.x][cell.y]==1){
            return false;
        }
        for(int i=0;i<n-1;i++){
            if(cellsA.get(i).x==cell.x&&cellsA.get(i).y==cell.y){
                return false;
            }
        }
        for(int i=0;i<n-1;i++){
            if(cellsB.get(i).x==cell.x&&cellsB.get(i).y==cell.y){
                return false;
            }
        }
        return true;
    }

    private void judge(){//判断玩家下一步操作是否合法
        List<Cell> cellsA=playerA.getCell();
        List<Cell> cellsB=playerB.getCell();
        boolean validA=check_valid(cellsA,cellsB);
        boolean validB=check_valid(cellsB,cellsA);
        if(!validA||!validB){
            status="over";

            if(!validA&&!validB){
                loser="all";
            }else if(!validA){
                loser="A";
            }else {
                loser="B";
            }
        }
    }

    private void sendAllMessage(String msg){
        for(Player player:List.of(playerA,playerB)){
            WebSocketServer client=WebSocketServer.users.get(player.getId());//只取一次，避免判空之后对方刚好断线
            if(client!=null){
                client.sendMessage(msg);
            }
        }

    }

    private void sendMove(){//向两个client传递移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event","move");
            resp.put("A_direction",nextstepA);
            resp.put("B_direction",nextstepB);
            sendAllMessage(resp.toJSONString());
            nextstepA=nextstepB=null;
        }finally {
            lock.unlock();
        }

    }

}
