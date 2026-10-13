package com.kob.backend.utils;

import com.kob.backend.service.impl.user.account.RefreshTokenServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

//refresh token只放在HttpOnly的Cookie里，前端的JS读不到
@Component
public class RefreshCookie {

    public static final String NAME = "refresh_token";

    @Value("${kob.cookie.secure}")
    private boolean secure;

    @Value("${kob.cookie.path}")
    private String path;

    //把service返回结果里的refresh token挪到Cookie中，不出现在响应体里
    public Map<String, String> moveToCookie(Map<String, String> result, HttpServletResponse response) {
        String refreshToken = result.remove(NAME);
        if (refreshToken != null) {
            write(response, refreshToken, RefreshTokenServiceImpl.TTL);
        }
        return result;
    }

    public void clear(HttpServletResponse response) {
        write(response, "", Duration.ZERO);
    }

    private void write(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(path)//只有登录、刷新、退出这几个接口会带上它
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}