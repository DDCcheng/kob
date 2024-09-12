package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Record;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;

    private final Integer cols;

    private final Integer inner_walls_count;

    private final int [][] g;

    private final static int[] dx={-1,0,1,0};

    private final static int[] dy={0,1,0,1,0};

    private final Player playerA,playerB;

    private  Integer nextstepA=null;

    private  Integer nextstepB=null;

    private ReentrantLock lock = new ReentrantLock();

    private String status="playing";//playing表示正在游戏,over表示游戏结束

    private String loser="";//all表示平局,A表示A输

    public  Game(Integer rows, Integer cols, Integer inner_walls_count,Integer idA,Integer idB){
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        playerA =new Player(idA,rows-2,1,new ArrayList<>());
        playerB =new Player(idB,1,cols-2,new ArrayList<>());
    }

    public Player getPlayerA(){
        return playerA;
    }

    public Player getPlayerB(){
        return playerB;
    }

    public void setNextstepA(Integer nextstepA){
        lock.lock();
        try {
            this.nextstepA=nextstepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextstepB(Integer nextstepB){
        lock.lock();
        try {
            this.nextstepB=nextstepB;
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

    private boolean nextstep() { //等待两名玩家的下一步操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(int i=0;i<5;i++){
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    if(nextstepA!=null&&nextstepB!=null){
                        playerA.getSteps().add(nextstepA);
                        playerB.getSteps().add(nextstepB);
                        return true;
                    }
                }finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return false;
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

    private void SaveToDatabase() {
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
        WebSocketServer.users.get(playerA.getId()).sendMessage(msg);
        WebSocketServer.users.get(playerB.getId()).sendMessage(msg);
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


    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            if(nextstep()){
                judge();
                if(Objects.equals(status, "playing")){
                    sendMove();
                }else {
                    sendResult();
                    break;
                }
            }else {
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
                break;
            }
        }
    }

}
