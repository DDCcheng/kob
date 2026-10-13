package com.kob.backend.controller.user.account;

import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.account.WsTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WsTicketController {

    @Autowired
    private WsTicketService wsTicketService;

    @PostMapping("/user/account/ws-ticket/")
    public Map<String,String> create(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String,String> map = new HashMap<>();
        map.put("error_message", "SUCCESS");
        map.put("ticket", wsTicketService.create(userDetails.getUser().getId()));
        return map;
    }
}