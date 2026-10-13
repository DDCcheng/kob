package com.kob.backend.controller.user.account;


import com.kob.backend.service.user.account.LoginService;
import com.kob.backend.utils.RefreshCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private RefreshCookie refreshCookie;


    @PostMapping("/user/account/token/")
    public Map<String,String> gettoken(@RequestParam Map<String,String> map, HttpServletResponse response) {
        String username = map.get("username");
        String password = map.get("password");
        return refreshCookie.moveToCookie(loginService.gettoken(username, password), response);
    }
}
