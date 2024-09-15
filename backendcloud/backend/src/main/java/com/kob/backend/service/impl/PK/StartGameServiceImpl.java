package com.kob.backend.service.impl.PK;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.PK.StartGameService;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl  implements StartGameService {
    @Override
    public String startGame(Integer aId, Integer bId) {
        System.out.println("StartGame"+aId+bId);
        WebSocketServer.startGame(aId,bId);
        return "StartGame";
    }
}
