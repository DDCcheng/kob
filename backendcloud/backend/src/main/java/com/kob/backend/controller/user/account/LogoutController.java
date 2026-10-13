package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.RefreshTokenService;
import com.kob.backend.utils.RefreshCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LogoutController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshCookie refreshCookie;

    @PostMapping("/user/account/logout/")
    public Map<String,String> logout(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken,
                                     HttpServletResponse response) {
        refreshTokenService.revoke(refreshToken);
        refreshCookie.clear(response);
        Map<String,String> map = new HashMap<>();
        map.put("error_message", "SUCCESS");
        return map;
    }
}