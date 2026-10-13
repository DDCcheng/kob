package com.kob.backend.service.impl.user.account;

import com.kob.backend.service.user.account.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    public static final Duration TTL = Duration.ofDays(14);

    private static final String KEY_PREFIX = "kob:refresh:";

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String create(Integer userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(key(refreshToken), userId.toString(), TTL);
        return refreshToken;
    }

    @Override
    public Integer consume(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) return null;
        //GETDEL是原子操作，同一个refresh token只能成功换一次
        String userId = redisTemplate.opsForValue().getAndDelete(key(refreshToken));
        return userId == null ? null : Integer.valueOf(userId);
    }

    @Override
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) return;
        redisTemplate.delete(key(refreshToken));
    }

    //Redis里只存哈希值，即使Redis泄露也拿不到可用的token
    private String key(String refreshToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}