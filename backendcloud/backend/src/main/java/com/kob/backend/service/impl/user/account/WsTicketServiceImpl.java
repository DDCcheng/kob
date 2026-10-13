package com.kob.backend.service.impl.user.account;

import com.kob.backend.service.user.account.WsTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

//浏览器的WebSocket API不能自定义请求头，token只能放在URL里，而URL会被代理和网关记进访问日志。
//所以URL里不放access token，改放一张30秒过期、只能用一次的票据，被记下来也没法再用
@Service
public class WsTicketServiceImpl implements WsTicketService {

    private static final Duration TTL = Duration.ofSeconds(30);

    private static final String KEY_PREFIX = "kob:ws-ticket:";

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String create(Integer userId) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, userId.toString(), TTL);
        return ticket;
    }

    @Override
    public Integer consume(String ticket) {
        if (ticket == null || ticket.isEmpty()) return null;
        String userId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + ticket);
        return userId == null ? null : Integer.valueOf(userId);
    }
}