package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.RefreshService;
import com.kob.backend.utils.RefreshCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RefreshController {

    @Autowired
    private RefreshService refreshService;

    @Autowired
    private RefreshCookie refreshCookie;

    @PostMapping("/user/account/refresh/")
    public Map<String,String> refresh(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken,
                                      HttpServletResponse response) {
        return refreshCookie.moveToCookie(refreshService.refresh(refreshToken), response);
    }
}