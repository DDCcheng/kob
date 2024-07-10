package com.kob.backend.controller.PK;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pk/")
public class BotInfoController {

    @RequestMapping("/getbotinfo/")
    public Map<String,String> getBotInfo(){
        List<Map<String,String>> list=new LinkedList<>();
        Map<String,String> map =new HashMap<>();
        map.put("name","apple");
        map.put("rating","1500");
        map.put("rank","1");
        return map;
    }
}