package com.kob.backend.service.impl.user.account;

import com.kob.backend.service.user.account.RefreshService;
import com.kob.backend.service.user.account.RefreshTokenService;
import com.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RefreshServiceImpl implements RefreshService {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public Map<String, String> refresh(String refreshToken) {
        Map<String, String> map = new HashMap<>();
        Integer userId = refreshTokenService.consume(refreshToken);
        if (userId == null) {
            map.put("error_message", "登录已过期");
            return map;
        }
        //轮换：旧的refresh token已作废，签发一对新的
        map.put("error_message", "SUCCESS");
        map.put("token", JwtUtil.createJWT(userId.toString()));
        map.put("refresh_token", refreshTokenService.create(userId));
        return map;
    }
}