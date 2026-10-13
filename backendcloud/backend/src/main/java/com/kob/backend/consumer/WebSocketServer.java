package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.service.user.account.WsTicketService;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{ticket}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    private User user;

    private Session session=null;

    public static UserMapper userMapper;

    public  static RecordMapper recordMapper;

    private static BotMapper botMapper;

    public  static RestTemplate restTemplate;

    public volatile Game game=null;

    private static String addPlayerUrl;

    private static String removePlayerUrl;

    public static String addBotUrl;

    @Value("${kob.matching-system.url}")
    public void setMatchingSystemUrl(String url){
        WebSocketServer.addPlayerUrl=url+"/player/add/";
        WebSocketServer.removePlayerUrl=url+"/player/remove/";
    }

    @Value("${kob.bot-running-system.url}")
    public void setBotRunningSystemUrl(String url){
        WebSocketServer.addBotUrl=url+"/bot/add/";
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper=userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper){
        WebSocketServer.recordMapper=recordMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper){
        WebSocketServer.botMapper=botMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        WebSocketServer.restTemplate=restTemplate;
    }

    private static WsTicketService wsTicketService;

    @Autowired
    public void setWsTicketService(WsTicketService wsTicketService){
        WebSocketServer.wsTicketService=wsTicketService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("ticket") String ticket) throws IOException {
        // 建立连接。URL里带的是一次性票据而不是access token，票据30秒过期且只能用一次
        this.session = session;
        Integer userId = wsTicketService.consume(ticket);
        this.user = userId == null ? null : userMapper.selectById(userId);
        if(user==null){
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "invalid ticket"));
            return;
        }
        users.put(userId, this);
        System.out.println("connected to websocket: " + userId);
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        if(this.user==null) return;
        System.out.println("disconnected from websocket: " + this.user.getId());
        //只移除自己这条连接，避免把同一用户刚建立的新连接删掉
        if(users.remove(this.user.getId(), this)){
            try {
                stopMatching();//断线后退出匹配池，不再被匹配上
            } catch (Exception e) {
                System.out.println("stop matching failed: " + e.getMessage());
            }
        }
    }

    public static void startGame(Integer aId, Integer bId,Integer aBotId,Integer bBotId){
        User a=userMapper.selectById(aId), b=userMapper.selectById(bId);
        Bot botA=botMapper.selectById(aBotId), botB=botMapper.selectById(bBotId);
        //只能用自己的Bot出战，不是自己的就当作亲自出马
        if(botA!=null&&!botA.getUserId().equals(aId)) botA=null;
        if(botB!=null&&!botB.getUserId().equals(bId)) botB=null;
        Game game=new Game(13,
                14,
                20,
                a.getId(),
                b.getId(),
                botA,
                botB
        );
        game.createMap();
        if(users.get(a.getId())!=null){
            users.get(a.getId()).game=game;
        }
        if(users.get(b.getId())!=null){
            users.get(b.getId()).game=game;
        }

        game.start();

        JSONObject respGame =new JSONObject();
        respGame.put("a_id",game.getPlayerA().getId());
        respGame.put("b_id",game.getPlayerB().getId());
        respGame.put("a_sx",game.getPlayerA().getSx());
        respGame.put("b_sx",game.getPlayerB().getSx());
        respGame.put("a_sy",game.getPlayerA().getSy());
        respGame.put("b_sy",game.getPlayerB().getSy());
        respGame.put("map",game.getG());
        JSONObject respA=new JSONObject();
        respA.put("event","start-matching");
        respA.put("opponent_username",b.getUsername());
        respA.put("opponent_photo",b.getPhoto());
        respA.put("game",respGame);
        if(users.get(a.getId())!=null){
            users.get(a.getId()).sendMessage(respA.toJSONString());
        }


        JSONObject respB=new JSONObject();
        respB.put("event","start-matching");
        respB.put("opponent_username",a.getUsername());
        respB.put("opponent_photo",a.getPhoto());
        respB.put("game",respGame);
        if(users.get(b.getId())!=null){
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }

    }

    private void startMatching(Integer botId){
        System.out.println("startMatching");
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        data.add("rating",this.user.getRating().toString());
        data.add("bot_id",botId.toString());
        restTemplate.postForObject(addPlayerUrl,data,String.class);
    }

    private void stopMatching(){
        System.out.println("stopMatching");
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl,data,String.class);
    }


    private void move(Integer direction){
        Game game=this.game;
        if(game==null||direction==null) return;
        if(game.getPlayerA().getId().equals(user.getId())){
            if(game.getPlayerA().getBotId().equals(-1))//-1是人亲自出马
                game.setNextstepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId().equals(-1))
                game.setNextstepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        if(this.user==null) return;
        JSONObject data=JSONObject.parseObject(message);
        String event=data.getString("event");
        if("start-matching".equals(event)){
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)){
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}

