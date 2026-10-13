package com.kob.botrunningsystem.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Bot implements com.kob.botrunningsystem.Utils.BotInterface {

    static class Cell{
        public int x,y;

        public Cell(int x, int y){
            this.x = x;
            this.y = y;
        }

    }

    private boolean check_tail_increasing(int step){
        if (step<=10) return true;
        return step%3==1;
    }

    public List<Cell> getCell(int sx, int sy,String steps){
        steps=steps.substring(1,steps.length()-1);
        List<Cell> res=new ArrayList<>();

        int [] dx={-1,0,1,0};
        int [] dy={0,1,0,-1};
        int x=sx;
        int y=sy;
        int step=0;
        res.add(new Cell(x,y));
        for(int i=0;i<steps.length();i++){
            int d=steps.charAt(i)-'0';
            x+=dx[d];
            y+=dy[d];
            res.add(new Cell(x,y));
            if(!check_tail_increasing(++step)){
                res.remove(0);
            }
        }
        return res;
    }

    @Override
    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        int [][] g=new int[13][14];
        for(int i=0,k=0;i<13;i++){
            for(int j=0;j<14;j++,k++){
                if(strs[0].charAt(k)=='1'){
                    g[i][j]=1;
                }
            }
        }

        int aSx=Integer.parseInt(strs[1]);
        int aSy=Integer.parseInt(strs[2]);

        int bSx=Integer.parseInt(strs[4]);
        int bSy=Integer.parseInt(strs[5]);

        List<Cell> aCells=getCell(aSx,aSy,strs[3]);
        List<Cell> bCells=getCell(bSx,bSy,strs[6]);

        for(Cell c:aCells){
            g[c.x][c.y]=1;
        }
        for(Cell c:bCells){
            g[c.x][c.y]=1;
        }
        //对每个能走的方向做一次flood-fill，选走过去以后可达区域最大的那个，
        //避免钻进被墙和蛇身围住的死胡同
        int [] dx={-1,0,1,0};
        int [] dy={0,1,0,-1};
        Cell head=aCells.get(aCells.size()-1);
        int best=0,bestArea=-1;
        for(int i=0;i<4;i++){
            int x=head.x+dx[i];
            int y=head.y+dy[i];
            if(x>=0&&x<13&&y>=0&&y<14&&g[x][y]==0){
                int area=floodFill(g,x,y);
                if(area>bestArea){
                    bestArea=area;
                    best=i;
                }
            }
        }
        return best;
    }

    //从(sx,sy)出发做flood-fill，返回能到达的空格数
    private int floodFill(int [][] g,int sx,int sy){
        int [] dx={-1,0,1,0};
        int [] dy={0,1,0,-1};
        boolean [][] visited=new boolean[13][14];
        ArrayDeque<Cell> queue=new ArrayDeque<>();
        queue.add(new Cell(sx,sy));
        visited[sx][sy]=true;
        int area=0;
        while(!queue.isEmpty()){
            Cell c=queue.poll();
            area++;
            for(int i=0;i<4;i++){
                int x=c.x+dx[i];
                int y=c.y+dy[i];
                if(x>=0&&x<13&&y>=0&&y<14&&g[x][y]==0&&!visited[x][y]){
                    visited[x][y]=true;
                    queue.add(new Cell(x,y));
                }
            }
        }
        return area;
    }
}
