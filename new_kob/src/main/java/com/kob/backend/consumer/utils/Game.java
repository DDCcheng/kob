package com.kob.backend.consumer.utils;

import java.util.Random;

public class Game {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final int [][] g;
    private final static int[] dx={-1,0,1,0};
    private final static int[] dy={0,1,0,1,0};

    public  Game(Integer rows, Integer cols, Integer inner_walls_count){
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
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
            g[0][r]=g[0][this.rows-1]=1;
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
}
