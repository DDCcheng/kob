package com.kob.backend.service.impl.PK;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.service.PK.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("Receive BotMove "+userId+" "+direction+" ");
        if(WebSocketServer.users.get(userId) != null) {
            Game game=WebSocketServer.users.get(userId).game;

            if(game!=null){
                if(game.getPlayerA().getId().equals(userId)){
                    game.setNextstepA(direction);
                } else if (game.getPlayerB().getId().equals(userId)) {
                    game.setNextstepB(direction);
                }
            }
        }
        return "bot move success";
    }
}
