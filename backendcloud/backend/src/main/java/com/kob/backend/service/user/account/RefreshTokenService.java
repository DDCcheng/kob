package com.kob.backend.service.user.account;

public interface RefreshTokenService {
    String create(Integer userId);//签发一个新的refresh token

    Integer consume(String refreshToken);//校验并作废，返回对应的userId，无效时返回null

    void revoke(String refreshToken);
}