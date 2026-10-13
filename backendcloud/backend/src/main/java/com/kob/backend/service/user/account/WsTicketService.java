package com.kob.backend.service.user.account;

public interface WsTicketService {
    String create(Integer userId);//签发一张建立WebSocket连接用的一次性票据

    Integer consume(String ticket);//校验并作废，返回对应的userId，无效时返回null
}