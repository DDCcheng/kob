package com.kob.backend.service.user.account;

import java.util.Map;

public interface RefreshService {
    Map<String,String> refresh(String refreshToken);
}