package com.kob.botrunningsystem.Service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class Consumer {

    private static RestTemplate restTemplate;

    private static String receiveBotMoveUrl;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    @Value("${kob.backend.url}")
    public void setBackendUrl(String url) {
        Consumer.receiveBotMoveUrl = url + "/pk/receive/bot/move";
    }

    public void startTimeOut(Sandbox sandbox, long timeout, Bot bot) {
        try {
            Integer direction = sandbox.run(bot.getBotCode(), bot.getInput(), timeout);

            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("user_id", bot.getUserId().toString());
            data.add("direction", direction.toString());
            restTemplate.postForObject(receiveBotMoveUrl, data, String.class);

            System.out.println("move: " + bot.getUserId() + " " + direction);
        } catch (Exception e) {
            System.out.println("bot failed: " + bot.getUserId() + " " + e.getMessage());
        }
    }
}
