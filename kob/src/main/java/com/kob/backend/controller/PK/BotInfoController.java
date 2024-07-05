package com.kob.backend.controller.PK;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/PK/")
public class BotInfoController {


    @RequestMapping("/getbotinfo/")
    public List<Map<String,String>> getBotInfo(){
        List<Map<String,String>> list=new LinkedList<>();
        Map<String,String> bot1 =new HashMap<>();
        bot1.put("name","sword");
        bot1.put("rating","1500");
        bot1.put("rank","1");
        Map<String,String> bot2 =new HashMap<>();
        bot2.put("name","tiger");
        bot2.put("rating","15");
        bot2.put("rank","100");
        list.add(bot2);
        list.add(bot1);
        return list;
    }
}
