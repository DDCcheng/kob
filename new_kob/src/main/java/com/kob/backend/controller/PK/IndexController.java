package com.kob.backend.controller.PK;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/PK/")
public class IndexController {

    @RequestMapping("Index/")
    public String index(){
        return "/PK/Index.html";
    }

}